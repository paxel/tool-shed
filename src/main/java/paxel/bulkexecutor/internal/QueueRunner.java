package paxel.bulkexecutor.internal;

import java.util.Queue;

/**
 * The QueueRunner takes a number of Runnables from a Qeue and runs them
 * sequentially.
 */
public class QueueRunner implements Runnable {

    private final Queue<Runnable> q;
    private final int bulk;

    /**
     * Constructs a QueueRunner that takes 'bulk' jobs from the Queue, if
     * available and runs them one after the other. In case a runnable throws an
     * exception, the processing stops with that faulty runnable. the remaining
     * jobs stay in the queue.
     *
     * @param q the Queue.
     * @param bulk the number of jobs to be processed in bulk.
     */
    public QueueRunner(Queue<Runnable> q, int bulk) {
        this.q = q;
        this.bulk = bulk;
    }

    @Override
    public void run() {
        for (int i = 0; i < bulk; i++) {
            final Runnable poll = q.poll();
            if (poll != null) {
                poll.run();
            } else {
                break;
            }
        }
    }

}
