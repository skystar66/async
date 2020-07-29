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
            testMulti5();
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
        Async.beginWork(1500, wrapper0, wrapper2);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();
    }

    /**
     * 0执行完,同时1和2, 1\2都完成后3
     * 1
     * 0        3
     * 2
     */
    private static void testMulti3() throws ExecutionException, InterruptedException {
        ParWorker parWorker0 = new ParWorker();
        ParWorker1 parWorker1 = new ParWorker1();
        ParWorker2 parWorker2 = new ParWorker2();
        ParWorker3 parWorker3 = new ParWorker3();


        WorkerWrapper<String, String> wrapper3 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker3)
                .worker(parWorker3)
                .param("3")
                .build();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker1)
                .worker(parWorker1)
                .next(wrapper3)
                .param("1")
                .build();


        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker2)
                .worker(parWorker2)
                .next(wrapper3)
                .param("2")
                .build();

        WorkerWrapper<String, String> wrapper0 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker0)
                .worker(parWorker0)
                .param("0")
                .next(wrapper1, wrapper2)
                .build();

        long now = SystemClock.now();
        Async.beginWork(10 * 60 * 1000, wrapper0);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();

    }


    /**
     * 0执行完,同时1和2, 1\2都完成后3
     * 1
     * 0        3
     * 2
     */
    private static void testMulti3Reverse() throws ExecutionException, InterruptedException {
        ParWorker parWorker0 = new ParWorker();
        ParWorker1 parWorker1 = new ParWorker1();
        ParWorker2 parWorker2 = new ParWorker2();
        ParWorker3 parWorker3 = new ParWorker3();


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


        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker2)
                .worker(parWorker2)
                .param("2")
                .depend(wrapper0)
                .build();


        WorkerWrapper<String, String> wrapper3 = new WorkerWrapper.Builder<String, String>()
                .callback(parWorker3)
                .worker(parWorker3)
                .param("3")
                .depend(wrapper1, wrapper2)
                .build();

        long now = SystemClock.now();
        Async.beginWork(10 * 60 * 1000, wrapper0);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();
    }


    /**
     * 0执行完,同时1和2, 1\2都完成后3，2耗时2秒，1耗时1秒。3会等待2完成
     * 1
     * 0        3
     * 2
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
     * 1
     * 0         3
     * 2
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
                .build();


        WorkerWrapper<String, String> wrapper1 = new WorkerWrapper.Builder<String, String>()
                .callback(w1)
                .worker(w1)
                .next(wrapper3,false)
                .param("1")
                .build();

        WorkerWrapper<String, String> wrapper2 = new WorkerWrapper.Builder<String, String>()
                .callback(w2)
                .worker(w2)
                .param("2")
                .next(wrapper3,false)
                .build();


        WorkerWrapper<String, String> wrapper = new WorkerWrapper.Builder<String, String>()
                .callback(w)
                .worker(w)
                .param("0")
                .next(wrapper1,wrapper2)
                .build();

        long now = SystemClock.now();
        Async.beginWork(4500, wrapper);
        System.out.println("cost time : " + (SystemClock.now() - now) + "ms");
        Async.shutDown();


    }


}
