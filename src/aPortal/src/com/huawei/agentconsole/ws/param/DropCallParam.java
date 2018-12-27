package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.huawei.agentconsole.common.constant.ParamPatternConstant;

/**
 * 
 * <p>Title:  拆除指定callid呼叫信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年8月1日
 * @since
 */
public class DropCallParam
{
    @Pattern(regexp = ParamPatternConstant.CALLID_PATTERN)
	@NotBlank
    private String callId;
	
	public String getCallId()
	{
		return callId;
	}

	public void setCallId(String callId)
	{
		this.callId = callId;
	}
	
	public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("callId:").append(callId);
        sb.append("}");
        return sb.toString();
    }

}
