
package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.constant.ParamPatternConstant;


/**
 * 
 * <p>Title:  账号登录时候用的param</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author l00357702
 * @version V1.0 2018年9月20日
 * @since
 */
public class LoginByAccountParam
{
    @NotBlank
    @Pattern(regexp = ParamPatternConstant.WORKNO_PATTERN)
    private String agentAccount;
    
    @Size(min = CommonConstant.MIN_PASSWORD_NUMBER, max = CommonConstant.MAX_PASSWORD_NUMBER)
    private String password;
    
    @Size(max = 4)
    private String verifyCode;

    /**
     * @return the agentAccount
     */
    public String getAgentAccount()
    {
        return agentAccount;
    }

    /**
     * @param agentAccount the agentAccount to set
     */
    public void setAgentAccount(String agentAccount)
    {
        this.agentAccount = agentAccount;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return the verifyCode
     */
    public String getVerifyCode()
    {
        return verifyCode;
    }

    /**
     * @param verifyCode the verifyCode to set
     */
    public void setVerifyCode(String verifyCode)
    {
        this.verifyCode = verifyCode;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("LoginByAccountParam [agentAccount=");
        builder.append(agentAccount);
        builder.append(", verifyCode=");
        builder.append(verifyCode);
        builder.append("]");
        return builder.toString();
    }
    
}
