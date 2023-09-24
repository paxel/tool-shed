package paxel.bulkexecutor.internal;

import java.util.Objects;
import java.util.Queue;

/**
 * The QueueBatchRunner takes a number of Runnables from a Queue and runs them
 * sequentially.
 */
class QueueBatchRunner implements Runnable {

    private final Queue<Runnable> q;
    private final int batch;

    private final Runnable queuePop;

    /**
     * Constructs a QueueRunner that takes 'batch' jobs from the Queue, if
     * available and runs them one after the other. In case a Runnable throws an
     * exception, the processing stops with that faulty runnable. The remaining
     * jobs stay in the queue.
     *
     * @param q        the Queue.
     * @param batch    the number of jobs to be processed in batch.
     * @param queuePop The runnable that is called when an entry is popped from the queue; before it is actually processed.
     */
    QueueBatchRunner(Queue<Runnable> q, int batch, Runnable queuePop) {
        this.q = Objects.requireNonNull(q, "Argument q can not be null");
        this.batch = batch;
        this.queuePop = Objects.requireNonNull(queuePop, "Argument queuePop can not be null");
    }

    @Override
    public void run() {
        for (int i = 0; i < batch; i++) {
            final Runnable poll = q.poll();
            if (poll != null) {
                queuePop.run();
                poll.run();
            } else {
                break;
            }
        }
    }

}
