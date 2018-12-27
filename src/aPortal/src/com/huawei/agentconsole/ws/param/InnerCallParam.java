package com.huawei.agentconsole.ws.param;



import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.NotBlank;
import com.huawei.agentconsole.common.constant.ParamPatternConstant;

/**
 * 
 * <p>Title:  内呼信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年7月31日
 * @since
 */
public class InnerCallParam
{
	/**
	 * 内呼座席工号
	 */
	@Pattern(regexp = ParamPatternConstant.WORKNO_PATTERN)
	@NotBlank
	private String called;
	
	public String getCalled()
	{
		return called;
	}
	
	public void setCalled(String called)
	{
		this.called = called;
	}
	
	public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("called:").append(called);
        sb.append("}");
        return sb.toString();
    }



}
