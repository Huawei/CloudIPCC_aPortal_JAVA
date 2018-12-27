
package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.NotNull;


/**
 * 
 * <p>Title:  是否自动应答的配置</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年8月10日
 * @since
 */
public class AutoAnswerParam
{
    @NotNull
    private boolean autoAnswer;

    public boolean getAutoAnswer()
    {
        return autoAnswer;
    }

    public void setAutoAnswer(boolean autoAnswer)
    {
        this.autoAnswer = autoAnswer;
    }
    
    
}
