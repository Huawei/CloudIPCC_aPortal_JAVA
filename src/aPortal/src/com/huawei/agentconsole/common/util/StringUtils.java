
package com.huawei.agentconsole.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.constant.AgentErrorCode;

public abstract class StringUtils
{
    
    private static final Logger log = LoggerFactory
            .getLogger(StringUtils.class);
    
    private static final String DATE_STANDARD = "yyyyMMddHHmmss";
    /**
     * 对String的trim操作，支持null
     * @param st String对象
     * @return 操作后的对象
     */
    public static String trim(String st)
    {
        if (st == null)
        {
            return st;
        }
        return st.trim();
    }
    
  
    
    
    
    /**
     * 判断字符串是否为null或者为空字符串（含空格）。
     * @param str 字符串变量
     * @return true/false
     */
    public static boolean isNullOrBlank(String str)
    {
        return str == null || str.trim().isEmpty();
    }
    
    
    /**
     * 判断字符串是否为null或者空字符串（不含空格）。
     * @param str 字符串变量
     * @return true/false
     */
    public static boolean isNullOrEmpty(String str)
    {
        return str == null || str.isEmpty();
    }
    
    /**
     * 判断是否是安全目录
     * @return
     */
    public static boolean isInSecureDir(File file)
    {
        String canPath;
        try
        {
            canPath = file.getCanonicalPath();
        }
        catch (IOException e)
        {
            return false;
        }
        String absPath = file.getAbsolutePath();
        if (canPath.equalsIgnoreCase(absPath))
        {
            return true;
        }
        return false;
    }
    
    /**
     * 判断是否是安全目录
     * @return
     */
    public static boolean isRegularFile(Path filePath)
    {
        BasicFileAttributes attr;
        try
        {
            attr = Files.readAttributes(filePath, 
                    BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            return attr.isRegularFile();
        }
        catch (IOException e)
        {
            return false;
        }
    }
    
    public static String generateToken()
    {
        return UUID.randomUUID().toString();
        
    }

    
    public static String getRetCode(Map<String, Object> result)
    {
        String retCode = String.valueOf(result.get("retcode"));
        if (StringUtils.isNullOrBlank(retCode))
        {
            retCode = AgentErrorCode.RETURN_CONTENT_ERROR;
            result.put(retCode, retCode);
            return retCode;
        }
        else
        {
            return retCode;
        }
    }
    
    public static int strToInt(String str)
    {
        int result = 0;
        if (!isNullOrBlank(str))
        {
            try
            {

                result = Integer.parseInt(str);
                return result;
            }
            catch (NumberFormatException e)
            {
                log.error("str to int error");
            }
        }
        
        return 0;
    }
    
    public static int getCeil(int a, int b)
    {
        if (a%b == 0)
        {
            return a/b;
        }
        else
        {
            return a/b + 1;
        }
    }
    
    /**
     * 时间转为字符串
     * @return
     */
    public static  String formateDateToString()
    {  
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_STANDARD);
        Date date = new Date();
        return dateFormat.format(date);
    }
}
