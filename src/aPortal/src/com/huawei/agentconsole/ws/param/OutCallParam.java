package com.huawei.agentconsole.ws.param;



import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.huawei.agentconsole.common.constant.ParamPatternConstant;
import com.huawei.agentconsole.common.util.LogUtils;

/**
 * 
 * <p>Title:  外呼信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年7月31日
 * @since
 */
public class OutCallParam
{
    /**
     * 主叫号码
     */
    @Pattern(regexp = ParamPatternConstant.CALLER_PATTERN)
    private String caller;
    
	/**
	 * 外呼电话号码
	 */
	@Pattern(regexp = ParamPatternConstant.CALLED_PATTERN)
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
	
	public String getCaller()
    {
        return caller;
    }

    public void setCaller(String caller)
    {
        this.caller = caller;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("caller:").append(LogUtils.formatPhoneNumber(caller));
        sb.append("called:").append(LogUtils.formatPhoneNumber(called));
        sb.append("}");
        return sb.toString();
    }
	

}
