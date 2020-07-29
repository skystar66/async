package com.xuliang.framework.test.depend;

import com.xuliang.framework.async.excuter.Async;
import com.xuliang.framework.async.worker.WorkResult;
import com.xuliang.framework.async.wrapper.WorkerWrapper;

import java.util.concurrent.ExecutionException;


/**
 * 后面请求依赖于前面请求的执行结果
 *
 * @author xuliang
 * @version 1.0
 */
public class Test {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        DeWorker w = new DeWorker();
        DeWorker1 w1 = new DeWorker1();
        DeWorker2 w2 = new DeWorker2();
        WorkerWrapper<WorkResult<User>, String> workerWrapper2 = new WorkerWrapper.Builder<WorkResult<User>, String>()
                .worker(w2)
                .callback(w2)
                .id("third")
                .build();
        WorkerWrapper<WorkResult<User>, User> workerWrapper1 = new WorkerWrapper.Builder<WorkResult<User>, User>()
                .worker(w1)
                .callback(w1)
                .id("second")
                .next(workerWrapper2)
                .build();
        WorkerWrapper<String, User> workerWrapper = new WorkerWrapper.Builder<String, User>()
                .worker(w)
                .param("0")
                .id("first")
                .next(workerWrapper1, true)
                .callback(w)
                .build();

        /**讲worker0 的结果作为work1 的入参，将work1 的结果作为work2的入参*/
        WorkResult<User> result = workerWrapper.getWorkResult();
        WorkResult<User> result1 = workerWrapper1.getWorkResult();
        /**将work的结果传参给work1*/
        workerWrapper1.setParam(result);
        /**将work1的结果传参给work2*/
        workerWrapper2.setParam(result1);

        long now = System.currentTimeMillis();


        Async.beginWork(10 * 60 * 1000, workerWrapper);

        System.out.println("workerWrapper2 result :" + workerWrapper2.getWorkResult());

        System.out.println("cost time :" + (System.currentTimeMillis() - now) + "ms");
        Async.shutDown();
    }
}
