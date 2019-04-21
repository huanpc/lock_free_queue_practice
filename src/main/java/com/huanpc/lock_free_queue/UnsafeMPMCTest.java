package com.huanpc.lock_free_queue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nauh94@gmail.com on 2019-04-21
 */
public class UnsafeMPMCTest {

    private static final Logger LOGGER;

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    private UnsafeMPMCRingBufferQueue<Integer> queue = new UnsafeMPMCRingBufferQueue<Integer>(1024);


    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        LOGGER = Logger.getLogger(UnsafeUtil.class.getName());
    }

    void startConsumer() {
        for (final Integer id: new Integer[]{1, 2, 3}) {
            executor.submit(() -> {
                Object value = queue.poll();
                while (value != null) {
                    LOGGER.log(Level.INFO, "[Consumer id: {0}] - Consume message {1}", new Object[]{id, (Integer) value});
                    value = queue.poll();
                }
                LOGGER.log(Level.INFO, "[Consumer id: {0}] - Queue is empty, consumer is exiting", id);
            });
        }
    }

    boolean produce(Integer value) {
        LOGGER.log(Level.INFO, "Publish message {0}", value);
        return queue.add(value);
    }

    public static void main(String[] args) {
        UnsafeMPMCTest unsafeMPMCTest = new UnsafeMPMCTest();
        for (int i = 0; i < 1000; i ++) {
            if (!unsafeMPMCTest.produce(i)) break;
        }
        unsafeMPMCTest.startConsumer();
    }
}
