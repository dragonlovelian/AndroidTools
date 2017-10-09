package com.dragon.androidtools.common_utils;



import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by dragon on 2017/9/29 0029.
 * 线程池
 */

public class ThreadPoolFactory {
    public static final ThreadPoolExecutor executor=new ThreadPoolExecutor(6,10,200, TimeUnit.MICROSECONDS,new ArrayBlockingQueue<Runnable>(5));
    /**
     * 执行线程任务
     * @param runnable
     */
   public static void executeTask(Runnable runnable){
        if(null!=runnable){
            executor.execute(runnable);
        }
   }

}
