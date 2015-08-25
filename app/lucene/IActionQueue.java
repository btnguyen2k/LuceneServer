package lucene;

import lucene.action.BaseAction;

import com.github.ddth.queue.IQueue;

/**
 * Queue to buffer index's actions for async-executions.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public interface IActionQueue extends IQueue {
    public BaseAction take();

    public boolean queue(BaseAction msg);

    public boolean requeue(BaseAction msg);

    public boolean requeueSilent(BaseAction msg);
}
