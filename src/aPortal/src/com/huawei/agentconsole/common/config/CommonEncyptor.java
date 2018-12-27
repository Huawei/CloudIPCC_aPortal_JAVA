
package com.huawei.agentconsole.common.config;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.util.LogUtils;




public abstract class CommonEncyptor
{
    /**
     * Logger for this class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonEncyptor.class);
    /**
     * 盐值的字节大小
     */
    private static final int SALT_BYTE_SIZE = 16;
    
    /**
     * 获取盐值
     * @return 返回随机数字
     */
    public static String getSalt()
    {
        int contentLength;
        try
        {
            //获取盐值的长度
            contentLength = Integer.valueOf(RootKeyManager
                    .getValueFromKeysMap("CRYPT_SALT_BYTE_SIZE"));
        }
        catch (NumberFormatException e)
        {
            contentLength = SALT_BYTE_SIZE;
            LOGGER.error("CRYPT_SALT_BYTE_SIZE is invalid");
        } 
        byte[] salt = new byte[contentLength];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        try
        {
            return new String(Base64.encodeBase64(salt), CommonConstant.UTF_8);
        }
        catch (UnsupportedEncodingException e)
        {
            LOGGER.error("getSalt failed, error is {}", LogUtils.encodeForLog(e.getMessage()));
            return "";
        }
    }
}
