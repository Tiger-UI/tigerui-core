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
package mb.rxui.property.publisher;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import mb.rxui.property.PropertyStream;
import mb.rxui.property.PropertyObserver;
import mb.rxui.property.PropertySubscriber;
import mb.rxui.subscription.CompositeSubscription;
import mb.rxui.subscription.Subscription;

/**
 * A {@link PropertyPublisher} that will combine the values of the provided
 * property streams any time the value of any of them change.
 *
 * @param <R>
 *            the type of the combined result
 */
public final class CombinePropertyPublisher<R> implements PropertyPublisher<R> {

    private final List<PropertyStream<?>> streams;
    private Supplier<R> combineSupplier;
    private int disposeCount = 0;

    public CombinePropertyPublisher(Supplier<R> combineSupplier, List<PropertyStream<?>> streams) {
        this.combineSupplier = requireNonNull(combineSupplier);
        this.streams = requireNonNull(streams);
    }

    /**
     * @return true if the disposeCount equals the number of streams that
     *         have been subscribed to.
     */
    private boolean incrementDisposeCount() {
        disposeCount++;
        return disposeCount == streams.size();
    }

    @Override
    public R get() {
        return combineSupplier.get();
    }

    @Override
    public Subscription subscribe(PropertyObserver<R> observer) {

        PropertySubscriber<R> combineSubscriber = new PropertySubscriber<>(observer);

        List<Subscription> subscriptions =
            streams.stream()
                       .map(subscribe(combineSubscriber))
                       .collect(Collectors.toList());

        CompositeSubscription subscription = new CompositeSubscription(subscriptions);

        combineSubscriber.doOnDispose(subscription::dispose);

        return combineSubscriber;
    }

    private Function<? super PropertyStream<?>, ? extends Subscription> subscribe(PropertySubscriber<R> combineSubscriber) {

        return stream -> stream.observe(value -> combineSubscriber.onChanged(get()), 
                                        () -> {
                                            if (incrementDisposeCount())
                                                combineSubscriber.onDisposed();
                                        });
    }
}
