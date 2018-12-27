
package com.huawei.agentconsole.service;


import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.common.config.ConfigList;
import com.huawei.agentconsole.common.config.ConfigProperties;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.LogUtils;

/**
 * 
 * <p>Title:  心跳检测的任务分发</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月25日
 * @since
 */
public class HeartCheckTaskDispatch extends Thread
{
    private static final Logger LOG = LoggerFactory.getLogger(HeartCheckTaskDispatch.class);
    
    private static final long SLEEP_TIME = 60000l;
    
    private static final int MAX_THEARDS = 100;
    
    private static HeartCheckTaskDispatch instance;
    
    
    
    private boolean isAlive = false;
    
    private ExecutorService threadPool;
    
    private HeartCheckTaskDispatch()
    {
        
    }
    
    public void run()
    {
        int maxThreads;
        try
        {
            maxThreads = Integer.valueOf(ConfigProperties.getKey(ConfigList.BASIC, "HEART_CHECK_MAX_THREAD"));
        }
        catch (NumberFormatException e1)
        {
            maxThreads = MAX_THEARDS;
            LOG.error("Get HEART_CHECK_MAX_THREAD failed. The error is {}", e1.getMessage());
        }
        threadPool = Executors.newFixedThreadPool(maxThreads);
        Map<String, AgentBaseInfoBean> allAgents;
        Iterator<Entry<String, AgentBaseInfoBean>> iter;
        Entry<String, AgentBaseInfoBean> entry;
        while (isAlive)
        {
            try
            {
                allAgents = GlobalObject.getAllAgentBaseInfos();
                iter = allAgents.entrySet().iterator();
                while (iter.hasNext())
                {
                    entry = iter.next();
                    threadPool.execute(new HeartCheckTaskThread(entry.getKey())); 
                }
            }
            catch (Exception e)
            {
                LOG.error("Unkown exception. The error is {}", e.getMessage());
            }
            
            doSleep();
        }
    }
    
    private void doSleep()
    {
        try
        {
            sleep(SLEEP_TIME);
        }
        catch (InterruptedException e)
        {
            LOG.error("Sleep failed. The error is {}", 
                    LogUtils.encodeForLog(e.getMessage()));
        }
    }
    
    public static void begin()
    {
        if (null == instance)
        {
            instance = new HeartCheckTaskDispatch();
            instance.isAlive = true;
            instance.setName("HeartCheckTaskDispatch");
            instance.start();
        }
    }
    
    public static void end()
    {
        if (null != instance)
        {
            instance.isAlive = false;
        }
    }
}
