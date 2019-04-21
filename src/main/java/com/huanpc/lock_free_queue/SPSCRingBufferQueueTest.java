package com.huanpc.lock_free_queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nauh94@gmail.com on 2019-04-21
 */
public class SPSCRingBufferQueueTest {

    private static final Logger LOGGER;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private SPSCRingBufferQueue<Integer> queue = new SPSCRingBufferQueue<Integer>(1024);

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        LOGGER = Logger.getLogger(UnsafeUtil.class.getName());
    }

    void startConsumer() {
        executor.submit(() -> {
            Object value = queue.poll();
            while (value != null) {
                LOGGER.log(Level.INFO, "[Consumer] - Consume message {0}", value);
                value = queue.poll();
            }
            LOGGER.log(Level.INFO, "[Consumer] - Queue is empty, consumer is exiting");
        });
    }

    boolean produce(Integer value) {
        LOGGER.log(Level.INFO, "Publish message {0}", value);
        return queue.add(value);
    }

    public static void main(String[] args) {
        SPSCRingBufferQueueTest spscTest = new SPSCRingBufferQueueTest();
        for (int i = 0; i < 1000; i ++) {
            if (!spscTest.produce(i)) break;
        }
        spscTest.startConsumer();
    }
}
