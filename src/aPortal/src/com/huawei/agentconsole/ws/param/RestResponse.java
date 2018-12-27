
package com.huawei.agentconsole.ws.param;

import java.util.HashMap;
import java.util.Map;

public class RestResponse
{ /**
     * 结果集，MAP中"retcode"定义结果码，其余键值可以自定义
     */
    private Map<String, Object> retObjects = new HashMap<String, Object>();
    
    /**
     * 设置结果码
     * @param retcode  结果码
     */
    public void setReturnCode(String retcode)
    {
        this.retObjects.put("retcode", retcode);
    }
    
    /**
     * 获取结果码
     * @return  结果码
     */
    public String getReturnCode()
    {
        Object retcode = this.retObjects.get("retcode");
        
        if (retcode == null)
        {
            return "";
        }
        else
        {
            return retcode.toString();
        }
    }
    
    /**
     * 设置返回信息
     * @param message 返回信息
     */
    public void setMessage(String message)
    {
        this.retObjects.put("message", message);
    }
    
    /**
     * 获取返回信息
     * @return 返回信息
     */
    public String getMessage()
    {
        Object retcode = this.retObjects.get("message");
        
        if (retcode == null)
        {
            return "";
        }
        else
        {
            return retcode.toString();
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
     * 获取最后返回值
     * @return  结果集， MAP形式，框驾后续会封装成JSON
     */
    public Map<String, Object> returnResult()
    {
        return this.retObjects;
    }
   
}
