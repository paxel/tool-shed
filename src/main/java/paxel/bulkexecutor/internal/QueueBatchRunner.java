package paxel.bulkexecutor.internal;

import java.util.Queue;

/**
 * The QueueBatchRunner takes a number of Runnables from a Qeue and runs them
 * sequentially.
 */
class QueueBatchRunner implements Runnable {

    private final Queue<Runnable> q;
    private final int batch;

    /**
     * Constructs a QueueRunner that takes 'batch' jobs from the Queue, if
     * available and runs them one after the other. In case a Runnable throws an
     * exception, the processing stops with that faulty runnable. The remaining
     * jobs stay in the queue.
     *
     * @param q the Queue.
     * @param batch the number of jobs to be processed in batch.
     */
    QueueBatchRunner(Queue<Runnable> q, int batch) {
        this.q = q;
        this.batch = batch;
    }

    @Override
    public void run() {
        for (int i = 0; i < batch; i++) {
            final Runnable poll = q.poll();
            if (poll != null) {
                poll.run();
            } else {
                break;
            }
        }
    }

}
