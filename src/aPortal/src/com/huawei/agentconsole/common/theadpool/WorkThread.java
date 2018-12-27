
package com.huawei.agentconsole.common.theadpool;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.util.LogUtils;




public class WorkThread extends Thread implements WorkThreadMBean
{

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkThread.class);

    private volatile boolean run = true;

    /** 已经处理的任务数. */
    private int finished;

    private final BlockingQueue<WorkTask> workQueue = new LinkedBlockingQueue<WorkTask>();

    private final Map<Integer, Integer> keyRef = new HashMap<Integer, Integer>();

    /**
     * 构造方法.
     * @param name  线程名
     */
    public WorkThread(String name)
    {
        super(name);

        //注册MBean
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try
        {
            ObjectName oname = new ObjectName("com.huawei.agentconsole:name=" + this.getName());
            mbs.registerMBean(this, oname);
        }
        catch (MalformedObjectNameException | InstanceAlreadyExistsException
                | MBeanRegistrationException | NotCompliantMBeanException e)
        {
            LOGGER.error("The work thread register MBean fail. name=" + this.getName() 
                    + ", exception = " + LogUtils.encodeForLog(e.getMessage()));
        }
		catch (Exception e)
        {
		    LOGGER.error("The work thread register MBean fail. name=" + this.getName() 
            + ", CommonException = " + LogUtils.encodeForLog(e.getMessage()));
        }
    }

    /**
     * 关闭工作线程.
     * 调用该方法后不一定会立即结束线程，只有在线程处于运行状态且处理完当前任务后才结束.
     */
    void shutdown()
    {
        this.run = false;
        //中断线程
        this.interrupt();

        //反注册MBean
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		try
        {
            ObjectName oname = new ObjectName("com.huawei.agentconsole:name=" + this.getName());
            mbs.unregisterMBean(oname);
        }
        catch (MalformedObjectNameException | MBeanRegistrationException | InstanceNotFoundException e)
        {
            LOGGER.error("The work thread unregister MBean fail. name=" + this.getName()
            + ", exception = " + LogUtils.encodeForLog(e.getMessage()));
        }
		catch (Exception e)
        {
		    LOGGER.error("The work thread unregister MBean fail. name=" + this.getName()
		    + ", CommonException = " + LogUtils.encodeForLog(e.getMessage()));
        }
    }

    int accept(WorkTask task)
    {
        Integer key = Integer.valueOf(task.getKey());

        synchronized (this.keyRef)
        {
            Integer refCount = this.keyRef.get(key);
            if (null != refCount)
            {
                //待处理任务中包含对应的键值，任务需要交由本线程处理
                return -1;
            }
            else
            {
                //否则以待处理任务数作为权重
                return this.workQueue.size();
            }
        }
    }

    /**
     * 增加处理任务.
     * @param task  待处理任务
     */
    void addTask(WorkTask task)
    {
        Integer key = Integer.valueOf(task.getKey());

        synchronized (this.keyRef)
        {
            Integer refCount = this.keyRef.get(key);
            if (null == refCount)
            {
                refCount = Integer.valueOf(1);
            }
            else
            {
                refCount = Integer.valueOf(refCount.intValue() + 1);
            }
            this.keyRef.put(key, refCount);
        }

        //增加待处理任务，如果队列满，任务将被丢弃
        boolean success = this.workQueue.offer(task);
        if (!success)
        {
            LOGGER.error("add task to queue fail. task=" + LogUtils.encodeForLog(task));
        }
    }

    @Override
    public void run()
    {
        while (this.run)
        {
            try
            {
                this.runTask();
            }
            catch (InterruptedException t)
            {
                LOGGER.error("run task exception, exception = " + LogUtils.encodeForLog(t.getMessage()));
            }
			catch (Throwable e)
			{
			    LOGGER.error("run task exception, CommonException = " + LogUtils.encodeForLog(e.getMessage()));
			}
			
        }
    }

    private void runTask() throws InterruptedException
    {
        WorkTask task;
        Integer key;
        int refCount;

        while (this.run)
        {
            task = this.workQueue.take();

            try
            {
                task.run();
            }
            finally
            {
                ++this.finished;

                key = Integer.valueOf(task.getKey());
                synchronized (this.keyRef)
                {
                    if (this.keyRef.containsKey(key))
                    {
                        Integer kr = this.keyRef.get(key);
                        if (null != kr)
                        {
                            refCount = kr.intValue();
                            if (refCount > 1)
                            {
                                this.keyRef.put(key, Integer.valueOf(refCount - 1));
                            }
                            else
                            {
                                //没有使用的key需要移除，避免内存漏泄
                                this.keyRef.remove(key);
                            }
                        }
                        else
                        {
                            //没有使用的key需要移除，避免内存漏泄
                            this.keyRef.remove(key);
                        }
                    }
                }
            }
        }
    }

    //以下为MBean方法
    public int getQueueSize()
    {
        return this.workQueue.size();
    }

    public int getFinishedTask()
    {
        return this.finished;
    }

    public int getReferenceKeySize()
    {
        synchronized (this.keyRef)
        {
            return this.keyRef.size();
        }
    }

}
