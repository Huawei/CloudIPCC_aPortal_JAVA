
package com.huawei.agentconsole.ws.exception;

import java.util.List;

import javax.validation.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ResteasyViolationException;

import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.util.RestUtils;
import com.huawei.agentconsole.ws.param.RestResponse;

@Provider
public class ResteasyViolationExceptionMapper implements ExceptionMapper<ValidationException>
{

    @Override
    public Response toResponse(ValidationException exception)
    {
        RestResponse responseBean = RestUtils.getErrorResult(AgentErrorCode.AGENT_REST_INVALID);
        responseBean.setMessage(exception.getMessage());
        if (exception instanceof ResteasyViolationException)
        {
            ResteasyViolationException resteasyViolationException = (ResteasyViolationException)ResteasyViolationException.class.cast(exception);
            List<ResteasyConstraintViolation> paramViolations =resteasyViolationException.getParameterViolations();
            if (paramViolations != null && !paramViolations.isEmpty())
            {
                StringBuilder sb = new StringBuilder();
                ResteasyConstraintViolation rcv = paramViolations.get(0);
                sb.append(rcv.getPath()).append(":").append(rcv.getMessage());
                responseBean.setMessage(sb.toString());
            }
            
        }
        Response response = Response.ok(responseBean.returnResult(),
                MediaType.APPLICATION_JSON).build();
        
        return response;
    }

}
