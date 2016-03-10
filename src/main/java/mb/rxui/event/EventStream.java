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
package mb.rxui.event;

import static java.util.Objects.requireNonNull;
import static mb.rxui.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import mb.rxui.EventLoop;
import mb.rxui.event.operator.Operator;
import mb.rxui.event.operator.OperatorChanges;
import mb.rxui.event.operator.OperatorDebounce;
import mb.rxui.event.operator.OperatorFilter;
import mb.rxui.event.operator.OperatorMap;
import mb.rxui.event.operator.OperatorScan;
import mb.rxui.event.operator.OperatorScanOptional;
import mb.rxui.event.operator.OperatorSwitchMap;
import mb.rxui.event.publisher.EventPublisher;
import mb.rxui.event.publisher.FlattenPublisher;
import mb.rxui.event.publisher.LiftEventPublisher;
import mb.rxui.event.publisher.MergeEventPublisher;
import mb.rxui.property.Property;
import mb.rxui.property.PropertyStream;
import mb.rxui.subscription.RollingSubscription;
import mb.rxui.subscription.Subscription;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

/**
 * An EventStream represents a stream that emits discrete events. This differs
 * from a {@link Property} since an {@link EventStream} does not have a current
 * value.
 * 
 * <p>
 * Notes:
 * <li>An Event Stream will emit zero, one or many events followed by one
 * completed event.
 * <li>An Event Stream can only be interacted with on the same thread that it
 * was created on. Attempting to access any of the methods outside of the thread
 * it was created on will throw an {@link IllegalStateException}
 * <li>An Event Stream does not <i><b>"remember"</b></i> the last event that it
 * published. Therefore new observers will not be called back immediately as
 * with a {@link Property}.
 * 
 * @param <E>
 *            The type of events emitted by this event stream.
 */
public class EventStream<E> {
    
    private final EventPublisher<E> eventPublisher;
    private final EventLoop eventLoop;
    
    protected EventStream(EventPublisher<E> eventPublisher, EventLoop eventLoop) {
        this.eventPublisher = requireNonNull(eventPublisher);
        this.eventLoop = requireNonNull(eventLoop);
    }
    
    /**
     * Creates an EventStream using the provided {@link EventPublisher}.
     * 
     * @param eventPublisher
     *            some {@link EventPublisher} to back this stream.
     */
    public EventStream(EventPublisher<E> eventPublisher) {        
        this(eventPublisher, EventLoop.createEventLoop());
    }
    
    /**
     * Subscribes to new events emitted by this stream.
     * 
     * @param eventHandler
     *            some event handler that should be fired when a new event is
     *            emitted by this stream.
     * @return a {@link Subscription} that can be used to stop the eventHandler
     *         from consuming events from this stream.
     * @throws IllegalStateException
     *             if called from a thread other than the thread that this event
     *             stream was created on.
     */
    public final Subscription onEvent(Consumer<E> eventHandler) {
        return observe(EventObserver.create(eventHandler));
    }
    
    /**
     * Subscribes to the completed event of this stream.
     * 
     * @param onCompletedAction some Runnable to execute when this stream is completed.
     * @return a {@link Subscription} that can be used to stop the onCompletedAction
     *         from firing when this stream completes.
     * @throws IllegalStateException
     *             if called from a thread other than the thread that this event
     *             stream was created on.
     */
    public final Subscription onCompleted(Runnable onCompletedAction) {
        return observe(EventObserver.create(onCompletedAction));
    }
    
    /**
     * Subscribes to onEvents and onCompleted events, via the provided
     * eventHandler and onCompleteAction.
     * 
     * @param observer
     *            some {@link EventObserver} to observe this stream.
     * @return a {@link Subscription} that can be used to stop this observer
     *         from responding to events.
     * @throws IllegalStateException
     *             if called from a thread other than the thread that this event
     *             stream was created on.
     */
    public final Subscription observe(Consumer<E> eventHandler, Runnable onCompleteAction) {
        return eventPublisher.subscribe(EventObserver.create(eventHandler, onCompleteAction));
    }
    
    /**
     * Subscribes to onEvents and onCompleted events, via the provided observer.
     * 
     * @param observer
     *            some {@link EventObserver} to observe this stream.
     * @return a {@link Subscription} that can be used to stop this observer
     *         from responding to events.
     * @throws IllegalStateException
     *             if called from a thread other than the thread that this event
     *             stream was created on.
     */
    public final Subscription observe(EventObserver<E> observer) {
        eventLoop.checkInEventLoop();
        return eventPublisher.subscribe(observer);
    }
    
    /**
     * Transforms this stream by the provided operator, creating a new stream.
     * 
     * @param operator
     *            some operator to transform this stream by.
     * @return a new {@link EventStream} transformed by the provided operator.
     */
    public final <R> EventStream<R> lift(Operator<E, R> operator) {
        requireNonNull(operator);
        return new EventStream<>(new LiftEventPublisher<>(operator, eventPublisher), eventLoop);
    }
    
    /**
     * Creates a new stream that transforms events emitted by this stream by the
     * provided mapper.
     * 
     * @param mapper
     *            some function to transform the events emitted by this stream.
     * @return a new {@link EventStream}, that transforms events emitted by this
     *         stream by the provided mapper.
     */
    public final <R> EventStream<R> map(Function<E, R> mapper) {
        return lift(new OperatorMap<>(mapper));
    }
    
    /**
     * Creates a new stream that filters the events emitted by this stream, such
     * that only those that satisfy the provided predicate are emitted.
     * 
     * @param predicate
     *            some predicate to filter this stream by.
     * @return A new {@link EventStream} that only emits values emitted by this
     *         stream that satisfy the provided predicate.
     */
    public final EventStream<E> filter(Predicate<E> predicate) {
        return lift(new OperatorFilter<>(predicate));
    }
    
    /**
     * Throttles emissions from this event stream, such that an event will only
     * be emitted after an amount of event silence.
     * 
     * @param timeout
     *            amount of event silence to wait for before emitting an event
     * @param timeUnit
     *            time unit for the provided time.
     * @return an {@link EventStream} that will only emit values after the
     *         prescribed amount of event silence has passed.
     */
    public final EventStream<E> debounce(long timeout, TimeUnit timeUnit) {
        return lift(new OperatorDebounce<>(eventLoop, timeout, timeUnit));
    }
    
    /**
     * Scans this stream by combining the previously computed value of R with
     * every event that is emitted generating a new R.
     * 
     * The event stream created by this method, will not emit a value until an
     * event is emitted.
     * 
     * @param scanFunction
     *            some function that will be applied to each emitted value and
     *            the last computed value generating a new R.
     * @return a new {@link EventStream} of values computed as per the scan
     *         function.
     */
    public final <R> EventStream<R> scan(BiFunction<E, Optional<R>, R> scanFunction) {
        return lift(new OperatorScanOptional<>(scanFunction));
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
    public final <R> EventStream<R> scan(BiFunction<E, R, R> scanFunction, R seed) {
        return lift(new OperatorScan<>(scanFunction, seed));
    }
    
    /**
     * Scans this stream by combining the previously computed value of R with
     * every event that is emitted generating a new R.
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
     *            the last computed value generating a new R.
     * @param seed
     *            some initial value to start the scan operation with. This
     *            value will be emitted immediately to all subscribers.
     * @return a new {@link EventStream} of values computed as per the scan
     *         function.
     */
    public final EventStream<E> accumulate(BinaryOperator<E> accumulator, E initialValue) {
        return scan(accumulator, initialValue);
    }
    
    /**
     * @return an {@link EventStream} of change events for this
     *         stream. The stream will not emit a value after subscribing
     *         until the stream has emitted at least two events.
     */
    public final <C> EventStream<C> changes(BiFunction<E, E, C> changeEventFactory) {
        return lift(new OperatorChanges<>(changeEventFactory));
    }
    
    /**
     * Merges the provided streams with this streams.
     * 
     * See {@link #merge(EventStream...)}
     * 
     * @param streams
     *            streams to merge with this stream
     * @return a new {@link EventStream} that results from merging this stream
     *         will all the provided streams.
     */
    @SafeVarargs
    public final EventStream<E> mergeWith(EventStream<E>... streams) {
        
        EventStream<E>[] eventStreams = Arrays.copyOf(streams, streams.length + 1);
        eventStreams[eventStreams.length -1] = this;
        
        return EventStream.merge(eventStreams);
    }
    
    /**
     * Merges the provided streams with this streams.
     * 
     * See {@link #merge(EventStream...)}
     * 
     * @param streams
     *            streams to merge with this stream
     * @return a new {@link EventStream} that results from merging this stream
     *         will all the provided streams.
     */
    public final EventStream<E> mergeWith(Iterable<EventStream<E>> streams) {
        
        List<EventStream<E>> streamList = 
                StreamSupport.stream(streams.spliterator(), false).collect(Collectors.toList());
        
        streamList.add(this);
        
        return EventStream.merge(streamList);
    }

    /**
     * Creates a new stream that can switch the output stream by the provided
     * switchFunction. Visually, this is roughly equivalent to the following:
     * <br>
     *                    _________________________________
     *                   |       switchFunction            | 
     *                   | [stream 1] -->                  |
     * [this stream] --> | [stream 2] --> switches between | --> [stream 1 | stream 2 | stream 3]
     *                   | [stream 3] -->     streams      |
     *                   |_________________________________|
     * <br>
     * @param switchFunction
     *            function that can be used to switch between a set of source
     *            event streams.
     * @return a new event stream that uses the provided switchFunction to switch
     *         between a set of source event streams.
     */
    public final <R> EventStream<R> switchMap(Function<E, EventStream<R>> switchFunction) {
        return lift(new OperatorSwitchMap<>(switchFunction));
    }
    
    /**
     * Like {@link #switchMap(Function)}, except that the switchMap creates
     * property streams. The property stream that is created, will have the same
     * lifetime as this event stream.
     * 
     * @param switchFunction
     *            function that can be used to switch between a set of source
     *            property streams.
     * @param initialValue
     *            the initial value for the property stream that is returned.
     * @return a new property stream that uses the provided switchFunction to
     *         switch between a set of source property streams.
     */
    public final <R> PropertyStream<R> switchMap(Function<E, PropertyStream<R>> switchFunction, R initialValue) {
        
        Property<R> property = Property.create(initialValue);
        RollingSubscription subscription = new RollingSubscription();
        EventStream<PropertyStream<R>> propertyEventStream = map(switchFunction);

        Subscription propertyStreamSubscription = 
                propertyEventStream.observe(stream -> subscription.set(property.bind(stream)),
                                            property::dispose);
        
        property.onDisposed(propertyStreamSubscription::dispose);
        
        return property;
    }
    
    /**
     * Creates a property that is bound to this stream.
     * 
     * @param initialValue
     *            some value to initialize the property with.
     * @return a new {@link Property}, that is bound to this stream and is
     *         initialized with the provided intialValue.
     * @throws IllegalStateException
     *             if called from a thread other than the thread that this event
     *             stream was created on.
     * TODO: perhaps this should return a PropertyStream instead?
     */
    public final Property<E> toProperty(E initialValue) {
        
        Property<E> property = Property.create(initialValue);
        
        Subscription subscription = property.bind(this);
        property.onDisposed(subscription::dispose);
        
        return property;
    }
    
    /**
     * Creates a new {@link Observable} backed by this event stream.
     * 
     * <p>
     * <b>Note:</b> The created observable can only be subscribed to from the same
     * thread as this stream was created on. Attempting to subscribe on a
     * different thread will throw an {@link IllegalStateException}.
     * 
     * @return an {@link Observable} backed by this event stream.
     */
    public final Observable<E> asObservable() {
        return Observable.create(subscriber -> {
            observe(EventObserver.create(subscriber::onNext, subscriber::onCompleted)); 
        });
    }
    
    /**
     * Creates an event stream that whose source of events is some observable.
     * 
     * @param observable
     *            an {@link Observable} to provide the source of events to the
     *            created event stream.
     * @return A new {@link EventStream} whose source of events is the provided
     *         observable.
     */
    public static final <E> EventStream<E> from(Observable<E> observable) {
        
        return new EventStream<>(eventObserver -> {

            EventSubscriber<E> subscriber = new EventSubscriber<>(eventObserver);

            rx.Subscriber<E> rxSubscriber = new Subscriber<E>() {
                @Override
                public void onCompleted() {
                    subscriber.onCompleted();
                }

                @Override
                public void onError(Throwable e) {
                    // TODO: What to do here, perhaps complete and send the error to the global error handler.
                }

                @Override
                public void onNext(E event) {
                    subscriber.onEvent(event);
                }
            };

            observable.subscribe(rxSubscriber);

            Subscription eventStreamSubscription = Subscription.create(rxSubscriber::unsubscribe);

            rxSubscriber.add(Subscriptions.create(eventStreamSubscription::dispose));

            return eventStreamSubscription;
        });
    }
    
    /**
     * Creates an event stream from the provided events. Each subscriber to the
     * returned event stream will be dispatched all the events and then be
     * completed.
     * 
     * @param events
     *            some events to create an event stream for.
     * @return a new {@link EventStream} that will dispatch all the events when
     *         subscribed to and then complete.
     */
    @SafeVarargs
    public static final <T> EventStream<T> fromArray(T... events) {
        return fromIterable(Arrays.asList(events));
    }

    /**
     * Creates an event stream from the provided iterable of events. Each subscriber to the
     * returned event stream will be dispatched all the events and then be
     * completed.
     * 
     * @param events
     *            some iterable of events to create an event stream for.
     * @return a new {@link EventStream} that will dispatch all the events when
     *         subscribed to and then complete.
     */
    public static <T> EventStream<T> fromIterable(Iterable<T> eventList) {
        return new EventStream<>(observer -> {
            EventSubscriber<T> subscriber = new EventSubscriber<>(observer);
            
            eventList.forEach(subscriber::onEvent);
            subscriber.onCompleted();
            
            return subscriber;
        });
    }

    /**
     * Create a new stream that results from merging all events emitted by the
     * provided streams.
     * 
     * @param eventStreams
     *            some event streams to merge, must contain at least one stream.
     * @return a new {@link EventStream} that will emit all the events emitted
     *         by the source event streams
     * @throws IllegalArgumentException
     *             if the provided array of streams is empty
     */
    @SafeVarargs
    public final static <E> EventStream<E> merge(EventStream<E>... eventStreams) {
        checkArgument(eventStreams.length > 0, "You must provide at least one stream to merge");
        
        if (eventStreams.length == 1)
            return eventStreams[0];
        
        return new EventStream<E>(new MergeEventPublisher<>(Arrays.asList(eventStreams)));
    }
    
    /**
     * Create a new stream that results from merging all events emitted by the
     * provided streams.
     * 
     * @param eventStreams
     *            some event streams to merge, must contain at least one stream.
     * @return a new {@link EventStream} that will emit all the events emitted
     *         by the source event streams
     * @throws IllegalArgumentException
     *             if the provided array of streams is empty
     */
    public final static <E> EventStream<E> merge(Iterable<EventStream<E>> eventStreams) {
        
        Collection<EventStream<E>> streamList;
        if ( eventStreams instanceof Collection ) {            
            streamList = (Collection<EventStream<E>>) eventStreams;
        }
        else {            
            streamList = StreamSupport.stream(eventStreams.spliterator(), false).collect(Collectors.toList());
        }
        
        
        checkArgument(streamList.size() > 0, "You must provide at least one stream to merge");
        
        if (streamList.size() == 1)
            return streamList.iterator().next();
        
        return new EventStream<>(new MergeEventPublisher<>(streamList));
    }
    
    /**
     * Removes one level of nesting from a stream of streams.
     * 
     * @param streamOfStreams
     *            a stream of streams to flatten
     * @return a new event stream with one level of nesting removed.
     */
    public final static <E> EventStream<E> flatten(EventStream<EventStream<E>> streamOfStreams) {
        return new EventStream<>(new FlattenPublisher<>(streamOfStreams));
    }
}
