package com.xuliang.framework.test.parallel;

import com.xuliang.framework.async.excuter.Async;
import com.xuliang.framework.async.excuter.timer.SystemClock;
import com.xuliang.framework.async.wrapper.WorkerWrapper;

import java.util.concurrent.ExecutionException;

public class TestPar {


    public static void main(String[] args) throws ExecutionException, InterruptedException {

//        testNormal();
//        testMulti();
//        testMultiReverse();
//        testMultiError();
//        testMulti3();
//        testMulti3Reverse();
//        testMulti4();
//        testMulti4Reverse();
//        testMulti5();
//        testMulti5Reverse();
//        testMulti6();
        testMulti7();

//        testMulti8();

    }

    /**
     * 3个并行，测试不同时间的超时
     */
    private static void testNormal() throws InterruptedException, ExecutionException {

        ParWorker parWorker = new ParWorker();
        ParWorker1 parWorker1 = new ParWorker1();
        ParWorker2 parWorker2 = new ParWorker2();

        WorkerWrapper<String, String> wrapper = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker)
                .worker(parWorker)
                .param("0")
                .build();

        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker1)
                .worker(parWorker1)
                .param("1")
                .build();


        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker2)
                .worker(parWorker2)
                .param("2")
                .build();

        long now = SystemClock.now();

        Async.beginWork(2000, wrapper, wrapper1, wrapper2);

        System.err.println("cost time " + (SystemClock.now() - now) + " ms");

        Async.shutDown();

    }

    /**
     * 0,2同时开启,1在0后面
     * 0---1
     * 2
     */
    private static void testMulti() throws ExecutionException, InterruptedException {
        ParWorker parWorker0 = new ParWorker();
        ParWorker1 parWorker1 = new ParWorker1();
        ParWorker2 parWorker2 = new ParWorker2();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker1)
                .worker(parWorker1)
                .param("1")
                .build();


        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker2)
                .worker(parWorker2)
                .param("2")
                .build();

        WorkerWrapper<String, String> wrapper0 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker0)
                .worker(parWorker0)
                .param("0")
                .next(wrapper1)
                .build();


        long now = SystemClock.now();
        Async.beginWork(10 * 60 * 1000, wrapper0, wrapper2);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();
    }

    /**
     * 0,2同时开启,1在0后面
     * 0---1
     * 2
     */
    private static void testMultiReverse() throws ExecutionException, InterruptedException {
        ParWorker parWorker0 = new ParWorker();
        ParWorker1 parWorker1 = new ParWorker1();
        ParWorker2 parWorker2 = new ParWorker2();


        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker2)
                .worker(parWorker2)
                .param("2")
                .build();

        WorkerWrapper<String, String> wrapper0 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker0)
                .worker(parWorker0)
                .param("0")
                .build();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker1)
                .worker(parWorker1)
                .param("1")
                .depend(wrapper0)
                .build();

        long now = SystemClock.now();
        Async.beginWork(10 * 60 * 1000, wrapper0, wrapper2);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();


    }

    /**
     * 0,2同时开启,1在0后面. 组超时,则0和2成功,1失败
     * 0---1
     * 2
     */
    private static void testMultiError() throws ExecutionException, InterruptedException {
        ParWorker parWorker0 = new ParWorker();
        ParWorker1 parWorker1 = new ParWorker1();
        ParWorker2 parWorker2 = new ParWorker2();


        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker2)
                .worker(parWorker2)
                .name("worker2")
                .param("2")
                .build();

        WorkerWrapper<String, String> wrapper0 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker0)
                .worker(parWorker0)
                .name("worker0")
                .param("0")
                .build();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker1)
                .worker(parWorker1)
                .param("1")
                .name("worker1")
                .depend(wrapper0)
                .build();
        long now = SystemClock.now();
        Async.beginWork(1500, wrapper0, wrapper2);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();
    }

    /**
     * 0执行完,同时1和2, 1\2都完成后3
     *      1
     * 0        3
     *      2
     */
    private static void testMulti3() throws ExecutionException, InterruptedException {
        ParWorker parWorker0 = new ParWorker();
        ParWorker1 parWorker1 = new ParWorker1();
        ParWorker2 parWorker2 = new ParWorker2();
        ParWorker3 parWorker3 = new ParWorker3();


        WorkerWrapper<String, String> wrapper3 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker3)
                .worker(parWorker3)
                .name("worker3")
                .param("3")
                .build();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker1)
                .worker(parWorker1)
                .next(wrapper3)
                .name("worker1")
                .param("1")
                .build();


        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker2)
                .worker(parWorker2)
                .next(wrapper3)
                .name("worker2")
                .param("2")
                .build();

        WorkerWrapper<String, String> wrapper0 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker0)
                .worker(parWorker0)
                .param("0")
                .name("worker0")
                .next(wrapper1, wrapper2)
                .build();

        long now = SystemClock.now();
        Async.beginWork(10 * 60 * 1000, wrapper0);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();

    }


    /**
     * 0执行完,同时1和2, 1\2都完成后3
     *      1
     * 0        3
     *      2
     */
    private static void testMulti3Reverse() throws ExecutionException, InterruptedException {
        ParWorker parWorker0 = new ParWorker();
        ParWorker1 parWorker1 = new ParWorker1();
        ParWorker2 parWorker2 = new ParWorker2();
        ParWorker3 parWorker3 = new ParWorker3();


        WorkerWrapper<String, String> wrapper0 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker0)
                .worker(parWorker0)
                .name("worker0")
                .param("0")
                .build();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker1)
                .worker(parWorker1)
                .param("1")
                .name("worker1")
                .depend(wrapper0)
                .build();


        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker2)
                .worker(parWorker2)
                .param("2")
                .name("worker2")
                .depend(wrapper0)
                .build();


        WorkerWrapper<String, String> wrapper3 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker3)
                .worker(parWorker3)
                .param("3")
                .name("worker3")
                .depend(wrapper1, wrapper2)
                .build();

        long now = SystemClock.now();
        Async.beginWork(10 * 60 * 1000, wrapper0);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();
    }


    /**
     * 0执行完,同时1和2, 1\2都完成后3，2耗时2秒，1耗时1秒。3会等待2完成
     *      1
     * 0        3
     *      2
     * <p>
     * 执行结果0，1，2，3
     */
    private static void testMulti4() throws ExecutionException, InterruptedException {
        ParWorker parWorker0 = new ParWorker();
        ParWorker1 parWorker1 = new ParWorker1();

        ParWorker2 parWorker2 = new ParWorker2();
        parWorker2.setSleepTime(2000);

        ParWorker3 parWorker3 = new ParWorker3();

        WorkerWrapper<String, String> wrapper3 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker3)
                .worker(parWorker3)
                .param("3")
                .build();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker1)
                .worker(parWorker1)
                .param("1")
                .next(wrapper3)
                .build();

        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker2)
                .worker(parWorker2)
                .param("2")
                .next(wrapper3)
                .build();


        WorkerWrapper<String, String> wrapper = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker0)
                .worker(parWorker0)
                .param("0")
                .next(wrapper1, wrapper2)
                .build();


        long now = SystemClock.now();
        Async.beginWork(4500, wrapper);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();
    }


    /**
     * 0执行完,同时1和2, 1\2都完成后3，2耗时2秒，1耗时1秒。3会等待2完成
     *      1
     * 0         3
     *      2
     * <p>
     * 执行结果0，1，2，3
     */
    private static void testMulti4Reverse() throws ExecutionException, InterruptedException {

        ParWorker parWorker0 = new ParWorker();
        ParWorker1 parWorker1 = new ParWorker1();

        ParWorker2 parWorker2 = new ParWorker2();
        parWorker2.setSleepTime(2000);

        ParWorker3 parWorker3 = new ParWorker3();


        WorkerWrapper<String, String> wrapper = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker0)
                .worker(parWorker0)
                .param("0")
                .build();


        WorkerWrapper<String, String> wrapper3 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker3)
                .worker(parWorker3)
                .param("3")
                .build();

        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker1)
                .worker(parWorker1)
                .param("1")
                .depend(wrapper)
                .next(wrapper3)
                .build();


        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker2)
                .worker(parWorker2)
                .param("2")
                .depend(wrapper)
                .next(wrapper3)
                .build();


        long now = SystemClock.now();
        Async.beginWork(4500, wrapper);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();

    }

    /**
     * 0执行完,同时1和2, 1\2 任何一个执行完后，都执行3
     *      1
     * 0            3
     *      2
     * <p>
     * 则结果是：
     * 0，2，3，1
     * 2，3分别是500、400.3执行完毕后，1才执行完
     */

    private static void testMulti5() throws ExecutionException, InterruptedException {
        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();

        ParWorker2 w2 = new ParWorker2();
        w2.setSleepTime(500);

        ParWorker3 w3 = new ParWorker3();
        w3.setSleepTime(400);

        WorkerWrapper<String, String> wrapper3 = new WorkerWrapper.Builder<String, String>()
                .callback(w3)
                .worker(w3)
                .param("3")
                .name("worker3")
                .build();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(w1)
                .worker(w1)
                .next(wrapper3, false)
                .name("worker1")
                .param("1")
                .build();

        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(w2)
                .worker(w2)
                .param("2")
                .name("worker2")
                .next(wrapper3, false)
                .build();


        WorkerWrapper<String, String> wrapper = new WorkerWrapper.Builder<String, String>()
                .callback(w)
                .worker(w)
                .name("worker0")
                .param("0")
                .next(wrapper1, wrapper2)
                .build();

        long now = SystemClock.now();
        Async.beginWork(4500, wrapper);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();


    }


    /**
     * 0执行完,同时1和2, 1\2 任何一个执行完后，都执行3
     *      1
     * 0        3
     *      2
     * <p>
     * 则结果是：
     * 0，2，3，1
     * 2，3分别是500、400.3执行完毕后，1才执行完
     */
    private static void testMulti5Reverse() throws ExecutionException, InterruptedException {

        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();

        ParWorker2 w2 = new ParWorker2();
        w2.setSleepTime(500);

        ParWorker3 w3 = new ParWorker3();
        w3.setSleepTime(400);


        WorkerWrapper<String, String> wrapper = new WorkerWrapper.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .build();

        WorkerWrapper<String, String> wrapper3 = new WorkerWrapper.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .param("0")
                .build();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .depend(wrapper)
                .next(wrapper3)
                .param("0")
                .build();

        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .depend(wrapper)
                .next(wrapper3)
                .param("0")
                .build();


        long now = SystemClock.now();
        Async.beginWork(4500, wrapper);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();

    }


    /**
     * 0执行完,同时1和2, 必须1执行完毕后，才能执行3. 无论2是否领先1完毕，都要等1
     *      1
     * 0        3
     *      2
     * <p>
     * 则结果是：
     * 0，2，1，3
     * <p>
     * 2，3分别是500、400.2执行完了，1没完，那就等着1完毕，才能3
     */
    private static void testMulti6() throws ExecutionException, InterruptedException {

        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();

        ParWorker2 w2 = new ParWorker2();
        w2.setSleepTime(500);

        ParWorker3 w3 = new ParWorker3();
        w3.setSleepTime(400);


        WorkerWrapper<String, String> wrapper3 = new WorkerWrapper.Builder<String, String>()
                .callback(w3)
                .worker(w3)
                .name("worker3")
                .param("2")
                .build();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(w1)
                .worker(w1)
                .param("1")
                .name("worker1")
                .next(wrapper3)
                .build();

        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(w2)
                .worker(w2)
                .name("worker2")
                .param("2")
                .build();


        WorkerWrapper<String, String> wrapper = new WorkerWrapper.Builder<String, String>()
                .callback(w)
                .worker(w)
                .param("0")
                .name("worker0")
                .next(wrapper1, wrapper2)
                .build();

        long now = SystemClock.now();
        Async.beginWork(4500, wrapper);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();


    }


    /**
     * 两个0并行，上面0执行完,同时1和2, 下面0执行完开始1，上面的 必须1、2执行完毕后，才能执行3. 最后必须2、3都完成，才能4
     *      1
     * 0            3
     *      2            4
     * ---------
     * 0   1        2
     * <p>
     * 则结果是：
     * callback worker0 success--1577242870969----result = 1577242870968---param = 00 from 0-threadName:Thread-1
     * callback worker0 success--1577242870969----result = 1577242870968---param = 0 from 0-threadName:Thread-0
     * callback worker1 success--1577242871972----result = 1577242871972---param = 11 from 1-threadName:Thread-1
     * callback worker1 success--1577242871972----result = 1577242871972---param = 1 from 1-threadName:Thread-2
     * callback worker2 success--1577242871973----result = 1577242871973---param = 2 from 2-threadName:Thread-3
     * callback worker2 success--1577242872975----result = 1577242872975---param = 22 from 2-threadName:Thread-1
     * callback worker3 success--1577242872977----result = 1577242872977---param = 3 from 3-threadName:Thread-2
     * callback worker4 success--1577242873980----result = 1577242873980---param = 4 from 3-threadName:Thread-2
     */
    private static void testMulti7() throws ExecutionException, InterruptedException {


        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();
        ParWorker3 w3 = new ParWorker3();
        ParWorker4 w4 = new ParWorker4();

        WorkerWrapper<String, String> wrapper4 = new WorkerWrapper.Builder<String, String>()
                .worker(w4)
                .callback(w4)
                .param("4")
                .name("worker4")
                .build();


        WorkerWrapper<String, String> wrapper3 = new WorkerWrapper.Builder<String, String>()
                .worker(w3)
                .callback(w3)
                .next(wrapper4)
                .name("worker3")
                .param("3")
                .build();

        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .param("1")
                .next(wrapper3)
                .name("worker1")
                .build();

        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .param("2")
                .name("worker2")
                .next(wrapper3)
                .build();


        WorkerWrapper<String, String> wrapper = new WorkerWrapper.Builder<String, String>()
                .worker(w)
                .callback(w)
                .param("0")
                .name("worker0")
                .next(wrapper1, wrapper2)
                .build();


        WorkerWrapper<String, String> wrapper02 = new WorkerWrapper.Builder<String, String>()
                .worker(w2)
                .callback(w2)
                .name("worker02")
                .param("02")
                .next(wrapper4)
                .build();

        WorkerWrapper<String, String> wrapper01 = new WorkerWrapper.Builder<String, String>()
                .worker(w1)
                .callback(w1)
                .name("worker01")
                .param("01")
                .next(wrapper02)
                .build();


        WorkerWrapper<String, String> wrapper00 = new WorkerWrapper.Builder<String, String>()
                .worker(w)
                .callback(w)
                .name("worker00")
                .param("00")
                .next(wrapper01)
                .build();
        long now = SystemClock.now();
        Async.beginWork(4500, wrapper, wrapper00);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();
    }

    /**
     * a1 -> b -> c
     * a2 -> b -> c
     * <p>
     * b、c
     */
    private static void testMulti8() throws ExecutionException, InterruptedException {

        ParWorker w = new ParWorker();
        ParWorker1 w1 = new ParWorker1();
        ParWorker2 w2 = new ParWorker2();

        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(w2)
                .worker(w2)
                .param("2")
                .build();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(w1)
                .worker(w1)
                .param("1")
                .next(wrapper2)
                .build();

        WorkerWrapper<String, String> wrapper = new WorkerWrapper.Builder<String, String>()
                .callback(w)
                .worker(w)
                .param("0")
                .next(wrapper1)
                .build();

        WorkerWrapper<String, String> wrapper22 = new WorkerWrapper.Builder<String, String>()
                .callback(w2)
                .worker(w2)
                .param("2")
                .build();


        WorkerWrapper<String, String> wrapper11 = new WorkerWrapper.Builder<String, String>()
                .callback(w1)
                .worker(w1)
                .param("1")
                .next(wrapper22)
                .build();

        WorkerWrapper<String, String> wrapper00 = new WorkerWrapper.Builder<String, String>()
                .callback(w)
                .worker(w)
                .param("0")
                .next(wrapper11)
                .build();

        long now = SystemClock.now();
        Async.beginWork(3100, wrapper, wrapper00);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();
    }


}
