
package com.huawei.agentconsole.common.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.util.LogUtils;



public abstract class ConfigProperties
{
    /**
     * 日志
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConfigProperties.class);
    
    /**
     * 读取后的配置信息列表
     */
    private static Map<String, Properties> propsMap = new ConcurrentHashMap<String, Properties>();
    
    /**
     * config目录路径
     */
    private static final String CONFIG_PATH = "config/";
    
    private static String getRootPath()
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (null == classLoader)
        {
            LOG.error("getRootPath failed. the classLoader is null");
            return "";
        }
        URL url = classLoader.getResource("");
        if (null != url)
        {
            String binPath = url.getPath();
            return binPath.substring(0, binPath.lastIndexOf("classes"));
        }
        else
        {
            LOG.error("getRootPath failed. the url is null");
        }
        return "";
    }
    
    /**
     * 在启动的时候，读取该函数用于将config下面存在的配置文件，全部读取进来
     * @param rootPath 路径
     * @return 读取配置文件成功或者失败
     */
    public static boolean loadConfig()
    {
        Field[] configListFields = ConfigList.class.getFields();
        
        //循环获取所有配置文件信息
        try
        {
            for (Field field : configListFields)
            {
                //获取到配置文件的文件名值 modified by l00198551, 修复findbugs问题DP_DO_INSIDE_DO_PRIVILEGED
                String filename = getFileName(field);
                String filepath = getRootPath() + CONFIG_PATH + filename;
                File fileConfig = new File(filepath);
                
                if (fileConfig.exists())
                {
                    readFileProperties(fileConfig);
                }
                else
                {
                    LOG.info(LogUtils.encodeForLog("Neither File:" 
                            + LogUtils.encodeForLog(fileConfig.getAbsoluteFile())
                            + " exist"));
                    //文件不存在,将该配置文件信息设置为空. Value值不能为空，因此添加一个空的实例化对象
                    propsMap.put(filename, new Properties());
                }
             }
        } 
        catch (IllegalArgumentException e)
        {
            LOG.info("Load properties from file has exception.",LogUtils.encodeForLog(e));
            return false;
        }
        catch (IllegalAccessException e)
        {
            LOG.info("Load properties from file has exception.",LogUtils.encodeForLog(e));
            return false;
        } 

        return true;
    }
    
    /**
     * 获取文件名
     * @param field
     * @return 文件名
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static String getFileName(Field field)
        throws IllegalArgumentException, IllegalAccessException
    {
        //获取到配置文件的文件名值
        return String.valueOf(field.get(null));
    }

    
    /**
     * 获取配置信息
     * 
     * @param configFile
     *            配置文件
     * @param key
     *            键
     * @return 配置信息
     */
    public static String getKey(String configFile, String key)
    {
        if (propsMap.containsKey(configFile))
        {
            String result = propsMap.get(configFile).getProperty(key);
            if (result != null)
            {
                return result.trim();
            }
        }
        return "";
    }
    
    /**
     * 保存配置信息
     * @param configFile 配置文件
     * @param key 键
     * @param value 值
     */
    public static void setKey(String configFile, String key, String value)
    {
        if (propsMap.containsKey(configFile))
        {
            propsMap.get(configFile).setProperty(key, value);
        }
    }
    
   
    /**
     * 获取指定配置文件的属性
     * @param fileName 配置文件的名称
     * @return 属性
     */
    public static Properties getProperties(String fileName)
    {
        return propsMap.get(fileName);
    }
    
    /**
     * 重新设置指定的配置文件属性
     * @param fileName 配置文件的名称
     * @param properties 配置文件属性
     */
    public static void setProperties(String fileName, Properties properties)
    {
        propsMap.put(fileName, properties);
    }
    
    
    /**
     * 从文件读取属性
     * @param file 文件
     */
    private static void readFileProperties(File file)
    {        
        InputStream inputFile = null;
        try
        {
            inputFile = new BufferedInputStream(new FileInputStream(
                    file.getAbsolutePath()));
            Properties props = new Properties();
            props.load(inputFile);
            propsMap.put(file.getName(), props);
        }
        catch (IOException e)
        {
            LOG.error("Load {} failed.\r\n{}", file.getName(), e.getMessage());
        }
        finally
        {
            closeFile(inputFile);
        }
        
        Properties temp = propsMap.get(file.getName());
        if (null != temp)
        {
            PasswdPropertyPlaceholder.loadProperties(file.getPath(), temp);
        }
    }
    
    
    /**
     * 关闭打开的文件流
     * @param inputFile 文件流
     */
    private static void closeFile(InputStream inputFile)
    {
        try
        {
            if (inputFile != null)
            {
                inputFile.close();
            }
        }
        catch (IOException e)
        {
            LOG.error("Close file failed.");
        }
    }
    
    
    /**
     * 重新加载特定文件的配置信息
     * @param configFile 配置文件
     */
    public static void reLoadConfig(String configFile)
    {
        String filepath = getRootPath() + CONFIG_PATH  + configFile;
        
        
        File file = new File(filepath);
        if (!file.exists())
        {
            LOG.info("File " + LogUtils.encodeForLog(file.getAbsoluteFile()) + "is not exist");
            /**
             * 文件不存在，将该配置文件信息设置为空
             * Value值不能为空，因此添加一个空的实例化对象
             */
            propsMap.put(configFile, new Properties());
            return;
        }
        
        InputStream inputFile = null;
        try
        {
            inputFile = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
            Properties props = new Properties();
            props.load(inputFile);
            propsMap.put(configFile, props);
        }
        catch (IOException e)
        {           
            LOG.error("Load {} failed.\r\n{}", LogUtils.encodeForLog(configFile), LogUtils.encodeForLog(e.getMessage()));
        }
        finally
        {
            closeFile(inputFile);
        } 

        Properties tempProp = propsMap.get(configFile);
        if (null != tempProp)
        {
            PasswdPropertyPlaceholder.loadProperties(filepath, tempProp);
        }
    }
    
    
}
