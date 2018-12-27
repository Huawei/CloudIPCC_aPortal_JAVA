
package com.huawei.agentconsole.ws.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.codehaus.jackson.JsonParseException;

import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.util.RestUtils;
import com.huawei.agentconsole.ws.param.RestResponse;

public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

	@Override
	public Response toResponse(JsonParseException exception) {
		RestResponse responseBean = RestUtils.getErrorResult(AgentErrorCode.AGENT_REST_INVALID);
		responseBean.setMessage(exception.getMessage());
		Response response = Response.ok(responseBean.returnResult(),
	                MediaType.APPLICATION_JSON).build();
	        
	     return response;
	}

}
