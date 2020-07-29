package com.xuliang.framework.test.depend;


import com.xuliang.framework.async.callback.ICallBack;
import com.xuliang.framework.async.callback.IWorker;
import com.xuliang.framework.async.worker.WorkResult;
import com.xuliang.framework.async.wrapper.WorkerWrapper;

import java.util.Map;
/**
 * @author xuliang
 */
public class DeWorker2 implements IWorker<WorkResult<User>, String>, ICallBack<WorkResult<User>, String> {

    @Override
    public String action(WorkResult<User> result, Map<String, WorkerWrapper> allWrappers) {
        System.out.println("work2的入参来自于work1： " + result.getResult());
        try {
            /**处理数据库 业务 rpc  等 其他业务操作*/
            System.out.println("work2 excute bussiness");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result.getResult().getName();
    }


    @Override
    public String defaultValue() {
        return "default";
    }

    @Override
    public void begin() {
        System.out.println(Thread.currentThread().getName() + "- start --" + System.currentTimeMillis());
    }

    @Override
    public void call(boolean success, WorkResult<User> param, WorkResult<String> workResult) {
        System.out.println("worker2 的结果是：" + workResult.getResult());
    }

}
