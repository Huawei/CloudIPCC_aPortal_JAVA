
package com.huawei.agentconsole.service;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.common.config.ConfigList;
import com.huawei.agentconsole.common.config.ConfigProperties;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.LogUtils;

/**
 * 
 * <p>Title: 超时检查 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月30日
 * @since
 */
public class AgentTimeOutTask extends Thread
{
    private static final Logger LOG = LoggerFactory.getLogger(AgentTimeOutTask.class);
    
    /**
     * 每100ms执行一次
     */
    private static final int SLEEP_TIME = 100;
    
    /**
     * 默认最大超时时间为5分钟
     */
    private static final long MAX_TIMEOUT = 300000l;
    
    private static final long WARN_TIME = 60000l;
    
    
    private static AgentTimeOutTask instance = null;
    
    /**
     * 线程是否启动
     */
    private boolean isAlive = false;
    
    private AgentTimeOutTask()
    {
        
    }
    
    public void run()
    {
        long maxTimeout;
        try
        {
            maxTimeout = Long.valueOf(ConfigProperties.getKey(ConfigList.BASIC, "AGENT_MAX_TIMEOUT")) * 60000l;
        }
        catch (NumberFormatException e1)
        {
            LOG.error("Get AGENT_MAX_TIMEOUT failed. The error is {}", e1.getMessage());
            maxTimeout = MAX_TIMEOUT;
        }
        Map<String, AgentBaseInfoBean> allAgents;
        Iterator<Entry<String, AgentBaseInfoBean>> iterator;
        Entry<String, AgentBaseInfoBean> entry;
        AgentBaseInfoBean agentInfoBean;
        int lastCallTimes;
        OnlineAgentService onlineAgentService;
        while (isAlive)
        {
            
            try
            {
                allAgents = GlobalObject.getAllAgentBaseInfos();
                iterator = allAgents.entrySet().iterator();
                
                while (iterator.hasNext())
                {
                    entry = iterator.next();
                    agentInfoBean = entry.getValue();
                    lastCallTimes = agentInfoBean.getLastCallTimes();
                    if (lastCallTimes > maxTimeout)
                    {
                        LOG.error(LogUtils.AGENT_ID + "logout by timeout",  agentInfoBean.getAgentId());
                        onlineAgentService  = new OnlineAgentService(agentInfoBean.getAgentId());
                        onlineAgentService.logout();
                        GlobalObject.delAgentBaseInfo(agentInfoBean.getAgentId());
                    }
                    else 
                    {
                        if (lastCallTimes != 0  && lastCallTimes % WARN_TIME == 0)
                        {
                            LOG.error(LogUtils.AGENT_ID + "has {} mintues not receive any message from client", 
                                    agentInfoBean.getAgentId(), (lastCallTimes / WARN_TIME));
                        }
                        agentInfoBean.setLastCallTimes(lastCallTimes + SLEEP_TIME);
                    }
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
            LOG.error("Sleep failed. The error is {}", e.getMessage());
        }
    }
    
    
    public static void begin()
    {
        if (null == instance)
        {
            instance = new AgentTimeOutTask();
            instance.isAlive = true;
            instance.setName("AgentTimeOutTask");
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
