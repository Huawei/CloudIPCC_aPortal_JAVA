package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.util.LogUtils;

/**
 * 
 * <p>Title:  呼叫转移信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年8月1日
 * @since
 */
public class TransferParam
{
	/**
	 *转移设备类型 
	 */
	@Min(1)
	@Max(5)
	private int deviceType;
	
	/**
	 *转移设备地址 
	 */
	@Size(max = CommonConstant.MAX_ADDRESS_NUMBER)
	@NotBlank
	private String address;
	
	/**
	 *转移模式
	 */
	@Max(4)
	@Min(0)
	private int mode;

	public int getDeviceType()
	{
		return deviceType;
	}

	public void setDeviceType(int deviceType)
	{
		this.deviceType = deviceType;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public int getMode()
	{
		return mode;
	}

	public void setMode(int mode)
	{
		this.mode = mode;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append("deviceType:").append(deviceType).append(",");
		sb.append("address:").append(LogUtils.formatPhoneNumber(address)).append(",");
		sb.append("mode:").append(mode);
		sb.append("}");
		return sb.toString();
	}
	
	
	

	
	

}
