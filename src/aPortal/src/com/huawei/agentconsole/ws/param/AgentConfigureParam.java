package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.Pattern;

import com.huawei.agentconsole.common.constant.ParamPatternConstant;

/**
 * 
 * <p>Title:  坐席个人配置信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author l00467145
 * @version V1.0 2018年10月30日
 * @since
 */
public class AgentConfigureParam
{
    /**
     * 是否自动应答
     */
    private String isAutoAnswer;
    
    /**
     * 最大整理时长
     */
    @Pattern(regexp = ParamPatternConstant.MAXWORKTIME_PATTERN)
    private String maxWorkTime;
    
    /**
     * 外呼主叫号码
     */
    @Pattern(regexp = ParamPatternConstant.CALLER_PATTERN)
    private String  outCallerNo;

    public String isAutoAnswer()
    {
        return isAutoAnswer;
    }

    public String getIsAutoAnswer()
    {
        return isAutoAnswer;
    }

    public void setIsAutoAnswer(String isAutoAnswer)
    {
        this.isAutoAnswer = isAutoAnswer;
    }

    public void setAutoAnswer(String isAutoAnswer)
    {
        this.isAutoAnswer = isAutoAnswer;
    }

    public String getMaxWorkTime()
    {
        return maxWorkTime;
    }

    public void setMaxWorkTime(String maxWorkTime)
    {
        this.maxWorkTime = maxWorkTime;
    }

    public String getOutCallerNo()
    {
        return outCallerNo;
    }

    public void setOutCallerNo(String outCallerNo)
    {
        this.outCallerNo = outCallerNo;
    }
    
    
}
