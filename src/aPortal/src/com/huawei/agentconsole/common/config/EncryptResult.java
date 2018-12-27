
package com.huawei.agentconsole.common.config;


public class EncryptResult
{
    private boolean isSuccess;
    
    private String salt;
    
    private String result;
    
    public EncryptResult(boolean isSuccess, String salt, String result)
    {
        this.isSuccess = isSuccess;
        this.salt = salt;
        this.result = result;
    }

    public boolean isSuccess()
    {
        return isSuccess;
    }

    public String getSalt()
    {
        return salt;
    }

    public String getResult()
    {
        return result;
    }
    
}
