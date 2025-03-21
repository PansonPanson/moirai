package top.panson.common.toolkit;

/**
 * Thread util.
 */
public class ThreadUtil {

    /**
     * New thread.
     *
     * @param runnable
     * @param name
     * @param isDaemon
     * @return {@link Thread}
     */
    public static Thread newThread(Runnable runnable, String name, boolean isDaemon) {
        Thread t = new Thread(null, runnable, name);
        t.setDaemon(isDaemon);
        return t;
    }

    /**
     * Suspend the current thread.
     *
     * @param millis
     * @return
     */
    public static boolean sleep(long millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return true;
    }
}

