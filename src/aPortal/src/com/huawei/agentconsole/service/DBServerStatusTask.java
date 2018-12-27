
package com.huawei.agentconsole.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.config.ConfigList;
import com.huawei.agentconsole.common.config.ConfigProperties;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.dao.init.MybatisForMySql;
import com.huawei.agentconsole.dao.init.Mybatis;
import com.huawei.agentconsole.dao.service.ServerStatusService;

public class DBServerStatusTask extends Thread
{

    private boolean isAlive = false;
    
    private static boolean isConnected = false;
    
    private static boolean isMysqlConnected = false;
    
   
    private static DBServerStatusTask task = new DBServerStatusTask();
    
    private static final Logger log = LoggerFactory
            .getLogger(DBServerStatusTask.class);
    
    
    public  void run()
    {
        boolean result = true;
        
        boolean isSysdbEnabled = ConfigProperties.getKey(ConfigList.BASIC, "EBABLE_SYSDB").equals("true");
        boolean isUidbEnabled = ConfigProperties.getKey(ConfigList.BASIC, "ENABLE_UIDB").equals("true");
        
        if (!isSysdbEnabled && !isUidbEnabled)
        {
            return;
        }
        
        while ( isAlive )
        {
            //检查UIDB连接
            if (isUidbEnabled)
            {
                try
                {
                    if (Mybatis.isInitDBOk())
                    {
                        result = ServerStatusService.getInstance().checkDBStatus();
                        
                        if (result)
                        {
                            if (!isConnected())
                            {
                                log.info(" connect DB successfully!");
                                setConnected(true);
                            }
                        }
                        else
                        {
                            if (isConnected())
                            {
                                log.error("Connect DB failed!!");
                                setConnected(false);
                            }
                        }
                    }
                 
                    if (!isConnected())
                    {
                        log.warn("Try to reconnect DB !!!");
                        Mybatis.initUIDBSqlSessionFactory();
                    }                              
                }
                catch (Exception e) 
                {
                    log.error(LogUtils.encodeForLog(e.getMessage()));
                }
            }
           
            //检测mysql连接
            if (isSysdbEnabled)
            {
                try
                {
                    if (MybatisForMySql.isInitDBOk())
                    {
                        result = ServerStatusService.getInstance().checkMysqlDBStatus();
                        
                        if (result)
                        {
                            if (!isMysqlConnected())
                            {
                                log.info(" connect mysql DB successfully!");
                                setMysqlConnected(true);
                            }
                        }
                        else
                        {
                            if (isMysqlConnected())
                            {
                                log.error("Connect mysql DB failed!!");
                                setMysqlConnected(false);
                            }
                        }
                    }
                 
                }
                catch (Exception e) 
                {
                    log.error(LogUtils.encodeForLog(e.getMessage()));
                }
            }
            
            //休眠10s
            try
            {
                sleep(CommonConstant.SERVER_STATUS_UPDATE_INTERVAL);
            }
            catch (InterruptedException e)
            {
                log.error(LogUtils.encodeForLog(e.getMessage()));
            }
            
        }        
        
    }
    
    /**
     * 启动检测任务
     */
    public static void begin()
    {
        if (task.isAlive)
        {
            log.info("DBServercheck task has been started. ");
            return;
        }
        
        task.isAlive = true;
        
        task.setName("DBServerStatusTask");
        task.start();
    }
    
    /**
     * 停止检测任务
     */
    public static void end()
    {
        if (!task.isAlive)
        {
            log.info("ServerStatusTask has been ended");
            return;
        }
        task.isAlive = false;
    }
    
    /**
     * 是否连接
     * @return boolean
     */
    public static boolean isConnected()
    {
        return isConnected;
    }
    
    /**
     * 设置连接状态
     * @param isConnected
     */
    public static void setConnected(boolean isConnected)
    {
        DBServerStatusTask.isConnected = isConnected;
    }
    
    /**
     * mysql是否连接
     * @return boolean
     */
    public static boolean isMysqlConnected()
    {
        return isMysqlConnected;
    }

    /**
     * 设置mysql连接状态
     * @param isConnected
     */
    public static void setMysqlConnected(boolean isMysqlConnected)
    {
        DBServerStatusTask.isMysqlConnected = isMysqlConnected;
    }

    
}
