package com.huawei.agentconsole.ws.param;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 
 * <p>Title:  取保持信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年7月31日
 * @since
 */
public class GetHoldParam
{
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