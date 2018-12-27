
package com.huawei.agentconsole.common.config;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;





public final class StandardEncryptor
{
	/**
     * Logger for this class
     */
    private static Logger logger = LoggerFactory.getLogger(StandardEncryptor.class);
    
    /**
     * 128
     */
    private static final int NUMBER_128 = 128;
    
    
    /** 
     * 全局密钥
     */
    private String secretKey = "";
    
    /**
     * 连接字符串
     */
    private static final char SPLIT = ';';
   
    
    /**
     *  导出密钥的迭代次数
     */
    private static int DK_ITER_COUNT = 50000;
    
    /**
     *  导出密钥的字节长度
     */
    private static int DK_LENGTH = 256;
    
    
    
    /**
     * 将secretKey和salt通过PBKDF2进行加密获取到密钥，当前主要用于配置文件加密
     * @param secretKey 
     * @param salt
     */
    public StandardEncryptor(String secretKey, String salt)
    {
        int iterCount = 0;
        try
        {
            //获取迭代次数
            iterCount = Integer.valueOf(RootKeyManager
                    .getValueFromKeysMap("CRYPT_PKBDF2_ITERATION_COUNT"));
        }
        catch (NumberFormatException e)
        {
            iterCount = DK_ITER_COUNT;
            logger.error("CRYPT_PKBDF2_ITERATION_COUNT is invalid");
        }
    	this.secretKey = encryptWithPBKDF2WithSHA256(secretKey, salt, iterCount);
    }
    
    /**
     * 直接 将secretKey作为密钥，当前主要用于媒体内容的加密
     * @param secretKey
     */
    public StandardEncryptor(String secretKey)
    {
        this.secretKey = secretKey;
    }
    
    
    /**
     * 使用SHA256的PBKDF2进行加密
     * @param plaintext 明文
     * @param salt      盐值
     * @return          密文
     */
    private String encryptWithPBKDF2WithSHA256(String plaintext, String salt, int count)
    {
     // 1. 校验参数
        plaintext = (plaintext != null) ? plaintext : "";
        salt = (salt != null) ? salt : "";
        
        // 2. 加密
        String ciphertext = "";
        
        try 
        {
            PKCS5S2ParametersGeneratorEx generator = new PKCS5S2ParametersGeneratorEx();
            generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(plaintext.toCharArray()), 
                    salt.getBytes(CommonConstant.UTF_8), count);
            KeyParameter key = (KeyParameter)generator.generateDerivedMacParameters(getContentLength());
            ciphertext = new String(Base64.encodeBase64(key.getKey()), CommonConstant.UTF_8);
        }
        catch (UnsupportedEncodingException e)
        {
            logger.error("encryptWithPBKDF2 failed, {}", LogUtils.encodeForLog(e.getMessage()));
        }
        return ciphertext;
    }
    
    private int getContentLength()
    {
        int contentLength = 0;
        try
        {
            //获取密钥的字节长度
            contentLength = Integer.valueOf(RootKeyManager
                    .getValueFromKeysMap("CRYPT_PKBDF2_ENCRYPT_LENGTH"));
        }
        catch (NumberFormatException e)
        {
            contentLength = DK_LENGTH;
            logger.error("CRYPT_PKBDF2_ENCRYPT_LENGTH is invalid");
        }
        return contentLength;
    }
    

    /**
     * AES 加密
     * @param plaintext 明文
     * @return 密文
     */
    public String encryptAES(String plaintext)
    {
        if (StringUtils.isNullOrEmpty(plaintext))
        {
            return plaintext;
        }
        String encryptPwd = null;
        String salt = CommonEncyptor.getSalt();
        try
        {
            encryptPwd = encryptAES(plaintext.getBytes(CommonConstant.UTF_8), Base64.decodeBase64(salt.getBytes(CommonConstant.UTF_8)));
        }
        catch (UnsupportedEncodingException e)
        {
            logger.error("encryptAES error. {}", LogUtils.encodeForLog(e.getMessage()));
        }
        return salt + SPLIT + encryptPwd;
    }
    
    
    /**
     * AES 加密
     * @param plaintext 明文
     * @return 密文
     */
    public EncryptResult encryptContent(String plaintext)
    {
        if (StringUtils.isNullOrEmpty(plaintext))
        {
            return new EncryptResult(false, "", plaintext);
        }
        String encryptPwd;
        String salt = CommonEncyptor.getSalt();
        try
        {
            encryptPwd = encryptAES(plaintext.getBytes(CommonConstant.UTF_8), Base64.decodeBase64(salt.getBytes(CommonConstant.UTF_8)));
        }
        catch (UnsupportedEncodingException e)
        {
            logger.error("encryptContent error. {}", LogUtils.encodeForLog(e.getMessage()));
            return new EncryptResult(false, "", plaintext);
        }
        return new EncryptResult(true, salt, encryptPwd);
    }
    
    
    /**
     * AES 解密
     * @param ciphertext 密文
     * @return           明文
     */
    public String decryptAES(String ciphertext)
    {
        if (ciphertext == null || ciphertext.isEmpty())
        {
            return ciphertext;
        }
        String decryptPwd = null;
        try
        {
        	// 2. 将密文拆分成盐值和密码
        	byte []salt = null;
            String oldPass = "";
            int commaIdx = ciphertext.indexOf(SPLIT);
            if (commaIdx == -1)
            {
                salt = null;
                oldPass = ciphertext;
            }
            else
            {
            	salt = Base64.decodeBase64(ciphertext.substring(0, commaIdx).getBytes(CommonConstant.UTF_8));
                oldPass = ciphertext.substring(commaIdx + 1);
            }
            
            decryptPwd = decryptAES(oldPass.getBytes(CommonConstant.UTF_8), salt);
        }
        catch (UnsupportedEncodingException e)
        {
            logger.error("decryptAES error. {}", LogUtils.encodeForLog(e.getMessage()));
        }
        return decryptPwd;
    }
    
    /**
     * AES 加密
     * @param  plaintext 明文
     * @param salt 盐值
     * @return           密文
     * @throws UnsupportedEncodingException 
     */
    private String encryptAES(byte[] plaintext, byte[] salt) throws UnsupportedEncodingException
    {
        byte[] ciphertext = null;
      
        try 
        {
            SecretKeySpec key = new SecretKeySpec(getAESKey(), RootKeyManager.getValueFromKeysMap("CRYPT_AES_KEY_ALGORITHM"));
            Cipher cipher = Cipher.getInstance(RootKeyManager.getValueFromKeysMap("CRYPT_CIPHER_TRANSFORMATION"));
            IvParameterSpec iv = new IvParameterSpec(salt);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            ciphertext = cipher.doFinal(plaintext);
        }
        catch (RuntimeException e) 
        {
        	logger.error("encryptAES error. {}", LogUtils.encodeForLog(e.getMessage()));
            throw new RuntimeException("encryptAES failed.");
        }
        catch (Exception e) 
        {
        	logger.error("encryptAES error. {}", LogUtils.encodeForLog(e.getMessage()));
            throw new RuntimeException("encryptAES failed.");
        }
        
        return new String(Base64.encodeBase64(ciphertext), CommonConstant.UTF_8);
    }
    
    /**
     * AES 解密
     * @param ciphertext 密文
     * @param salt 盐值
     * @return           明文
     * @throws UnsupportedEncodingException 
     */
    private String decryptAES(byte[] ciphertext, byte[] salt) throws UnsupportedEncodingException
    {
        ciphertext = Base64.decodeBase64(ciphertext);
        byte[] plaintext = null;
        try 
        {
            SecretKeySpec key = new SecretKeySpec(getAESKey(), RootKeyManager.getValueFromKeysMap("CRYPT_AES_KEY_ALGORITHM"));
            Cipher cipher = Cipher.getInstance(RootKeyManager.getValueFromKeysMap("CRYPT_CIPHER_TRANSFORMATION"));
            IvParameterSpec iv = new IvParameterSpec(salt);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            plaintext = cipher.doFinal(ciphertext);
        } 
        catch (RuntimeException e) 
        {
        	logger.error("decryptAES error. {}", LogUtils.encodeForLog(e.getMessage()));
            throw new RuntimeException("decryptAES failed.");
        }
        catch (Exception e) 
        {
        	logger.error("decryptAES error. {}", LogUtils.encodeForLog(e.getMessage()));
            throw new RuntimeException("decryptAES failed.");
        }
        
        return new String(plaintext, CommonConstant.UTF_8);
    }
    
    /**
     * 获取 AES 加密算法的 KEY
     * @return key
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException 
     */
    private byte[] getAESKey() throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        KeyGenerator kgen = KeyGenerator.getInstance(RootKeyManager.getValueFromKeysMap("CRYPT_AES_KEY_ALGORITHM"));
        SecureRandom secureRandom = SecureRandom.getInstance(RootKeyManager.getValueFromKeysMap("CRYPT_AES_KEY_SECURERANDOM_ALGORITHM"));
        secureRandom.setSeed(secretKey.getBytes(CommonConstant.UTF_8)); 
        int contentLength = 0;
        try
        {
            //AES加密算法的key的长度
            contentLength = Integer.valueOf(RootKeyManager
                    .getValueFromKeysMap("CRYPT_AES_KEY_CONTENT_LENGTH"));
        }
        catch (NumberFormatException e)
        {
            contentLength = NUMBER_128;
            logger.error("CRYPT_AES_KEY_CONTENT_LENGTH is invalid");
        } 
        kgen.init(contentLength, secureRandom);
        SecretKey tmpSecretKey = kgen.generateKey();
        return tmpSecretKey.getEncoded();
    }
	
	
}
