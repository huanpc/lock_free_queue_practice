package com.huanpc.lock_free_queue;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nauh94@gmail.com on 2019-04-20
 */
public class UnsafeUtil {
    private static final  Logger logger = Logger.getLogger(UnsafeUtil.class.getName());

    public static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "cannot get unsafe", e);
            return null;
        }
    }

    public static long sizeOf(Object object) {
        Unsafe unsafe = getUnsafe();
        return unsafe.getAddress( normalize( unsafe.getInt(object, 4L) ) + 12L );
    }

    public static long normalize(int value) {
        if(value >= 0) return value;
        return (~0L >>> 32) & value;
    }

    public static void main(String[] args) {
        int [] a = new int[3];
        a[0] = 10; a[1] = 20; a[2] = 30;
        Unsafe unsafe = getUnsafe();
        if (unsafe != null) {
            int baseOffset = unsafe.arrayBaseOffset(a.getClass());
            int indexScale = unsafe.arrayIndexScale(a.getClass());
            logger.log(Level.INFO, "ArrayOffset: {0}", baseOffset);
            logger.log(Level.INFO, "ArrayIndexScale: {0}", indexScale);
        }
    }
}
