
package com.huawei.agentconsole.common.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;


import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;

public class RootKeyManager
{
    private static Logger log = LoggerFactory.getLogger(RootKeyManager.class);
    
    /**
     * keys.properties的内容
     */
    private static Map<String, String> keysMap = new HashMap<String, String>();
    
    /**
     * keys.properties文件
     */
    private static final String KEYS_PROPERTIES = "WEB-INF/config/keys.properties";
    
    /**
     * 文件分隔符
     */
    private static final String FILE_SPLIT = ";";
    
    /**
     * 根秘钥的盐值
     * 通过StandardEncryptor.getSalt生成
     */
    private static final String ROOTKEY_SALT = "8mc+tHgXSsKhThOZ/kPccg==";

    /**
     * 根秘钥
     */
    private static String rootKey = "";
    
    /**
     * 工作秘钥
     */
    private static String workKey = "";
    
    
    
    /**
     * 是否需要更新工作秘钥
     */
    private static boolean isNeedUpdateWorkKey = false;
    
    /**
     * 新的工作秘钥
     */
    private static String newWorkKey = null;
    
    /**
     * 安全随机数生成器
     */
    private static SecureRandom random = new SecureRandom();
    
   
//    public static void main(String args[])
//    {
//        parseKeysProperties("F:\\9.tools\\eclipse-jee-oxygen-3a-win32-x86_64\\eclipse-workspace\\CloudAgentConsole\\WebContent\\");
//    }
    
    /**
     * 清理密钥
     */
    public static void cleanKey()
    {
        rootKey = null;
        workKey = null;
        newWorkKey = null; 
    }
    
    /**
     * 判断是否需要更新工作秘钥
     * @return
     */
    public static boolean isNeedUpdateWorkKey()
    {
        return isNeedUpdateWorkKey;
    }
    
    /**
     * 获取新的工作秘钥
     * @return
     */
    public static String getNewWorkKey()
    {
        return newWorkKey;
    }

    /**
     * 获取指定key的对应的keys.properties的解析内容
     * @param key
     * @return
     */
    public static String getValueFromKeysMap(String key)
    {
        String value = keysMap.get(key);
        return null == value ? "" : value;
    }
    
    /**
     * 获取工作秘钥
     * @return
     */
    public static String getWorkKey()
    {
        return workKey;
    }
        
    /**
     * 在系统启动时进行解析keys.properties的内容
     * @param path keys.properties的路径
     * @return
     */
    public static boolean parseKeysProperties(String path)
    {
        /**
         * 1.读取keys.properties
         */
        String keysFilePath = path + KEYS_PROPERTIES;
        File keysFile = new File(keysFilePath);
        keysMap = readKeysFile(keysFile);
        
        /**
         * 2.获取根秘钥组件内容长度
         */
        String contentLengthStr = keysMap.get("CRYPT_KEYGEN_ROOTKEY_COMPONENTS_LENGTH");
        if (StringUtils.isNullOrBlank(contentLengthStr))
        {
            //根秘钥组件长度不能为空
            log.error("CRYPT_KEYGEN_ROOTKEY_COMPONENTS_LENGTH is empty");
            return false; 
        }
        
        int contentLength = 0;
        try
        {
            contentLength = Integer.parseInt(contentLengthStr);
        }
        catch (NumberFormatException e)
        {
            log.error("CRYPT_KEYGEN_ROOTKEY_COMPONENTS_LENGTH is invalid");
            return false;
        }
        
        
        /**
         * 3.获取原始根秘钥
         */
        rootKey = getRootKey(path, "CRYPT_KEYGEN_ROOTKEY_COMPONENTS", contentLength);
        if (null == rootKey)
        {
            log.error("generate root key failed!");
            return false;
        }
        
        
        
        /**
         * 4.获取工作秘钥并解密
         */
        String encyptWorkKey = keysMap.get("CRYPT_KEYGEN_WORKKEY");
        if (StringUtils.isNullOrBlank(encyptWorkKey))
        {
            //工作秘钥不能为空
            log.error("CRYPT_KEYGEN_WORKKEY is empty");
            return false; 
        }
        workKey = decryptByRootKey(encyptWorkKey);
        
        /**
         * 5.获取是否开启根密钥更新功能
         */
        String updateFlag =  keysMap.get("CRYPT_ROOTKEY_UPDATE_ENABLED");
        if (StringUtils.isNullOrBlank(updateFlag))
        {
            //是否开启根密钥更新功能的标记不能为空
            log.error("CRYPT_ROOTKEY_UPDATE_ENABLED is invalid");
            return false; 
        }
        
        if (!("false".equalsIgnoreCase(updateFlag)
                || "true".equalsIgnoreCase(updateFlag)))
        {
            //是否开启根密钥更新功能的标记只能为true或false
            log.error("CRYPT_ROOTKEY_UPDATE_ENABLED is invalid");
            return false;
        }
        
        /**
         * 6.需要进行根秘钥更新
         */
        if ("true".equalsIgnoreCase(updateFlag))
        {
           
            /**
             * 7.获取更新的根秘钥
             */
            String rootCompContents[] =  generateNewRootCompContent("CRYPT_KEYGEN_ROOTKEY_COMPONENTS", contentLength);
            if (rootCompContents.length == 0)
            {
                log.error("generate updated root key failed!");
                return false;
            }
            rootKey = computeRootkey(contentLength, rootCompContents);
            if (null == rootKey)
            {
                log.error("generate updated root key failed!");
                return false;
            }
            
            /**
             * 8.用更新后的根秘钥对工作秘钥进行加密
             */
            keysMap.put("CRYPT_KEYGEN_WORKKEY", encryptByRootKey(workKey));
            
            /**
             * 9.重新写keys.properties文件
             */
            keysMap.put("CRYPT_KEYGEN_ROOTKEY_COMPONENTS_UPDATE", "");
            keysMap.put("CRYPT_ROOTKEY_UPDATE_ENABLED", "false");
            rewriteKeysProperties(keysFile);
            updateRootKeyCompFile(path, "CRYPT_KEYGEN_ROOTKEY_COMPONENTS", rootCompContents);
            log.info("System change Root key success!");
        }
        
        /**
         * 10.获取是否开启工作密钥更新功能
         */
        String workKeyUpdate =  keysMap.get("CRYPT_WORKKEY_UPDATE_ENABLED"); 
        if (!("false".equalsIgnoreCase(workKeyUpdate)
                || "true".equalsIgnoreCase(workKeyUpdate)))
        {
            //是否开启工作密钥更新功能的标记只能为true或false
            log.error("CRYPT_WORKKEY_UPDATE_ENABLED is invalid");
            return false;
        }
        
        /**
         * 11. 更新工作秘钥
         */
        if ("true".equalsIgnoreCase(workKeyUpdate))
        {
            return doUpdateWorkKey(keysFile);
        }
        
        return true;
    }
    
    /**
     * 执行工作秘钥的更新操作
     * @param keysFile keys.properties文件
     * @return
     */
    private static boolean doUpdateWorkKey(File keysFile)
    {
        isNeedUpdateWorkKey = true;
        newWorkKey = CommonEncyptor.getSalt();
        
        /**
         * 初始化工作秘钥
         */
        PasswdPropertyPlaceholder.init();
        if (!ConfigProperties.loadConfig())
        {
            log.error("Load config file failed, we are shutdown now. Change work key Failed");
            return false;
        }
        
        keysMap.put("CRYPT_KEYGEN_WORKKEY", encryptByRootKey(newWorkKey));
        keysMap.put("CRYPT_WORKKEY_UPDATE_ENABLED", "false");
        rewriteKeysProperties(keysFile);
        isNeedUpdateWorkKey = false;
        workKey = newWorkKey;
        log.info("System change work key success!");
        return true;
    }


    /**
     * 生成根密钥文件的内容
     * @param contentLength 根证书文件内容的长度
     * @return
     */
    private static String generateRootKeyComponent(int contentLength)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < contentLength; i++)
        {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    
    /**
     * 生成新的根密钥文件的内容
     * @param contentLength 根证书文件内容的长度
     * @param fileNumber 根密钥文件个数
     * @return
     */
    private static String[] generateNewRootCompContent(String components, int contentLength)
    {
        String rootKeyFilesStr = keysMap.get(components);
        if (StringUtils.isNullOrBlank(rootKeyFilesStr))
        {
            log.error(components + " is empty");
            return new String[0];
        }
        
        //获取根密钥文件个数
        String []rootKeyFiles = rootKeyFilesStr.split(FILE_SPLIT);
        int fileNumber = rootKeyFiles.length;
        
        String roots[] = new String[fileNumber];
        for (int i = 0; i < fileNumber; i++)
        {
            roots[i] = generateRootKeyComponent(contentLength);
        }
        return roots;
    }
    
    /**
     * 更新根密钥文件的内容
     * @param rootCompContents
     */
    private static void updateRootKeyCompFile(String path, String components, String[] rootCompContents)
    {
        String rootKeyFilesStr = keysMap.get(components);
        if (StringUtils.isNullOrBlank(rootKeyFilesStr))
        {
            log.error(components + " is empty");
            return;
        }
        
        String []rootKeyFiles = rootKeyFilesStr.split(FILE_SPLIT);
        if (rootKeyFiles.length != rootCompContents.length)
        {
            log.error(components + " is invalid");
            return;
        }
        
        int fileNumber = rootKeyFiles.length;
        String tempFile;
        File file;
        FileOutputStream outputStream;
        for (int i = 0; i < fileNumber; i++)
        {
            tempFile =  path + "WEB-INF/"  +rootKeyFiles[i].trim();
            file = FileUtils.getFile(tempFile);
            if (file == null)
            {
                // 文件不存在或不是文件或不能读
                log.error("rootkey components {}　is not exist", LogUtils.encodeForLog(tempFile));
                return;
            }
            outputStream = null;
            try
            {
                outputStream = new FileOutputStream(file);
                outputStream.write(rootCompContents[i].getBytes(CommonConstant.UTF_8));
            }
            catch (FileNotFoundException e)
            {
                log.error("rootkey components {}　is not exist", LogUtils.encodeForLog(tempFile));
            }
            catch (IOException e)
            {
                log.error("rootkey components {} cannot write", LogUtils.encodeForLog(tempFile));
            }
            finally
            {
                if (outputStream != null)
                {
                    try
                    {
                        outputStream.close();
                    }
                    catch (IOException e)
                    {
                        log.error("rootkey components {}　close stream fialed", LogUtils.encodeForLog(tempFile));
                    }
                }
            }
            
            
        }
    }
   
    
    
    /**
     * 获取根秘钥
     * @param path 地址
     * @param components 根秘钥的组件
     * @param contentLength 根秘钥长度
     * @return
     */
    private static String getRootKey(String path, String components, int contentLength)
    {
        /**
         * 获取根秘钥组件地址
         */
        String rootKeyFilesStr = keysMap.get(components);
        if (StringUtils.isNullOrBlank(rootKeyFilesStr))
        {
            log.error(components + " is empty");
            return null;
        }
        
        /**
         * 根秘钥组件必须大于1
         */
        String []rootKeyFiles = rootKeyFilesStr.split(FILE_SPLIT);
        if (rootKeyFiles.length < 2)
        {
            log.error(components + " is invalid");
            return null;
        }
        
        /**
         * 获取根秘钥组件文件
         */
        int fileNumber = rootKeyFiles.length;
        String tempFile;
        File file;
        File[] files = new File[fileNumber];
        for (int i = 0; i < fileNumber; i++)
        {
            tempFile =  path + "WEB-INF/"  +rootKeyFiles[i].trim();
            file = FileUtils.getFile(tempFile);
            if (file == null)
            {
                // 文件不存在或不是文件或不能读
                log.error("rootkey components {}　is not exist or cannot read", LogUtils.encodeForLog(tempFile));
                return null;
            }
            try
            {
                file = file.getCanonicalFile();
            }
            catch (IOException e)
            {
                log.error("rootkey components is invalid. The error is {}", LogUtils.encodeForLog(e.getMessage()));
                return null;
            }
            if (!file.exists() || !file.isFile() || !file.canRead())
            {
                // 文件不存在或不是文件或不能读
                log.error("rootkey components {}　is not exist or cannot read", LogUtils.encodeForLog(tempFile));
                return null;
            }
            files[i] = file;
        }
        
        /**
         * 3.读取根秘钥组件的内容
         */
        String [] rootCompContents = new String[fileNumber];
        for (int i = 0; i < fileNumber; i++)
        {
            try
            {
                rootCompContents[i] = readKeyFileContent(files[i], contentLength);
            }
            catch (IOException e)
            {
                log.error("read rootkey component Contents failed. The error is {}",
                        LogUtils.encodeForLog(e.getMessage()));
                return null;  
            }
        }
        
       /**
        * 4.获取根秘钥内容
        */
        return computeRootkey(contentLength, rootCompContents);
    }
    
    /**
     * 获取根密钥内容
     * @param contentLength 根密钥文件长度
     * @param rootCompContents 根密钥文件的内容
     * @return
     */
    private static String computeRootkey(int contentLength, String [] rootCompContents)
    {
        StringBuilder sb = new StringBuilder();
        int fileNumber = rootCompContents.length;
        for (int i = 0; i < contentLength; i++)
        {
            try
            {
                int temp = Integer.parseInt(rootCompContents[0].substring(i, i + 1));
                for (int j = 1; j < fileNumber; j++)
                {
                    temp = temp ^ Integer.parseInt(rootCompContents[j].substring(i, i + 1));
                }
                sb.append(temp);
            }
            catch (NumberFormatException e)
            {
                log.error("Generate root key failed. The error is {}", 
                        LogUtils.encodeForLog(e.getMessage()));
                return null;  
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 读取keys.properties
     * @param file
     * @return
     */
    private static Map<String, String> readKeysFile(File file)
    {
        Map<String, String> resultMap = new HashMap<String, String>();
        FileInputStream in = null;
        try
        {
            Properties properties = new Properties();
            in =  new FileInputStream(file);
            properties.load(in);
            properties.keySet().iterator();
            Iterator<Object> propKeys =  properties.keySet().iterator();
            String propKey;
            String propVal;
            while (propKeys.hasNext())
            {
                propKey = (String) propKeys.next();
                propVal = properties.getProperty(propKey);
                resultMap.put(propKey, propVal);
            }
        }
        catch (FileNotFoundException e)
        {
            log.error("readKeysFile failed. the error is {}", LogUtils.encodeForLog(e.getMessage()));
        }
        catch (IOException e)
        {
            log.error("readKeysFile failed. the error is {}", LogUtils.encodeForLog(e.getMessage()));
        }
        finally 
        {
            if (null != in)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    log.error("readKeysFile failed. the error is {}", LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
        return resultMap;
    }
    
    /**
     * 读取指定文件中的内容
     * @param path 文件路径
     * @param contentLength 文件内容长度
     * @return
     * @throws IOException 
     */
    private static String readKeyFileContent(File file, int contentLength) throws IOException
    {
        InputStream in = null;
        BufferedReader reader = null;
        try
        {
            if (!StringUtils.isInSecureDir(file)
                    || !StringUtils.isRegularFile(file.toPath()))
            {
                log.error("file is not in sercure dir:" 
                        + LogUtils.encodeForLog(file.getAbsolutePath()));
                return "";
            }
            in = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(in, CommonConstant.UTF_8));
            StringBuffer sb = new StringBuffer();
            int intC;
            while ((intC = reader.read()) != -1)
            {
                //读取指定长度内容
                char c = (char) intC;
                sb.append(c);
                if (sb.length() > contentLength)
                {
                    log.error("key file is over:" + String.valueOf(contentLength));
                    return "";
                }
            }
            
            if (StringUtils.isNullOrBlank(sb.toString()))
            {
                log.error("key file is empty.");
                return "";
            }
            
            if (!isNumeric(sb.toString()))
            {
                log.error("key file is not number");
                return "";
            }
            return sb.toString();
        }
        catch (FileNotFoundException e)
        {
            throw e;
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            if (null != reader)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    log.error(LogUtils.encodeForLog(e.getMessage()));
                }
            }
            if (null != in)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    log.error(LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
    }
    
    /**
     * 判断是否为数字
     * @param str
     * @return
     */
    private static boolean isNumeric(String str)
    {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
    
    /**
     * 使用根秘钥加密
     * @param content 明文
     * @return 密文
     */
    private static String encryptByRootKey(String content)
    {
        StandardEncryptor encryptor = new StandardEncryptor(rootKey, ROOTKEY_SALT);
        return encryptor.encryptAES(content);
    }
    
    /**
    * 使用根密钥解密
    * @param content 密文
    * @return 明文。解密失败返回null
    */
    private static String decryptByRootKey(String content)
    {
        StandardEncryptor encryptor = new StandardEncryptor(rootKey, ROOTKEY_SALT);
        return encryptor.decryptAES(content);
    }
    
    /**
     * 在根秘钥进行更新时需要重写keys.properties
     * @param file keys.properties文件
     * @return true表示重写成功；false表示重写失败
     */
    private static boolean rewriteKeysProperties(File file)
    {
        FileInputStream in = null;
        FileOutputStream out = null;
        try
        {
            Properties properties = new Properties();
            in =  new FileInputStream(file);
            properties.load(in);
            properties.keySet().iterator();
            Iterator<Object> propKeys =  properties.keySet().iterator();
            String propKey;
            while (propKeys.hasNext())
            {
                propKey = (String) propKeys.next();
                properties.setProperty(propKey, keysMap.get(propKey));
            }
            in.close();
            out = new FileOutputStream(file);
            properties.store(out, "Update value");
            return true;
        }
        catch (FileNotFoundException e)
        {
            log.error("readKeysFile failed. the error is {}", LogUtils.encodeForLog(e.getMessage()));
        }
        catch (IOException e)
        {
            log.error("readKeysFile failed. the error is {}", LogUtils.encodeForLog(e.getMessage()));
        }
        finally 
        {
            if (null != in)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    log.error("readKeysFile failed. the error is {}", LogUtils.encodeForLog(e.getMessage()));
                }
            }
            if (null != out)
            {
                try
                {
                    out.close();
                }
                catch (IOException e)
                {
                    log.error("readKeysFile failed. the error is {}", LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
        return false;
    }
}
