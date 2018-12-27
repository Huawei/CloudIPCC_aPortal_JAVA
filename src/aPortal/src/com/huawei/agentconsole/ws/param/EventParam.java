
package com.huawei.agentconsole.ws.param;


import javax.validation.constraints.Size;


/**
 * 
 * <p>Title:  AgentServer推送的事件消息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月25日
 * @since
 */
public class EventParam
{
    @Size(max = 10)
    private String message;
    
    @Size(max = 10)
    private String retcode;
    
    private EventContentParam event;

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getRetcode()
    {
        return retcode;
    }

    public void setRetcode(String retcode)
    {
        this.retcode = retcode;
    }

    public EventContentParam getEvent()
    {
        return event;
    }

    public void setEvent(EventContentParam event)
    {
        this.event = event;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("event:").append(event);
        sb.append('}');
        return sb.toString();
    } 
    
}
