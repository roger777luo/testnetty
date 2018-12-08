package com.roger;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;

import java.util.concurrent.TimeUnit;

public class TestFastThreadLocal {
    private static void testThreadLocal(int times) throws InterruptedException {
        final int threadLocalCount = 1000;
        final int threadLcoalReadTimes = times;
        final ThreadLocal<String>[] caches = new ThreadLocal[threadLocalCount];
        final Thread mainThread = Thread.currentThread();
        for (int i=0;i<threadLocalCount;i++) {
            caches[i] = new ThreadLocal();
        }
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (int i=0;i<threadLocalCount;i++) {
                    caches[i].set("float.lu");
                }
                long start = System.nanoTime();
                for (int i=0;i<threadLocalCount;i++) {
                    for (int j=0;j<threadLcoalReadTimes;j++) {
                        caches[i].get();
                    }
                }
                long end = System.nanoTime();
                System.out.println("ThreadLocal read " + threadLcoalReadTimes + " times take[" + TimeUnit.NANOSECONDS.toMillis(end - start) +
                        "]ms");
            }

        });
        t.start();
        t.join();
    }

    private static void testFastThreadLocal(int times) throws InterruptedException {
        final int threadLocalCount = 1000;
        final int threadLcoalReadTimes = times;
        final FastThreadLocal<String>[] caches = new FastThreadLocal[threadLocalCount];
        final Thread mainThread = Thread.currentThread();
        for (int i=0;i<threadLocalCount;i++) {
            caches[i] = new FastThreadLocal();
        }
        Thread t = new FastThreadLocalThread(new Runnable() {
            public void run() {
                for (int i=0;i<threadLocalCount;i++) {
                    caches[i].set("float.lu");
                }
                long start = System.nanoTime();
                for (int i=0;i<threadLocalCount;i++) {
                    for (int j=0;j<threadLcoalReadTimes;j++) {
                        caches[i].get();
                    }
                }
                long end = System.nanoTime();
                System.out.println("FastThreadLocal read " + threadLcoalReadTimes + " times take[" + TimeUnit.NANOSECONDS.toMillis(end - start) +
                        "]ms");
            }

        });
        t.start();
        t.join();
    }

    public static void main(String ...s) throws InterruptedException {
        int times = 10000;
        for (; times <= 1000000; times *= 10) {
            System.out.println("===== " + times + " =====");
            testThreadLocal(times);
            testFastThreadLocal(times);
            Runtime.getRuntime().gc();
        }
    }
}
