/**
 * Copyright 2015 Mike Baum
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package mb.rxui.property;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import mb.rxui.EventLoop;
import mb.rxui.Preconditions;
import mb.rxui.event.EventStream;
import mb.rxui.property.operator.OperatorFilterToOptional;
import mb.rxui.property.operator.OperatorIsDirty;
import mb.rxui.property.operator.OperatorMap;
import mb.rxui.property.operator.OperatorSwitchMap;
import mb.rxui.property.operator.OperatorTake;
import mb.rxui.property.operator.PropertyConditionBuilder;
import mb.rxui.property.operator.PropertyOperator;
import mb.rxui.property.publisher.CombinePropertyPublisher;
import mb.rxui.property.publisher.JustPropertyPublisher;
import mb.rxui.property.publisher.PropertyPublisher;
import mb.rxui.subscription.Subscription;
import rx.Observable;
import rx.subscriptions.Subscriptions;

/**
 * A property that can only be observed. This is effectively a read-only version
 * of a {@link Property}.<br>
 * <br>
 * NOTES:<br>
 * 1) A property stream will emit a new value via
 * {@link #onChanged(Consumer)} when it's value changes.<br>
 * 2) Once the property is disposed it will emit an onDiposed event.<br>
 * 3) A property stream is assumed to always contain a value.<br>
 * 
 * @param <M>
 *            the type of the value this property stream emits.
 * @see Property
 */
public class PropertyStream<M> implements Supplier<M> {
    
    private final PropertyPublisher<M> propertyPublisher;
    private final EventLoop eventLoop;
    private final M initialValue;
    
    /**
     * Creates a new {@link PropertyStream}
     * @param propertyPublisher some property publisher to back this property stream.
     */
    protected PropertyStream(PropertyPublisher<M> propertyPublisher) {
        this.propertyPublisher = requireNonNull(propertyPublisher);
        this.eventLoop = EventLoop.createEventLoop();
        initialValue = requireNonNull(propertyPublisher.get());
    }
    
    /**
     * Creates a property stream for the provided property publisher.<br>
     * <br>
     * NOTE:<br>
     * 1) Only use this constructor if the provided property publisher is a read only source, since
     * there is no guarantee that the provided publisher will respect the re-entrancy contract that
     * is ensured by using the {@link Property} class.
     * 
     * @param propertyPublisher some property publisher
     * @return a new {@link PropertyStream} that is linked to the provided publisher
     */
    public static <M> PropertyStream<M> create(PropertyPublisher<M> propertyPublisher) {
        return new PropertyStream<>(propertyPublisher);
    }

    /**
     * Gets the current value of this property.
     * 
     * NOTE: Calling get will always return a non-null value. If not the
     * implementation is misbehaved.
     * 
     * @return the current value
     * @throws IllegalStateException
     *             if called from a thread other than the one that this property
     *             was created from.
     */
    @Override
    public final M get() {
        eventLoop.checkInEventLoop();
        return propertyPublisher.get();
    }

    /**
     * Adds an observer to this property stream.
     * @param observer some property observer
     * @return a {@link Subscription} that can be used to cancel the subscription.
     */
    public final Subscription observe(PropertyObserver<M> observer) {
        eventLoop.checkInEventLoop();
        return propertyPublisher.subscribe(observer);
    }

    /**
     * Observe onChange and onDestroy events.
     * 
     * @param onChanged
     *            some listener of onChanged events.
     * @param onDisposed
     *            some listener of onDisposed events.
     * @return a {@link Subscription} that can be used to cancel the
     *         subscription.
     * @throws IllegalStateException
     *             if called from a thread other than the one that this property
     *             was created from.
     */
    public final Subscription observe(Consumer<M> onChanged, Runnable onDisposed) {
        return observe(PropertyObserver.create(onChanged, onDisposed));
    }

    /**
     * Adds a listener that will be updated when the value of this property
     * changes.
     * 
     * NOTE: The listener will be called back immediately with the current value
     * when subscribing.
     * 
     * @param onChanged
     *            some listener to update when this property's value changes
     * @return a {@link Subscription} that can be used to cancel the
     *         subscription.
     * @throws IllegalStateException
     *             if called from a thread other than the one that this property
     *             was created from.
     */
    public final Subscription onChanged(Consumer<M> onChanged) {
        return observe(PropertyObserver.create(onChanged));
    }

    /**
     * Adds some {@link Runnable} to execute when this property is disposed.
     * 
     * @param onDisposedAction
     *            some runnable to run when this property is disposed.
     * @return a {@link Subscription} that can be used to cancel the
     *         subscription.
     * @throws IllegalStateException
     *             if called from a thread other than the one that this property
     *             was created from.
     */
    public final Subscription onDisposed(Runnable onDisposedAction) {
        return observe(PropertyObserver.create(onDisposedAction));
    }
    
    /**
     * Transforms this Property Stream by the provided mapper function.
     * 
     * @param mapper some function the emitted values of this property stream.
     * @return a new {@link PropertyStream} with the values transformed by the provided mapper.
     */
    public final <R> PropertyStream<R> map(Function<M, R> mapper) {
        return lift(new OperatorMap<>(mapper));
    }
    
    /**
     * Filters out the values emitted by this property that do not satisfy the
     * provided predicate. If the current value of this property stream does
     * not satisfy the predicate the current value of the filtered property will
     * become {@link Optional#empty()}.
     * 
     * @param predicate
     *            some predicate to use to filter this property stream.
     * @return a new {@link PropertyStream} that optionally emits the
     *         current value or empty if the current value does not satisfy the
     *         predicate.
     */
    public final PropertyStream<Optional<M>> filterToOptional(Predicate<M> predicate) {
        return lift(new OperatorFilterToOptional<>(predicate));
    }
    
    /**
     * Filters out the values emitted by this property that do not satisfy the
     * provided predicate.
     * 
     * @param predicate
     *            some predicate to use to filter this property stream.
     * @return a new {@link EventStream} that only emits values that satisfy the predicate.
     */
    public final EventStream<M> filter(Predicate<M> predicate) {
        return asEventStream().filter(predicate);
    }
    
    /**
     * Throttles emissions from this property, such that an event will only be
     * emitted after an amount of event silence.
     * 
     * @param timeout
     *            amount of event silence to wait for before emitting an event
     * @param timeUnit
     *            time unit for the provided time.
     * @return an {@link EventStream} that will only emit values after the
     *         prescribed amount of event silence has passed.
     */
    public final EventStream<M> debounce(long timeout, TimeUnit timeUnit) {
        return asEventStream().debounce(timeout, timeUnit);
    }
    
    /**
     * Creates a new {@link PropertyStream} that checks if the current value
     * of this property stream equals the provided value.
     * 
     * @param value
     *            some value to compare the current value to.
     * @return true if the current value matches the provided value to compare
     *         to, false otherwise.
     */
    public final PropertyConditionBuilder<M> is(M value) {
        return new PropertyConditionBuilder<M>(this, value);
    }
    
    /**
     * Creates a new property stream that emits true or false whether or not
     * the current value of this property stream differs from the initial
     * value.
     * 
     * @return a new {@link PropertyStream} that emits true if the current
     *         value is different than the initial value, false otherwise.
     */
    public final PropertyStream<Boolean> isDirty() {
        return lift(new OperatorIsDirty<M>(initialValue));
    }
    
    /**
     * Creates a new {@link PropertyStream} that when subscribed to will
     * only every emit as many items as specified by the provided amount
     * parameter.<br>
     * <br>
     * NOTE: The returned {@link PropertyStream} will always return the
     * latest value of the property stream that it was derived from.
     * 
     * @param amount
     *            the amount of values to emit
     * @return a new {@link PropertyStream} that only emits as many items as
     *         specified in the amount parameter
     * @throws IllegalArgumentException if zero elements is requested
     */
    public final PropertyStream<M> take(int amount) {
        Preconditions.checkArgument(amount > 0, "Cannot take zero elements");
        return lift(new OperatorTake<>(amount));
    }
    
    /**
     * Scans this stream by combining the previously computed value of R with
     * every property change that is emitted generating a new R.
     * 
     * The event stream created by this method, will emit a value immediately.
     * 
     * @param scanFunction
     *            some function that will be applied to each emitted value and
     *            the last computed value generating a new R.
     * @return a new {@link EventStream} of values computed as per the scan
     *         function.
     */
    public final <R> EventStream<R> scan(BiFunction<M, Optional<R>, R> scanFunction) {
        return asEventStream().scan(scanFunction);
    }
    
    /**
     * Scans this stream by combining the previously computed value of R with
     * every event that is emitted generating a new R.
     * 
     * The event stream created by this method, will emit a value immediately
     * using the seed value and then a new value every time this stream emits an
     * event.
     * 
     * @param scanFunction
     *            some function that will be applied to each emitted value and
     *            the last computed value generating a new R.
     * @param seed
     *            some initial value to start the scan operation with. This
     *            value will be emitted immediately to all subscribers.
     * @return a new {@link EventStream} of values computed as per the scan
     *         function.
     */
    public final <R> EventStream<R> scan(BiFunction<M, R, R> scanFunction, R seed) {
        return asEventStream().scan(scanFunction, seed);
    }
    
    /**
     * Scans this stream by combining the previously computed value of M with
     * every property change that is emitted generating a new M.
     * 
     * The event stream created by this method, will emit a value immediately
     * using the seed value and then a new value every time this stream emits an
     * event.
     * 
     * This method is provided as a convenience, since
     * {@link #scan(BiFunction, Object)} could be called directly.
     * 
     * @param scanFunction
     *            some function that will be applied to each emitted value and
     *            the last computed value generating a new M.
     * @param seed
     *            some initial value to start the scan operation with. This
     *            value will be emitted immediately to all subscribers.
     * @return a new {@link EventStream} of values computed as per the scan
     *         function.
     */
    public final EventStream<M> accumulate(BinaryOperator<M> accumulator, M initialValue) {
        return asEventStream().scan(accumulator, initialValue);
    }
    
    /**
     * @return an {@link EventStream} of property change events for this
     *         property. The stream will not emit a value after subscribing
     *         until the property has changed at least once.
     */
    @SuppressWarnings("unchecked") // the use of the raw type in the method reference is okay.
    public final EventStream<PropertyChangeEvent<M>> changeEvents() {
        return asEventStream().changes(PropertyChangeEvent::new);
    }
    
    /**
     * Creates a new property stream that uses the provided switch function to
     * change the bound stream. This is, as the name implies, just like switch
     * but for streams.<br>
     * <br>
     * See {@link EventStream#switchMap(Function)}
     * 
     * @param switchFunction
     *            function that can be used to switch between a set of source
     *            property streams.
     * @return a new property stream that uses the provided switchFunction to
     *         switch between a set of source property streams.
     */
    public final <R> PropertyStream<R> switchMap(Function<M, PropertyStream<R>> switchFunction) {
        return lift(new OperatorSwitchMap<>(switchFunction));
    }
    
    /**
     * Using the provided operator creates a new, converted property stream.
     * 
     * @param operator
     *            some operator that converts the value stream.
     * @return a new {@link PropertyStream} which results from applying the
     *         provided operator to this property stream.
     */
    public final <R> PropertyStream<R> lift(PropertyOperator<M, R> operator) {
        Objects.requireNonNull(operator);
        return new PropertyStream<>(operator.apply(propertyPublisher));
    }
    
    /**
     * Creates an Observable that is backed by this property.<br>
     * <br>
     * NOTES:<br>
     * 1) If this property is destroyed, the observable will be completed.<br>
     * 2) Subscribers to this observable will never have their onError method
     * called, since properties do not propagate errors.
     * 
     * @return an {@link Observable} that is backed by this property.
     */
    public final Observable<M> asObservable() {
        return Observable.create(subscriber -> {
            Subscription subscription = observe(subscriber::onNext, subscriber::onCompleted);
            subscriber.add(Subscriptions.create(subscription::dispose));
        });
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((get() == null) ? 0 : get().hashCode());
        return result;
    }

    /**
     * Compares the current value of this property stream to the current
     * value of the provided property stream.
     * 
     * @param obj
     *            some other property to compare for equality
     * @return true if the provided object is a property and if the current
     *         value of this property and the provided object are equal, false
     *         otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
            
        return get().equals(((Supplier<?>) obj).get());
    }
    
    /**
     * Creates an {@link EventStream} backed by this property stream.
     * @return a new {@link EventStream} backed by this property stream.
     * @throws IllegalStateException
     *             if called from a thread other than the one that this property
     *             was created from.
     */
    private EventStream<M> asEventStream() {
        eventLoop.checkInEventLoop();
        return new EventStream<>(observer -> {
            return observe(PropertyObserver.create(observer::onEvent, observer::onCompleted));
        });
    }

    /**
     * Combines the values of two property streams and produces a new result
     * using the provided function any time either of the values changes.
     * 
     * @param stream1
     *            the first stream to combine
     * @param stream2
     *            the second stream to combine
     * @param combiner
     *            some function that will be called any time either of the
     *            provided streams changes
     * @return a new {@link PropertyStream} that will emit the result of combining
     *         the values of the provided streams using the provided
     *         function any time either streams' value changes.
     */
    public static <T1, T2, R> PropertyStream<R> combine(PropertyStream<T1> stream1, 
                                                        PropertyStream<T2> stream2, 
                                                        BiFunction<T1, T2, R> combiner) {
        List<PropertyStream<?>> streams = Arrays.asList(stream1, stream2);
        Supplier<R> combineSupplier = () -> combiner.apply(stream1.get(), stream2.get());
        
        return new PropertyStream<R>(new CombinePropertyPublisher<R>(combineSupplier, streams));
    }
    
    /**
     * Creates a new property stream that only emits the provided value and then
     * completes. Another name for this would be a constant property.
     * 
     * @param value
     *            the value to emit
     * @return a new {@link PropertyStream} that when subscribed to emits the
     *         provided values and then signals disposed.
     */
    public static <R> PropertyStream<R> just(R value) {
        return new PropertyStream<>(new JustPropertyPublisher<>(value));
    }
}