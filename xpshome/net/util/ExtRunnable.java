package xpshome.net.util;

import java.util.UUID;

/**
 * Created by Christian Poschinger on 11.08.2015.
 */
public abstract class ExtRunnable implements Runnable {
    private Thread thread;
    private UUID id;
    private Callback finishedCallback;

    public ExtRunnable() {
        id = UUID.randomUUID();
        thread = null;
        finishedCallback = null;
    }
    public final Thread getThread() {
        return thread;
    }
    public final UUID getID() {
        return id;
    }
    public void setFinishedCallback(Callback callback) {
        this.finishedCallback = callback;
    }

    @Override
    public void run() {
        thread = Thread.currentThread();
        task();
        if (finishedCallback != null) {
            finishedCallback.call(null);
        }
    }

    public abstract void task();
}
