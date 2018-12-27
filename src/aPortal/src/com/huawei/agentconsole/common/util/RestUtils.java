
package com.huawei.agentconsole.common.util;

import com.huawei.agentconsole.ws.param.RestResponse;

public class RestUtils
{
    public static RestResponse getErrorResult(String retcode)
    {
        RestResponse rs = new RestResponse();
        rs.setReturnCode(retcode);
        rs.setMessage("");
        return rs;
    }
   
    public static RestResponse getErrorResult(String retcode, String message)
    {
        RestResponse rs = new RestResponse();
        rs.setReturnCode(retcode);
        rs.setMessage(message);
        return rs;
    }
    
    
}
