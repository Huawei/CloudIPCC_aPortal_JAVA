
package com.huawei.agentconsole.ws.exception;



import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;

import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.util.RestUtils;
import com.huawei.agentconsole.ws.param.RestResponse;


@Provider
public class UnrecognizedPropertyExceptionMapper implements ExceptionMapper<UnrecognizedPropertyException>
{

    @Override
    public Response toResponse(UnrecognizedPropertyException exception)
    {
        RestResponse responseBean = RestUtils.getErrorResult(AgentErrorCode.AGENT_REST_INVALID);
        responseBean.setMessage("Unrecognized field: " + exception.getUnrecognizedPropertyName());
        Response response = Response.ok(responseBean.returnResult(),
                MediaType.APPLICATION_JSON).build();
        
        return response;
    }

}
