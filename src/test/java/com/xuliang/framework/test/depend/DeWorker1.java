package com.xuliang.framework.test.depend;


import com.xuliang.framework.async.callback.ICallBack;
import com.xuliang.framework.async.callback.IWorker;
import com.xuliang.framework.async.worker.WorkResult;
import com.xuliang.framework.async.wrapper.WorkerWrapper;

import java.util.Map;
/**
 * @author xuliang
 */
public class DeWorker1 implements IWorker<WorkResult<User>, User>, ICallBack<WorkResult<User>, User> {

    @Override
    public User action(WorkResult<User> result, Map<String, WorkerWrapper> allWrappers) {
        System.out.println("work1的入参来自于work0： " + result.getResult());
        try {
            /**处理数据库 业务 rpc  等 其他业务操作*/
            System.out.println("work1 excute bussiness");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new User("user1");
    }


    @Override
    public User defaultValue() {
        return new User("default User");
    }

    @Override
    public void begin() {
        System.out.println(Thread.currentThread().getName() + "- start --" + System.currentTimeMillis());
    }

    @Override
    public void call(boolean success, WorkResult<User> param, WorkResult<User> workResult) {
        System.out.println("worker1 的结果是：" + workResult.getResult());
    }

}
