package com.huanpc.lock_free_queue;

/**
 * Created by nauh94@gmail.com on 2019-04-21
 * Best case without concurrent handling
 */
public class SPSCRingBufferQueue<T> implements LockFreeQueue<T> {

    private int slotMask;

    private Object[] arrayData;

    private volatile int start;

    private volatile int end;

    public SPSCRingBufferQueue(int queueSize) {
        if (!isPowerOf2(queueSize))
            throw new IllegalArgumentException("Maximum size must be power of 2");
        arrayData = new Object[queueSize];
        start = end = 0;
        slotMask = queueSize - 1;
    }

    private boolean isPowerOf2(int queueSize) {
        return (queueSize & (queueSize - 1)) == 0;
    }

    @Override
    public boolean add(T item) {
        int nextEnd = (end + 1) & slotMask;
        if (nextEnd == start) return false;
        arrayData[end] = item;
        end = nextEnd;
        return true;
    }

    @Override
    public T poll() {
        if (isEmpty()) return null;
        @SuppressWarnings("unchecked")
        T data = (T) arrayData[start];
        start = (start + 1) & slotMask;
        return data;
    }

    @Override
    public boolean isEmpty() {
        return start == end;
    }
}
