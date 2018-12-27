
package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.constant.ParamPatternConstant;

/**
 * 
 * <p>Title:  座席登录信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月24日
 * @since
 */
public class LoginParam
{
    @Pattern(regexp = ParamPatternConstant.WORKNO_PATTERN)
    @NotBlank
    private String agentId;
    
    /**
     * 座席密码
     */
    @Size(min = CommonConstant.MIN_PASSWORD_NUMBER, max = CommonConstant.MAX_PASSWORD_NUMBER)
    private String password;
    
    /**
     * 座席电话号码
     */
    @Pattern(regexp = ParamPatternConstant.PHONE_PATTERN)
    @NotBlank
    @Size(min = CommonConstant.MIN_PHONE_NUMBER, max = CommonConstant.MAX_PHONE_NUMBER)
    private String phonenum;
    
    @Size(max = 4)
    private String verifyCode;

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPhonenum()
    {
        return phonenum;
    }

    public void setPhonenum(String phonenum)
    {
        this.phonenum = phonenum;
    }

    public String getAgentId()
    {
        return agentId;
    }

    public void setAgentId(String agentId)
    {
        this.agentId = agentId;
    }
    
    
    
    public String getVerifyCode()
    {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode)
    {
        this.verifyCode = verifyCode;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("phonenum:").append(phonenum);
        sb.append("}");
        return sb.toString();
    }
    
}
