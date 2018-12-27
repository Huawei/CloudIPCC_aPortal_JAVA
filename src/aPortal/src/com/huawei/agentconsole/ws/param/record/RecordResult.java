
package com.huawei.agentconsole.ws.param.record;

import java.util.HashMap;
import java.util.Map;


public class RecordResult
{

    private Map<String, Object> retObjects = new HashMap<String, Object>();
    
    public void setRecordRetCode(String retCode)
    {
        this.retObjects.put("returnCode", retCode);
    }
    
    public String getRecordRetCode()
    {
        Object retcode = this.retObjects.get("returnCode");
        
        if (retcode == null)
        {
            return "";
        }
        else
        {
            return retcode.toString();
        }
    }
    
    public void setRetDesc(String retCode)
    {
        this.retObjects.put("returnDesc", retCode);
    }
    
    public String getRetDesc()
    {
        Object retdesc = this.retObjects.get("returnDesc");
        
        if (null == retdesc)
        {
            return "";
        }
        else
        {
            return retdesc.toString();
        }
    }
    
    /**
     * 设置自定义返回值
     * @param key        键，需要返回给客户端的某些自定义键
     * @param object     值，自定义键的值
     */
    public void setRetObject(String key, Object object)
    {
        this.retObjects.put(key, object);
    }
    
    /**
     * 获取自定义返回值
     * @param key   自定义键
     * @return   自定义键对应的值
     */
    public Object getRetObject(String key)
    {
        return this.retObjects.get(key);
    }
    
    /**
     * 获取最后的返回值
     * @return map形式的结果集
     */
    public Map<String, Object> returnResult()
    {
        return this.retObjects;
    }
}
