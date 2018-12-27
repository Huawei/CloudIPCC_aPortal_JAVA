
package com.huawei.agentconsole.ws.exception;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.util.RestUtils;
import com.huawei.agentconsole.ws.param.RestResponse;

/**
 * 
 * <p>Title: URL不正确 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月23日
 * @since
 */
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException>
{

    @Override
    public Response toResponse(NotFoundException exception)
    {
        RestResponse responseBean = RestUtils.getErrorResult(AgentErrorCode.AGENT_REST_INVALID);
        responseBean.setMessage(exception.getMessage());
        Response response = Response.ok(responseBean.returnResult(),
                MediaType.APPLICATION_JSON).build();
        
        return response;
    }
}
