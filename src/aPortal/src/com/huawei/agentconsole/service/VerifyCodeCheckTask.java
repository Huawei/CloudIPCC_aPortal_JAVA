
package com.huawei.agentconsole.service;

import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.config.ConfigList;
import com.huawei.agentconsole.common.config.ConfigProperties;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.util.LogUtils;

/**
 * 
 * <p>Title:  进行定时判断是否还需要进行验证码检验</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年8月25日
 * @since
 */
public class VerifyCodeCheckTask extends Thread
{
    private static final Logger LOG = LoggerFactory.getLogger(VerifyCodeCheckTask.class);
    
    /**
     * 每10s执行一次
     */
    private static final long SLEEP_TIME = 10000l;
    
    private static final long DEFAULT_VERIFYCODE_MAXVALIDTIME = 300000L;
    
    private static VerifyCodeCheckTask instance;
    
    /**
     * 线程是否启动
     */
    private boolean isAlive = false;
    
    private ServletContext servletContext;
    
    private VerifyCodeCheckTask(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }
    
    public void run()
    {
        Enumeration<String> enmus;
        Date lastUpdateTime;
        String key;
        Date currentDate;
        long maxLiveTime;
        try
        {
            maxLiveTime = Long.valueOf(ConfigProperties.getKey(ConfigList.VERIFY, "VERIFYCODE_MAXVALIDTIME"));
        }
        catch (NumberFormatException e)
        {
            LOG.error("Get VERIFYCODE_MAXVALIDTIME failed. The error is {}", LogUtils.encodeForLog(e.getMessage()));
            maxLiveTime = DEFAULT_VERIFYCODE_MAXVALIDTIME;
        }
        while (isAlive)
        {
            try
            {
                enmus = servletContext.getAttributeNames();
                currentDate = new Date();
                if (null != enmus)
                {
                    while(enmus.hasMoreElements())
                    {
                        key = enmus.nextElement();
                        if (key.contains(CommonConstant.IS_NEED_VERIFY))
                        {
                            lastUpdateTime = (Date) servletContext.getAttribute(key);
                            if (null != lastUpdateTime
                                    && (currentDate.getTime() > lastUpdateTime.getTime() + maxLiveTime))
                            {
                                //不在需要校验验证码了
                                servletContext.removeAttribute(key);
                                LOG.info("Remove agent verifycode info {}", LogUtils.encodeForLog(key));
                            }
                        }
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
    
    public static void begin(ServletContext servletContext)
    {
        if (null == instance)
        {
            instance = new VerifyCodeCheckTask(servletContext);
            instance.isAlive = true;
            instance.setName("VerifyCodeCheckTask");
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
