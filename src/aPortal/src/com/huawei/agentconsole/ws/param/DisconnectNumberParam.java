
package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.huawei.agentconsole.common.constant.ParamPatternConstant;
import com.huawei.agentconsole.common.util.LogUtils;

/**
 * 
 * <p>Title:  释放指定电话号码</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年8月8日
 * @since
 */
public class DisconnectNumberParam 
{
    @Pattern(regexp = ParamPatternConstant.PHONE_PATTERN)
    @NotBlank
	private String number;

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }
	
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("number").append(LogUtils.formatPhoneNumber(number)).append("}");
        return sb.toString();
    }

	
	
}
