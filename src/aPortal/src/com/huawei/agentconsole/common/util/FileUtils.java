
package com.huawei.agentconsole.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils
{

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);
    
    /**
     * 创建文件
     * @param path
     * @return
     */
    public static File createRecordFile(String path)
    {
        File tempFile = new File(path);
        //如果存在就删除
        if (tempFile.exists())
        {
            return tempFile;
        }
        
        File parent = tempFile.getParentFile();
        if (parent != null && (parent.exists() || parent.mkdirs()))
        {
            
            try
            {
                if (!tempFile.createNewFile())
                {
                    LOG.error("file [{}] create file failed!", LogUtils.encodeForLog(tempFile.getPath()));
                    return null;
                }
            }
            catch (SecurityException|IOException e)
            {
                LOG.error("file [{}] create file failed! The exception is \r\n {}", 
                        LogUtils.encodeForLog(tempFile.getPath()),  LogUtils.encodeForLog(e.getMessage()));
                return null;
            }
        }
        else
        {
            LOG.error("file [{}] create parent directory failed!", LogUtils.encodeForLog(tempFile.getPath()));
            return null;
        }
        
        return tempFile;
    }
    
    /**
     * 删除文件
     * @param tempFile
     * @return
     */
    public static boolean deleteFile(File tempFile)
    {
        if (tempFile.exists() && tempFile.isFile())
        {
            try
            {
                if (tempFile.delete())
                {                 
                    return true;
                }
                else
                {
                    LOG.error("file [{}] delete failed!", LogUtils.encodeForLog(tempFile.getPath()));
                    return false; 
                }
            }
            catch (SecurityException e)
            {
                LOG.error("file [{}] delete failed! The exception is \r\n {}", 
                        LogUtils.encodeForLog(tempFile.getPath()),  LogUtils.encodeForLog(e.getMessage()));
                return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    /**
     * 拷贝文件
     * @param oriPath
     * @param desPath
     * @return
     */
    public static boolean copyExcelFile(String oriPath,String desPath)
    {
        File oriFile = new File(oriPath);
        File desFile = new File(desPath);
        
        if (desFile.exists())
        {
            if(!desFile.delete())
            {
                LOG.error("delete file failed");
            }
        }
        File parentPath = desFile.getParentFile();
        if (parentPath != null && (parentPath.exists() || parentPath.mkdirs()))
        {
            try
            {
                if(!desFile.createNewFile())
                {
                    LOG.error("create file fail");
                    return false;
                }
                //赋值文件流
                if(copyFileStream(oriFile, desFile))
                {
                    return true;
                }
            }
            catch (IOException e)
            {
                LOG.error("copy file failed,the exception is {}",LogUtils.encodeForLog(e.getMessage()));
            }
        }   
        return false;
    }
    
    /**
     * 拷贝字节流
     * @param oriFile
     * @param desFile
     * @throws IOException
     */
    private static boolean copyFileStream(File oriFile,File desFile)
    {
        boolean result = false;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        
        try
        {
            fileInputStream = new FileInputStream(oriFile);
            fileOutputStream = new FileOutputStream(desFile);
            
            byte[] buffer = new byte[1024];
            int i = 0;
            while((i = fileInputStream.read(buffer)) > 0)
            {
                fileOutputStream.write(buffer, 0, i);
            }
            fileOutputStream.flush();
            result = true;
        }
        catch (IOException e) 
        {
            LOG.error("copy file error the exception is {}",LogUtils.encodeForLog(e.getMessage()));
        }
        finally
        {
            closeInputStream(fileInputStream);
            closeOutputStream(fileOutputStream);
        }
        return result;

    }
    
    /**
     * 关闭输入流
     * @param in
     */
    public static void closeInputStream(InputStream in)
    {
        try
        {
            if (null != in)
            {
                in.close();
            }
        }
        catch (IOException e)
        {
            LOG.error("Close input stream failed. The exception is {}", LogUtils.encodeForLog(e.getMessage()));
        }
    }
    
    /**
     * 关闭输出流
     * @param out
     */
    public static void closeOutputStream(OutputStream out)
    {
        try
        {
            if (null != out)
            {
                out.close();
            }
        }
        catch (IOException e)
        {
            LOG.error("Close out stream failed. The exception is {}", LogUtils.encodeForLog(e.getMessage()));
        }
    }
    
    /**
     * 临时文件转换成响应输出给客户端,同时删除临时文件
     */
    public static void fileToResponse(String filePath, HttpServletResponse response)
    {
        File file = new File(filePath);
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;
        
        try
        {
            response.setContentType("application/x-xls");
            response.addHeader("Content-Disposition", "attachment;filename=" + file.getName());
            fileInputStream = new FileInputStream(file);
            outputStream = response.getOutputStream();
            
            int i = 0;
            byte[] fileBuffer = new byte[1024];
            while ((i = fileInputStream.read(fileBuffer)) > 0)
            {
                outputStream.write(fileBuffer,0,i);
            }
            outputStream.flush();
        }
        catch (IOException e)
        {
            LOG.error("output failed ,the exception is {}", LogUtils.encodeForLog(e.getMessage()));
        }
        finally 
        {
            closeInputStream(fileInputStream);
            closeOutputStream(outputStream);
            deleteFile(file);
        }
    }
}
