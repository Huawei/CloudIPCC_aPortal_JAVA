package com.huawei.agentconsole.service;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.exception.CommonException;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.LogUtils;





public class TempFileDelete extends Thread
{
    private static final Logger LOG = LoggerFactory.getLogger(TempFileDelete.class);
    
    private static final long TEMP_FILE_TIMEOUT = 10 * 60000l; //更新超过10分钟，则清理
    
    private static final long SLEEP_INTERVAL = 60000l; //执行间隔：1分钟
    
    private static TempFileDelete instance;
    
    private boolean isAlive = false;
    
    private TempFileDelete()
    {
        
    }
    
    @Override
    public void run()
    {
        //清理录音缓存 以及 excel文件缓存
        File tempRecordFile = FileUtils.getFile(getTempRecordPath());
        if (null == tempRecordFile)
        {
            LOG.error("get the path {} failed, so not stop TempFileDelete", 
                    LogUtils.encodeForLog(getTempRecordPath()));
            return;
        }
        

        File tempExcelFile = FileUtils.getFile(getTempExcelPath());
        if (null == tempExcelFile)
        {
            LOG.error("get the path {} failed, so not stop TempFileDelete", 
                    LogUtils.encodeForLog(getTempExcelPath()));
            return;
        }
        while(isAlive)
        {
            doCleanTempFile(tempRecordFile);
            doCleanTempFile(tempExcelFile);
            doSleep();
        }
    }
    
    /**
     * 得到录音缓存目录
     * @return
     */
    private static String getTempRecordPath()
    {
        String tempRecordPath = GlobalObject.getApp_path() +  CommonConstant.TEMPFILE_RECORD_PATH;
        return tempRecordPath;
    }
    
    /**
     * 得到excel缓存文件目录
     * @return
     */
    private static String getTempExcelPath()
    {
        String tempExcelPath = GlobalObject.getApp_path() +  CommonConstant.TEMPFILE_EXCEL_PATH;
        return tempExcelPath;
    }
    
    /**
     * 清空文件
     * @param file
     */
    private void doCleanTempFile(File file)
    {
        try
        {
            try
            {
                if (!file.exists())
                {
                    return;
                }
                
                if (file.isDirectory())
                {
                    //当前为目录
                    File[] files = file.listFiles(); //获取目录下的子文件和目录
                    if (null == files)
                    {
                        return;
                    }
                    if (files.length == 0)
                    {
                        if (file.getCanonicalPath().endsWith(CommonConstant.TEMPFOLDER_RECORD_PATH) ||file.getCanonicalPath().endsWith(CommonConstant.TEMPFOLDER_EXCEL_PATH))
                        {
                            return;
                        }
                        //当前目录为空，则直接删除
                        if(!file.delete())
                        {
                             LOG.error("delete {} failed.", LogUtils.encodeForLog(file.getName()));
                        }
                        return;
                    }
                    
                    for (int i = 0; i < files.length; i++)
                    {  
                        //删除子文件和目录
                        doCleanTempFile(files[i]);
                    }  
                }
                else
                {
                    long lastModify = file.lastModified();//获取文件的修改时间点
                    if ((new Date().getTime() - lastModify) > TEMP_FILE_TIMEOUT)
                    {
                        if(!file.delete())
                        {
                             LOG.error("delete {} failed.", LogUtils.encodeForLog(file.getName()));
                        }  
                    }
                }
            }
            catch (Exception e)
            {
                throw new CommonException(e);
            }
        }
        catch (CommonException e)
        {
            LOG.error("delete {} failed. the exception is \r\n {}", 
                    LogUtils.encodeForLog(file.getPath()), LogUtils.encodeForLog(e.getMessage()));
        }
    }

    
    private void doSleep()
    {
        try
        {
            sleep(SLEEP_INTERVAL);
        }
        catch (InterruptedException e)
        {
            LOG.warn("Sleep failed. the exception is \r\n {}", LogUtils.encodeForLog(e.getMessage()));
        }
    }
    
    public static void begin()
    {
        if (null != instance)
        {
            return;
        }
        instance = new TempFileDelete();
        instance.isAlive = true;
        instance.setName("TempRecordCleanTask");
        instance.start();
    }
    
    public static void end()
    {
        if (null != instance)
        {
            instance.isAlive = false;
        }
    }
}