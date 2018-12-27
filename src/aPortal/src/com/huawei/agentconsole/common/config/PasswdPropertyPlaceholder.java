
package com.huawei.agentconsole.common.config;



import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;





/**
 * <p>Title: PassPropertyPlacehoder </p>
 * <p>Description:  </p>
 * <p>Copyright: Copyright (c) 2011</p>
 * <p>Company: 华为技术有限公司</p>
 * @author Lu Wei 163294
 * @version V1.0 
 * @since 2012-4-13
 */
public final class PasswdPropertyPlaceholder
{
    /**待加密的后缀*/
    public static final String PWD_SUBFIX = ".password";
    
    /**加密后的后缀*/
    public static final String ENCRYPWD_SUBFIX = ".encryptpassword";
    
    private static Logger log = LoggerFactory.getLogger(PasswdPropertyPlaceholder.class);
    
    private static StandardEncryptor encryptor = null;
    
    /**
     * 是否需要更新工作秘钥
     */
    private static boolean isNeedUpdateWorkKey = false;
    
    /**
     * 新工作秘钥的加密工具
     */
    private static StandardEncryptor newEncryptor = null;
    
    
    /**
     * 工作秘钥的盐值
     */
    public static final String WORKKEY_SALT = "bpst6Ghl08eluQxs8imQsw==";
    
    private PasswdPropertyPlaceholder()
    {
        
    }
    
    /**
     * 在系统启动时，进行工作秘钥的初始化
     */
    public static void init()
    {
        encryptor = new StandardEncryptor(RootKeyManager.getWorkKey(), WORKKEY_SALT); 
        isNeedUpdateWorkKey = RootKeyManager.isNeedUpdateWorkKey();
        if (isNeedUpdateWorkKey)
        {
            newEncryptor = new StandardEncryptor(RootKeyManager.getNewWorkKey(), WORKKEY_SALT); 
        }
    }
    
    public static void clean()
    {
        encryptor = null;
        newEncryptor = null;
    }
    
    /**
     * 该函数主要作用: 
     * 1) 会对文件中的xxx.password的key进行处理，将其加密，并且替换为xxx.encryptpassword
     * 并覆盖原始文件。
     * 2) 对propsOut(包含的是处理器前原始文件的key/value)的密码相关属性值进行重新设置
     *    （xxx.password/xxx.encryptpassword)，  替换为xxx/原始密码，供程序使用，
     *    程序中只要查询xxx这个键值就可以了。
     *    
     * @param file 需要进行密码替换的文件
     * @param propsOut 替换原始文件的密码相关的key/value，如果原先xxx.password，
     *  体内换成xxx；如果原始文件中是xxx.encryptpassword，也是替换成key，value都是密码的
     *  明文。
     */
    public static void loadProperties(String file, Properties propsOut)
    {

        FileInputStream in = null;
        FileOutputStream out = null;
        try
        {
            Properties propConfig = new Properties();
            in =  new FileInputStream(file);
            propConfig.load(in);
            propConfig.keySet().iterator();
            Iterator<Object> propKeys =  propConfig.keySet().iterator();
            String propKey = null;
            String propVal = null;
            String tmpKey = null;
            String tmpVal = null;
            String tmpDecVal = null;
            // 1. 处理密码
            while (propKeys.hasNext())
            {
                propKey = (String) propKeys.next();
                propVal = propConfig.getProperty(propKey);
                if (propKey.endsWith(PWD_SUBFIX))
                {
                    // 明文 -> 密文
                    // 配置文件中: XYZ.password -> XYZ.encryptpassword
                    tmpKey = propKey.replace(PWD_SUBFIX, ENCRYPWD_SUBFIX);
                    propConfig.setProperty(propKey, "");   // 删除配置文件中的原始密码
                    if (StringUtils.isNullOrBlank(propVal))
                    {
                        continue;
                    }
                    
                    tmpVal = encryptor.encryptAES(propVal);
                        
                    propConfig.setProperty(tmpKey, tmpVal);
                    
                    // 更新内存中数据(XYZ.password -> XYZ)
                    propsOut.remove(propKey);
                    propsOut.setProperty(propKey.substring(0, propKey.length() - PWD_SUBFIX.length()), propVal);
                    
                }
                else if (propKey.endsWith(ENCRYPWD_SUBFIX))
                {
                    // 密文 -> 明文
                    // 更新内存中数据(XYZ.encryptpassword -> XYZ)
                    propsOut.remove(propKey);
                    if (StringUtils.isNullOrBlank(propVal))
                    {
                        propsOut.setProperty(
                                propKey.substring(0, propKey.length() - ENCRYPWD_SUBFIX.length()), 
                                "");
                    }
                    else
                    {
                	    tmpDecVal = encryptor.decryptAES(propVal);
                        propsOut.setProperty(
                                propKey.substring(0, propKey.length() - ENCRYPWD_SUBFIX.length()), 
                                encryptor.decryptAES(propVal));
                        if (isNeedUpdateWorkKey)
                        {
                            //需要更新工作秘钥时，用新的工作秘钥进行加密
                            propConfig.setProperty(propKey, 
                                    newEncryptor.encryptAES(tmpDecVal));
                        }
                    }
                }
            }
            
            // 2. 保存文件
            in.close();
            out = new FileOutputStream(file);
            propConfig.store(out, "Update value");
        }
        catch (IOException e)
        {
            log.error("loadProperties {}", LogUtils.encodeForLog(e.getMessage()));
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
                    log.error("loadProperties {}", LogUtils.encodeForLog(e.getMessage()));
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
                    log.error("loadProperties {}", LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
    }
}
