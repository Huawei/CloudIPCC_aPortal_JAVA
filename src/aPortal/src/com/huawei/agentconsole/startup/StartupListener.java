
package com.huawei.agentconsole.startup;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.config.ConfigList;
import com.huawei.agentconsole.common.config.ConfigProperties;
import com.huawei.agentconsole.common.config.PasswdPropertyPlaceholder;
import com.huawei.agentconsole.common.config.RootKeyManager;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.http.AgentRequest;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.dao.init.Mybatis;
import com.huawei.agentconsole.dao.init.MybatisForMySql;
import com.huawei.agentconsole.service.AgentTimeOutTask;
import com.huawei.agentconsole.service.DBServerStatusTask;
import com.huawei.agentconsole.service.HeartCheckTaskDispatch;
import com.huawei.agentconsole.service.TempFileDelete;
import com.huawei.agentconsole.service.VerifyCodeCheckTask;
import com.huawei.agentconsole.service.VerifyCodeService;

public class StartupListener implements ServletContextListener
{
    private static final Logger LOG = LoggerFactory.getLogger(StartupListener.class);    

    @Override
    public void contextDestroyed(ServletContextEvent event)
    {
        HeartCheckTaskDispatch.end();
        
        DBServerStatusTask.end();
        
        AgentTimeOutTask.end();
       
        VerifyCodeCheckTask.end();
        
        TempFileDelete.end();
        
    }

    @Override
    public void contextInitialized(ServletContextEvent event)
    {
        LOG.info("System initialization begin");

        String path = event.getServletContext().getRealPath("/");
        
        if (null != path)
        {
            GlobalObject.setApp_path(path);
        }
        /**
         * 解析keys.properties
         */
        LOG.info("Start Parse keys.properties begin...");
        if (!RootKeyManager.parseKeysProperties(path))
        {
            LOG.error("Parse keys.properties failed"); 
            PasswdPropertyPlaceholder.clean();
            return;
        }
        LOG.info("Start Parse keys.properties end...");
        
        /**
         * 初始化工作秘钥
         */
        PasswdPropertyPlaceholder.init();
        
        /**
         * 配置信息初始化
         */
        LOG.info("Start load config files...");
        if (!ConfigProperties.loadConfig())
        {
            LOG.error("Load config file failed, we are shutdown now.");
            PasswdPropertyPlaceholder.clean();
            RootKeyManager.cleanKey();
            return;
        }
        
        RootKeyManager.cleanKey();
        LOG.info("Start load config files end...");
        
        StringBuffer sb = new StringBuffer();
        if ("1".equals(ConfigProperties.getKey(ConfigList.BASIC, "AGENT_SERVER_ISSSL")))
        {
            sb.append("https://");
        }
        else
        {
            sb.append("http://");
        }
        sb.append(ConfigProperties.getKey(ConfigList.BASIC, "AGENT_SERVER_IP"));
        sb.append(":");
        sb.append(ConfigProperties.getKey(ConfigList.BASIC, "AGENT_SERVER_PORT"));
        sb.append("/agentgateway/resource");
        GlobalObject.setAgentServerUrl(sb.toString());
        
        //初始化CCID  VDNUSERNAME
        GlobalObject.addVdnInfo(CommonConstant.CCID, ConfigProperties.getKey(ConfigList.BASIC, CommonConstant.CCID));
        GlobalObject.addVdnInfo(CommonConstant.VDNUSERNAME, ConfigProperties.getKey(ConfigList.BASIC, CommonConstant.VDNUSERNAME));
        
        StringBuffer eventSb = new StringBuffer();
        if ("1".equals(ConfigProperties.getKey(ConfigList.BASIC, "LOCAL_SERVER_ISSSL")))
        {
            eventSb.append("https://");
        }
        else
        {
            eventSb.append("http://");
        }
        eventSb.append(ConfigProperties.getKey(ConfigList.BASIC, "LOCAL_SERVER_IP"));
        eventSb.append(":");
        eventSb.append(ConfigProperties.getKey(ConfigList.BASIC, "LOCAL_SERVER_PORT"));
        eventSb.append("/aPortal/resource/eventreceiver/event");
        GlobalObject.setEventPushUrl(eventSb.toString());
        GlobalObject.setCallcenterId(ConfigProperties.getKey(ConfigList.BASIC, "CALLCENTER_ID"));
        
        AgentRequest.init();
        
        HeartCheckTaskDispatch.begin();
        
        //初始化mybatis         
        if (ConfigProperties.getKey(ConfigList.BASIC, "ENABLE_UIDB").equals("true"))
        {
            LOG.info("Init uidb server  begin..."); 
            Mybatis.initUIDBSqlSessionFactory();
            LOG.info("Init uidb server  end...");            
        }
        if (ConfigProperties.getKey(ConfigList.BASIC, "EBABLE_SYSDB").equals("true"))
        {
            LOG.info("Init sysdb server  begin..."); 
            MybatisForMySql.initSYSDBSqlSessionFactory();
            LOG.info("Init sysdb server  end...");
        }
        /*DBServerStatusTask.begin();*/

        AgentTimeOutTask.begin();
        cleanTempRecordfile();
        
        if ("true".equalsIgnoreCase(ConfigProperties.getKey(ConfigList.VERIFY, "VERIFYCODE_ISUSED")))
        {
            //使用验证码校验
            VerifyCodeService.init();
            VerifyCodeCheckTask.begin(event.getServletContext());
        }
        
        //启动录音缓存文件定时清理
        TempFileDelete.begin();
    }
    /**
     * 启动的时候清楚临时缓存目录的文件
     */
    private void cleanTempRecordfile()
    {
        LOG.info("aConsole is starting, clean the temp record files start");
        File tempFile = FileUtils.getFile(GlobalObject.getApp_path() + CommonConstant.TEMPFILE_RECORD_PATH);
        if (null == tempFile)
        {
            LOG.error("clean temp file path failed");
            return;
        }
        File[] files = tempFile.listFiles(); //获取目录下的子文件和目录
        if (null != files &&  files.length > 0)
        {
            
            for (int i = 0; i < files.length; i++) 
            {
                
                cleanOnStartup(files[i]);
            }
        }
        LOG.info("aConsole is starting, clean the temp record files end");
    }

    /**
     * 循环删除临时目录下的文件
     * @param file
     */
    private void cleanOnStartup(File file)
    {
        if (file.isDirectory()) 
        {
            File[] files = file.listFiles(); //获取目录下的子文件和目录
            if (null != files &&  files.length > 0)
            {
                //递归删除目录中的子目录下
                for (int i = 0; i < files.length; i++) 
                {
                    cleanOnStartup(files[i]);
                }
            }
        }
        if(!file.delete())
        {
             LOG.error("delete {} failed.", LogUtils.encodeForLog(file.getName()));
        }
    }
}
