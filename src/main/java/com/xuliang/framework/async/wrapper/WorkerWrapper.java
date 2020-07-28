package com.xuliang.framework.async.wrapper;


import com.xuliang.framework.async.callback.DefaultCallback;
import com.xuliang.framework.async.callback.ICallBack;
import com.xuliang.framework.async.callback.IWorker;
import com.xuliang.framework.async.depend.DependWrapper;
import com.xuliang.framework.async.excuter.timer.SystemClock;
import com.xuliang.framework.async.worker.ResultState;
import com.xuliang.framework.async.worker.WorkResult;

import javax.print.attribute.standard.NumberUp;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对worker进行封装 处理
 *
 * @author xuliang
 */
public class WorkerWrapper<T, V> {

    /**
     * 该wrapper 的唯一标识
     */
    private String id;

    /**
     * worker处理的参数
     */

    private T param;

    /**
     * 具体处理的worker
     */
    private IWorker<T, V> iWorker;

    /**
     * 处理回调的callback
     */
    private ICallBack<T, V> callback;


    /**
     * 在自己后面的wrapper，如果没有，自己就是末尾；
     * 如果有一个，就是串行；
     * 如果有多个，有几个就需要开几个线程</p>
     * -------2
     * 1
     * -------3
     * 如1后面有2、3
     */
    private List<WorkerWrapper<?, ?>> nextWrappers;

    /**
     * 依赖的wrappers，有2种情况，
     * 1:必须依赖的全部完成后，才能执行自己
     * 2:依赖的任何一个、多个完成了，就可以执行自己
     * 通过must字段来控制是否依赖项必须完成
     * 1
     * -------3
     * 2
     * 1、2执行完毕后才能执行3
     */
    private List<DependWrapper> dependWrappers;

    /**
     * 标记该事件是否已经被处理过了，譬如已经超时返回false了，后续rpc又收到返回值了，则不再二次回调
     * 经试验,volatile并不能保证"同一毫秒"内,多线程对该值的修改和拉取
     * <p>
     * 1-finish, 2-error, 3-working
     */
    private AtomicInteger state = new AtomicInteger(0);

    /**
     * 该map存放所有wrapper的id和wrapper映射
     */
    private Map<String, WorkerWrapper> forParamUseWrappers;

    /**
     * 也是个钩子变量，用来存临时的结果
     */
    private volatile WorkResult<V> workResult = WorkResult.defaultResult();

    /**
     * 是否在执行自己前，去校验nextWrapper的执行结果<p>
     * 1   4
     * -------3
     * 2
     * 如这种在4执行前，可能3已经执行完毕了（被2执行完后触发的），那么4就没必要执行了。
     * 注意，该属性仅在nextWrapper数量<=1时有效，>1时的情况是不存在的
     */
    private volatile boolean needCheckNextWrapperResult = true;

    private static final int FINISH = 1;
    private static final int ERROR = 2;
    private static final int WORKING = 3;
    private static final int INIT = 0;


    public WorkerWrapper(String id, T param, IWorker<T, V> worker, ICallBack<T, V> callback) {
        if (worker == null) {
            throw new NullPointerException("async.worker is null");
        }
        this.id = id;
        this.param = param;
        this.iWorker = worker;
        //允许不设置回调
        if (callback == null) {
            /**设置默认的回调函数*/
            callback = new DefaultCallback<>();
        }
        this.callback = callback;
    }

    public void work(ThreadPoolExecutor threadPoolExecutor, long timeout, Map<String, WorkerWrapper> forParamUseWrappers) {

    }

    /**
     * 框架开始工作
     */
    public void work(ThreadPoolExecutor threadPoolExecutor, WorkerWrapper fromWrapper,
                     long timeout, Map<String, WorkerWrapper> forParamUseWrappers) {
        this.forParamUseWrappers = forParamUseWrappers;
        forParamUseWrappers.put(id, this);
        long now = SystemClock.now();
        /**总的已经超时啦，就快速失败，进行下一个*/
        if (timeout <= 0) {
            System.out.println("=== 快速失败 ===");
            fastFail(INIT, null);
            beginNext(threadPoolExecutor, now, timeout);
            return;
        }
        /**如果在执行前 需要校验一下nextWrapper的状态*/
        if (needCheckNextWrapperResult) {
            /**如果练上已经有任务执行完成啦，自己就不用执行啦,执行下一个*/
            if (!checkNextWrapperResult()) {
                System.out.println("=== 快速失败 ===");
                fastFail(INIT, null);
                beginNext(threadPoolExecutor, now, timeout);
                return;
            }
        }

        /**如果没有任何依赖，说明自己就是第一批执行的*/
        if (dependWrappers == null || dependWrappers.size() == 0) {
            System.out.println("=== 没有任何依赖 继续执行 " + Thread.currentThread().getName() + " ===");
            fire();
            beginNext(threadPoolExecutor, now, timeout);
            return;
        }

        /**
         *  如果前方有依赖，存在两种情况：
         *  1,前面只有一个wrapper，即 A-> B
         *  2,前面有多个wrapper，即 A C D -> B ,需要A C D都完成啦才能轮到B。
         *      但是无论是A执行完 还是C D 执行完 都回去唤醒B，
         *      所以需要B来做判断，必须B来判断，必须是ACD全部执行完，自己才能执行
         *
         * */
        if (dependWrappers.size() == 1) {
            System.out.println("===  存在依赖只有一个job 当前线程：" + Thread.currentThread().getName() + " === ");
            /**有一个依赖时*/
            doDependsOneJob(fromWrapper);
            /**前面的任务正常执行完毕，执行自己的任务*/
            fire();
            /**执行下一个任务*/
            beginNext(threadPoolExecutor, now, timeout);
        } else {
            System.out.println("===  存在多个依赖job 当前线程：" + Thread.currentThread().getName() + " ===");
            /**有多个依赖时*/
            doDependsJobs(threadPoolExecutor, dependWrappers, fromWrapper, now, timeout);
        }
    }

    /**
     * 校验调用链是否已全部执行完成
     */
    private boolean checkNextWrapperResult() {
        if (nextWrappers == null || nextWrappers.size() != 1) {
            return getState() == INIT;
        }
        WorkerWrapper nextWrapper = nextWrappers.get(0);
        boolean state = nextWrapper.getState() == INIT;
        /**继续校验下一个调用链的状态*/
        return state && nextWrapper.checkIsNullResult();
    }

    /**
     * 执行自己的job.具体的执行是在另一个线程里
     * 但判断阻塞超时是在work线程
     */
    private void fire() {
        //阻塞取结果
        System.out.println("=== 阻塞取结果 " + Thread.currentThread().getName() + " ===");
        workResult = workerDoJob();
    }

    /**
     * 具体单个worker执行任务
     */
    public WorkResult<V> workerDoJob() {

        /**避免重复执行(如果执行完成之后，workerresult 不能等于 default value)*/
        if (!checkIsNullResult()) {
            return workResult;
        }
        try {
            /**更新任务状态为 working，如果已经不是INIT 状态，说明正在被执行或执行完成，这一部很重要，可以保证任务不重复执行*/
            if (!compareAndSetState(INIT, WORKING)) {
                System.out.println("=== 当前任务已经执行完成或 正在执行中 ===");
                return workResult;
            }
            /**记录开始执行任务，调用回调方法*/
            callback.begin();

            /**具体执行我们的业务*/
            V resultValue = iWorker.action(param, forParamUseWrappers);

            /**更新任务状态为 success，如果状态不是working，说明被其他地方改变啦*/
            if (!compareAndSetState(WORKING, FINISH)) {
                System.out.println("=== 当前任务已经执行完成或 正在执行中 ===");
                return workResult;
            }

            workResult.setResultState(ResultState.SUCCESS);
            workResult.setResult(resultValue);


            return workResult;
        } catch (Exception ex) {
            /**避免重复回调*/
            if (!checkIsNullResult()) {
                return workResult;
            }

            fastFail(WORKING, null);
            return workResult;
        }

    }


    /**
     * 进行下一个任务
     *
     * @param now                当前时间
     * @param threadPoolExecutor 线程池
     * @param timeout            超时时间
     */
    private void beginNext(ThreadPoolExecutor threadPoolExecutor, long now, long timeout) {
        System.out.println("=== 进行下一个任务 " + Thread.currentThread().getName() + " ===");
        /**耗时时间*/
        long costtime = SystemClock.now() - now;
        System.out.println("=== 耗时 costtime: " + costtime + " ms , timeout:"
                + (timeout - costtime) + " ms");
        if (nextWrappers == null) {
            return;
        }
        /**调用链只有一个时，使用当前线程执行*/
        if (nextWrappers.size() == 1) {
            nextWrappers.get(0).work(threadPoolExecutor, WorkerWrapper.this,
                    timeout - costtime, forParamUseWrappers);
        }
        /**当调用链有多个时，进行并发执行*/
        CompletableFuture[] futures = new CompletableFuture[nextWrappers.size()];
        for (int i = 0; i < nextWrappers.size(); i++) {
            int finalI = i;
            CompletableFuture.runAsync(() -> nextWrappers.get(finalI).work(threadPoolExecutor, WorkerWrapper.this
                    , timeout - costtime, forParamUseWrappers), threadPoolExecutor);
        }
        try {
            CompletableFuture.allOf(futures).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * 快速失败
     */
    public boolean fastFail(int newVal, Exception e) {
        /**试图将当前wrapper  从 newVal 值改到 error*/
        if (!compareAndSetState(newVal, ERROR)) {
            return false;
        }

        /**处理尚未处理过的结果*/
        if (checkIsNullResult()) {

            if (e == null) {
                workResult = defaultResult();
            } else {
                workResult = defaultExResult(e);
            }

        }

        callback.call(false, param, workResult);
        return true;
    }


    /**
     * 处理只有一个依赖的任务
     */
    private void doDependsOneJob(WorkerWrapper dependWrapper) {
        System.out.println("=== 当前 dependWrapper 状态: " + dependWrapper.getWorkResult().getResultState() + "");
        /**校验依赖的 workerwrapper 是否执行完毕*/
        if (ResultState.TIMEOUT == dependWrapper.getWorkResult().getResultState()) {
            workResult = defaultResult();
            fastFail(INIT, null);
        } else if (ResultState.EXCEPTION == dependWrapper.getWorkResult().getResultState()) {
            workResult = defaultExResult(dependWrapper.getWorkResult().getEx());
            fastFail(INIT, null);
        }
    }


    /**
     * 处理多个依赖job,此处会有并发 操作 例如 ：
     * 0
     * 两个依赖任务同时并发操作
     * 1
     *
     * @param threadPoolExecutor
     * @param now
     * @param dependWrappers
     * @param fromWrapper
     * @param timeout
     */
    private synchronized void doDependsJobs(ThreadPoolExecutor threadPoolExecutor, List<DependWrapper> dependWrappers,
                                            WorkerWrapper fromWrapper, long now, long timeout) {
        boolean nowDepandIsMust = false;
        /**创建必须完成上有的wrapper集合*/
        Set<DependWrapper> mustWrapper = new HashSet<>();
        for (DependWrapper dependWrapper : dependWrappers) {
            /**校验当前依赖项是否是强依赖的*/
            if (dependWrapper.isMust()) {
                mustWrapper.add(dependWrapper);
            }
            /**校验依赖项是否与上游相等*/
            if (dependWrapper.getDependWrapper().equals(fromWrapper)) {
                nowDepandIsMust = dependWrapper.isMust();
            }
        }

        /**如果都不是不必须依赖的条件，到这里 只执行自己*/
        if (mustWrapper.size() == 0) {
            if (ResultState.TIMEOUT == fromWrapper.getWorkResult().getResultState()) {
                fastFail(INIT, null);
            } else {
                fire();
            }
            beginNext(threadPoolExecutor, now, timeout);
            return;
        }

        /**todo 如果存在需要必须完成的，且 fromWrapper 不是必须的，就什么都不做*/
        if (!nowDepandIsMust) {
            return;
        }

        /**如果fromwrapper 是必须依赖项*/
        boolean existNoFinish = false;
        boolean hasError = false;

        /**判断所依赖项的执行结果状态，如果失败，不走自己的action，直接break*/
        for (DependWrapper dependWrapper : dependWrappers) {
            WorkerWrapper workerWrapper = dependWrapper.getDependWrapper();
            WorkResult tempWorkResult = workerWrapper.getWorkResult();
            /**校验状态 为 init  或 working，说明他依赖的某个人物还没有执行完毕*/
            if (workerWrapper.getState() == INIT || workerWrapper.getState() == WORKING) {
                existNoFinish = true;
                break;
            }
            /**校验任务执行结果状态*/
            if (ResultState.TIMEOUT == tempWorkResult.getResultState()) {
                workResult = defaultResult();
                hasError = true;
                break;
            }
            if (ResultState.EXCEPTION == tempWorkResult.getResultState()) {
                workResult = defaultExResult(tempWorkResult.getEx());
                hasError = true;
                break;
            }
        }

        /**校验是否有失败的任务*/
        if (hasError) {
            fastFail(INIT, null);
            beginNext(threadPoolExecutor, now, timeout);
            return;
        }
        /**如果上游都没有失败，都已经全部完成，处理自己的任务*/
        if (!existNoFinish) {
            fire();
            beginNext(threadPoolExecutor, now, timeout);
        }
    }


    /**
     * 校验任务结果是否执行完毕
     */
    private boolean checkIsNullResult() {
        return ResultState.DEFAULT == workResult.getResultState();
    }


    /**
     * 更新任务状态
     *
     * @param expect 旧值
     * @param update 更新的值
     */
    private boolean compareAndSetState(int expect, int update) {
        return this.state.compareAndSet(expect, update);
    }

    /**
     * 设置默认值
     */
    private WorkResult<V> defaultResult() {
        workResult.setResultState(ResultState.TIMEOUT);
        workResult.setResult(iWorker.defaultValue());
        return workResult;
    }

    /**
     * 设置异常默认值
     */
    private WorkResult<V> defaultExResult(Exception ex) {
        workResult.setResultState(ResultState.EXCEPTION);
        workResult.setResult(iWorker.defaultValue());
        workResult.setEx(ex);
        return workResult;
    }


    public WorkResult<V> getWorkResult() {
        return workResult;
    }

    public void setWorkResult(WorkResult<V> workResult) {
        this.workResult = workResult;
    }


    public int getState() {
        return state.get();
    }

}
