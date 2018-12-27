package com.huawei.agentconsole.ws.param;



import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.NotBlank;
import com.huawei.agentconsole.common.constant.ParamPatternConstant;

/**
 * 
 * <p>Title:  二次拨号信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年8月1日
 * @since
 */
public class SecondDialParam
{
	/**
	 * 二次拨号号码
	 */
	@Pattern(regexp = ParamPatternConstant.CALLED_PATTERN)
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
}
