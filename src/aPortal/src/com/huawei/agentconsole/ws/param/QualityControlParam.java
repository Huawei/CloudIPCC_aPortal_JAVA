package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.huawei.agentconsole.common.constant.ParamPatternConstant;

/**
 * <p>Title: 实时质检所需信息</p>
 * <p>Description: </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年8月2日
 * @since
 */
public class QualityControlParam
{
    @Pattern(regexp=ParamPatternConstant.WORKNO_PATTERN)
    @NotBlank
	private int workNo;
	
	private String mediaType;
	
	@Pattern(regexp=ParamPatternConstant.CALLID_PATTERN)
	@NotBlank
	private String callId;

	public int getWorkNo()
	{
		return workNo;
	}

	public void setWorkNo(int workNo)
	{
		this.workNo = workNo;
	}

	public String getMediaType()
	{
		return mediaType;
	}

	public void setMediaType(String mediaType)
	{
		this.mediaType = mediaType;
	}

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
        sb.append("workNo:").append(workNo);
        if(mediaType != null)
        {
        	sb.append(",").append("mediaType:").append(mediaType);
        }
        if(callId != null)
        {
        	sb.append(",").append("callId").append(callId);
        }
        sb.append("}");
        return sb.toString();
    }
	

}
