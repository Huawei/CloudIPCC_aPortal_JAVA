
package com.huawei.agentconsole.ws.param;

import java.util.Map;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import com.huawei.agentconsole.common.constant.ParamPatternConstant;
import com.huawei.agentconsole.common.util.LogUtils;

/**
 * 
 * <p>Title:  具体的事件内容</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月25日
 * @since
 */
public class EventContentParam
{
    @Size(max = 64)
    @NotBlank
    private String eventType;

    @Pattern(regexp = ParamPatternConstant.WORKNO_PATTERN)
    @NotBlank
    private String workNo;
    
    
    private Object content;

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getWorkNo()
    {
        return workNo;
    }

    public void setWorkNo(String workNo)
    {
        this.workNo = workNo;
    }

    public Object getContent()
    {
        return content;
    }

    public void setContent(Object content)
    {
        this.content = content;
    } 
    
    @SuppressWarnings("unchecked")
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("workNo:" + workNo).append(", ");
        sb.append("eventType:" + eventType).append(", ");
        if (content instanceof Map)
        {
            sb.append("content:" + LogUtils.formatMap((Map<String, Object>)content));
        }
        else 
        {
            sb.append("content:" + content);
        }
        sb.append('}');
        return sb.toString();
    }
}
