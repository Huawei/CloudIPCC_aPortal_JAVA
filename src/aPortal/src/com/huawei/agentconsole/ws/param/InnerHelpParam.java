package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.huawei.agentconsole.common.constant.ParamPatternConstant;

/**
 * 
 * <p>Title:  内部求助信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年8月1日
 * @since
 */
public class InnerHelpParam
{
	/**
	 *求助对象，座席工号或技能队列ID 
	 */
	@Pattern(regexp = ParamPatternConstant.WORKNO_PATTERN)
	@NotBlank
	private String dstAddress;
	
	/**
	 * 设备类型，技能队列为1，座席工号为2
	 */
	private int deviceType = 2;
	
	/**
	 * 1为两方求助；2为三方求助
	 */
	private int mode = 1;

	public String getDstAddress()
	{
		return dstAddress;
	}

	public void setDstAddress(String dstAddress)
	{
		this.dstAddress = dstAddress;
	}
	
	
	
	public int getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("dstAddress:").append(dstAddress).append(",");
        sb.append("deviceType:").append(deviceType).append(",");
        sb.append("mode:").append(mode);
        sb.append("}");
        return sb.toString();
    }
	
	

}
