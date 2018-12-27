
package com.huawei.agentconsole.common.theadpool;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ThreadPool
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPool.class);

    private final String poolName;

    private final String threadNamePrefix;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final int poolSize;

    private final int maxPoolSize;

    private WorkThread[] workers;
    
    
    private final byte [] LOCK_OBJECT = new byte[0];

    /**
     * @param name 线程池名
     * @param poolSize 初始线程数
     * @param maxPoolSize 最大线程数
     * @param keepAliveTime 空闲线程在关闭前等待时间（当前没有使用填充0值）
     */
    public ThreadPool(String name, int poolSize, int maxPoolSize, long keepAliveTime)
    {
        this.poolName = name;
        this.threadNamePrefix = "p-" + name + '-';

        this.poolSize = poolSize;
        this.maxPoolSize = maxPoolSize;

        this.initPool();

        LOGGER.info("initialized thread pool: " + name + ", size: " + poolSize);
    }

    private void initPool()
    {
        synchronized (LOCK_OBJECT)
        {
            this.workers = new WorkThread[this.poolSize];
            for (int i = 0; i < this.poolSize; ++i)
            {
                this.workers[i] = this.startNewThread();
            }
        }
        
    }

    private WorkThread startNewThread()
    {
        WorkThread t = new WorkThread(this.threadNamePrefix + this.threadNumber.getAndIncrement());
        t.setDaemon(true);
        t.start();

        LOGGER.info("create and start work thread: " + t);
        return t;
    }

    /**
     * 加入需要线程池处理的任务.
     * @param task 需要处理的任务
     */
    public void addTask(WorkTask task)
    {
        synchronized (LOCK_OBJECT)
        {
            WorkThread worker = this.selectWorker(task);
            
            if (null != worker)
            {
                worker.addTask(task);
            }
        }
    }

    private WorkThread selectWorker(WorkTask task)
    {
        WorkThread worker = null;
        int weight = Integer.MAX_VALUE;

        int w;
        for (WorkThread t : this.workers)
        {
            w = t.accept(task);
            if (-1 == w)
            {
                return t;
            }
            else if (w < weight)
            {
                weight = w;
                worker = t;
            }
        }

        if ((weight > 5) && (this.workers.length < this.maxPoolSize))
        {
            //启新线程进行处理
            worker = this.startNewThread();

            WorkThread[] temp = new WorkThread[this.workers.length + 1];
            System.arraycopy(this.workers, 0, temp, 0, this.workers.length);
            temp[this.workers.length] = worker;

            this.workers = temp;
        }

        return worker;
    }

    /**
     * 关闭线程池.
     */
    public void shutdown()
    {
        synchronized (LOCK_OBJECT)
        {
            for (WorkThread t : this.workers)
            {
                t.shutdown();
            }
            this.workers = null;
        }
    }

    @Override
    public String toString()
    {
        return "ThreadPool{name=" + this.poolName + ", size=" + (this.threadNumber.get() - 1) + '}';
    }

}
