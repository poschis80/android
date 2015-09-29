package xpshome.net.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Christian Poschinger on 02.07.2015.
 */
public class LimitedSizeQueue<T> extends ArrayList<T> {
    private long maxSize;

    public long getMaxSize() { return maxSize; }

    public LimitedSizeQueue(long maxSize) {
        this.maxSize = maxSize;
    }

    public T getLast() {
        if (isEmpty()) {
            return null;
        }

        return get(size()-1);
    }

    /**
     * Add a new element and remove the oldest one if maxSize reached
     * @param object object to add
     * @return boolean true if no errors occured
     */
    @Override
    public boolean add(T object) {
        if (super.size() >= maxSize) {
            this.remove(0);
        }
        return super.add(object);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        // TODO : implement to avoid oversized
        return super.addAll(collection);
    }
}
