package lucene.queue;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import lucene.IActionQueue;
import lucene.action.BaseAction;

import com.github.ddth.queue.IQueueMessage;

/**
 * In-memory implementation of {@link IActionQueue}.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class InMemoryActionQueue implements IActionQueue {

    private BlockingQueue<BaseAction> queue;
    private int maxItems = 1024;

    public InMemoryActionQueue setMaxItems(int maxItems) {
        this.maxItems = maxItems;
        return this;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public InMemoryActionQueue init() {
        queue = new LinkedBlockingQueue<BaseAction>(maxItems > 0 ? maxItems : 1024);
        return this;
    }

    public void destroy() {
        // EMPTY
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean queue(IQueueMessage msg) {
        if (!(msg instanceof BaseAction)) {
            return false;
        }
        return queue((BaseAction) msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean queue(BaseAction msg) {
        if (msg != null) {
            try {
                return queue.offer(msg, 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requeue(IQueueMessage msg) {
        if (!(msg instanceof BaseAction)) {
            return false;
        }
        return requeue((BaseAction) msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requeue(BaseAction msg) {
        if (msg != null) {
            try {
                return queue.offer(msg, 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requeueSilent(IQueueMessage msg) {
        if (!(msg instanceof BaseAction)) {
            return false;
        }
        return requeueSilent((BaseAction) msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requeueSilent(BaseAction msg) {
        if (msg != null) {
            try {
                return queue.offer(msg, 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish(IQueueMessage msg) {
        // EMPTY
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IQueueMessage> getOrphanMessages(long thresholdTimestampMs) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean moveFromEphemeralToQueueStorage(IQueueMessage msg) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int queueSize() {
        return queue.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int ephemeralSize() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseAction take() {
        return queue.poll();
    }

}
