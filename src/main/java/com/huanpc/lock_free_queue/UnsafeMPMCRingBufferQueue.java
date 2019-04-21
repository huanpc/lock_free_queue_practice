package com.huanpc.lock_free_queue;

import sun.misc.Unsafe;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Support multiple producer & multiple consumer with direct memory access (no GC involved)
 * @param <T>
 */
public class UnsafeMPMCRingBufferQueue<T> implements LockFreeQueue<T> {

    private int slotMask;

    private Object[] arrayData;

    private AtomicBoolean lock = new AtomicBoolean(false);

    private volatile int start;

    private volatile int end;

    private static final long START_OFFSET;

    private static final long END_OFFSET;

    private static final int ARRAY_OFFSET;

    private static final int ARRAY_INDEX_SCALE;

    static {
        try {
            Unsafe unsafe = UnsafeUtil.getUnsafe();
            START_OFFSET = unsafe.objectFieldOffset(UnsafeMPMCRingBufferQueue.class.getDeclaredField("start"));
            END_OFFSET = unsafe.objectFieldOffset(UnsafeMPMCRingBufferQueue.class.getDeclaredField("end"));
            ARRAY_OFFSET = unsafe.arrayBaseOffset(Object[].class);
            ARRAY_INDEX_SCALE = unsafe.arrayIndexScale(Object[].class);
        } catch (NoSuchFieldException | NullPointerException e) {
            throw new AssertionError(e);
        }
    }

    public UnsafeMPMCRingBufferQueue(int queueSize) {
        if (!isPowerOf2(queueSize)) {
            throw new IllegalArgumentException("Maximum size must be power of 2");
        }
        arrayData = new Object[queueSize];
        start = end = 0;
        slotMask = queueSize - 1;
    }

    private boolean isPowerOf2(int queueSize) {
        return (queueSize & (queueSize - 1)) == 0;
    }

    @Override
    public boolean add(T item) {
        while (!lock.compareAndSet(false, true)) {
            /*waiting*/
        }
        try {
            int nextEnd = (end + ARRAY_INDEX_SCALE) & slotMask; // circular increment
            if (nextEnd == start) {
                return false; // queue is full
            }
            UnsafeUtil.getUnsafe().putObject(arrayData, ARRAY_OFFSET + end, item);
            UnsafeUtil.getUnsafe().putOrderedInt(this, END_OFFSET, nextEnd);
        } finally {
            lock.set(false);
        }
        return true;
    }

    @Override
    public T poll() {
        while (!lock.compareAndSet(false, true)) {
            /*waiting*/
        }
        try {
            if (isEmpty()) return null;
            @SuppressWarnings("unchecked")
            T result = (T) UnsafeUtil.getUnsafe().getObject(arrayData, ARRAY_OFFSET + start);
            UnsafeUtil.getUnsafe().putOrderedInt(this, START_OFFSET, (start + ARRAY_INDEX_SCALE) & slotMask);
            return result;
        } finally {
            lock.set(false);
        }
    }

    @Override
    public boolean isEmpty() {
        return start == end;
    }
}