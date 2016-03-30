package tigerui.event.operator;

import static tigerui.Preconditions.checkArgument;

import tigerui.event.EventObserver;
import tigerui.event.EventSubscriber;

/**
 * An operator that limits the number of events a subscriber receives.
 * Subscribers will receive at most the number of events as provided in the
 * constructor.
 *
 * @param <M>
 *            The type of the events that the event stream emits.
 */
public class OperatorTake<M> implements Operator<M, M> {
	
	private final int takeTotal;
    
    public OperatorTake(int takeTotal) {
    	checkArgument(takeTotal > 0, "Must take at least one event.");
        this.takeTotal = takeTotal;
    }

	@Override
	public EventSubscriber<M> apply(EventSubscriber<M> childSubscriber) {
        return new TakeSubscriber<>(childSubscriber, takeTotal);
	}
	
    private static class TakeSubscriber<M> extends EventSubscriber<M> {

        private final int takeTotal;
        private int takeCount = 0;
        
        public TakeSubscriber(EventObserver<M> observer, int takeCount) {
            super(observer);
            this.takeTotal = takeCount;
        }
        
        @Override
        public void onEvent(M newValue) {
            super.onEvent(newValue);
            takeCount++;
            
            if (takeCount == takeTotal)
                onCompleted();
        }
    }
}
