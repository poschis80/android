package xpshome.net.util;

import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import xpshome.net.components.Unity;

/**
 * Created by Christian Poschinger on 11.08.2015.
 */
public class ThreadManager extends Unity.SingletonBase {
    @SuppressWarnings("unused")
    public static void registerToUnity() {
        Unity.Instance().register(ThreadManager.class);
    }

    public static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private boolean isInitialized = false;
    private ThreadPoolExecutor threadPoolExecutor;
    private BlockingDeque<Runnable> workQueue;
    private final Object syncPool = new Object();

    @Override
    public void init() {
        if (threadPoolExecutor == null) {
            workQueue = new LinkedBlockingDeque<>();
            threadPoolExecutor = new ThreadPoolExecutor(
                    NUMBER_OF_CORES,
                    NUMBER_OF_CORES,
                    KEEP_ALIVE_TIME,
                    KEEP_ALIVE_TIME_UNIT,
                    workQueue);
            isInitialized = true;
        }
    }

    @SuppressWarnings("unused")
    public boolean overrideThreadPoolSize(int newSize) {
        if (newSize >= 0) {
            threadPoolExecutor.setCorePoolSize(newSize);
            threadPoolExecutor.setMaximumPoolSize(newSize);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    public boolean isInitialized() {
        return isInitialized;
    }

    @SuppressWarnings("unused")
    public void execute(ExtRunnable runnable) {
        threadPoolExecutor.execute(runnable);
    }

    private ExtRunnable[] getRunnableArray() {
        //noinspection SuspiciousToArrayCall
        return workQueue.toArray(new ExtRunnable[workQueue.size()]);
    }

     @SuppressWarnings("unused")
    public void cancelAll() {
        ExtRunnable[] ra = getRunnableArray();
        int len = ra.length;
        synchronized (syncPool) {
            for (ExtRunnable r : ra) {
                Thread t = r.getThread();
                if (t != null) {
                    t.interrupt();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public void cancel(UUID runnableID) {
        ExtRunnable[] ra = getRunnableArray();
        int len = ra.length;
        synchronized (syncPool) {
            for (ExtRunnable r : ra) {
                UUID id = r.getID();
                if (id.compareTo(runnableID) == 0) {
                    Thread t = r.getThread();
                    if (t != null) {
                        t.interrupt();
                        return;
                    }
                }
            }
        }
    }
}
