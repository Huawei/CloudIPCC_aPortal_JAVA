
package com.huawei.agentconsole.ws.rest;

import java.util.Map;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.service.BpoConfigService;
import com.huawei.agentconsole.service.VoiceCallService;
import com.huawei.agentconsole.ws.param.DisconnectNumberParam;
import com.huawei.agentconsole.ws.param.DropCallParam;
import com.huawei.agentconsole.ws.param.GetHoldParam;
import com.huawei.agentconsole.ws.param.InnerCallParam;
import com.huawei.agentconsole.ws.param.InnerHelpParam;
import com.huawei.agentconsole.ws.param.OutCallParam;
import com.huawei.agentconsole.ws.param.RestResponse;
import com.huawei.agentconsole.ws.param.SecondDialParam;
import com.huawei.agentconsole.ws.param.TransferParam;

/**
 * <p>Title: 语音通话服务  </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年7月30日
 * @since
 */
@Path("/voicecall")
public class VoiceCall
{
	private static final Logger LOG = LoggerFactory.getLogger(VoiceCall.class);

	/**
	 * 外呼接口
	 * @param agentId
	 * @param outCallParam
	 * @return Map<String, Object>
	 */
	@POST
	@Path("/callout")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> callOut(
			@QueryParam("agentId") String agentId, 
			@Valid OutCallParam outCallParam)
	{
		LOG.info(LogUtils.METHOD_IN + ",voiceCallParam:{}", LogUtils.encodeForLog(agentId), outCallParam);
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.callOut(outCallParam);
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 内部呼叫接口
	 * @param agentId
	 * @param innerCallParam
	 * @return Map<String, Object>
	 */
	@POST
	@Path("/callinner")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> callInner(
			@QueryParam("agentId") String agentId, 
			@Valid InnerCallParam innerCallParam)
	{
		LOG.info(LogUtils.METHOD_IN + ",innerCallParam:{}", LogUtils.encodeForLog(agentId), innerCallParam);
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.callInner(innerCallParam);
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 呼叫应答接口
	 * @param agentId
	 * @return Map<String, Object>
	 */
	@PUT
	@Path("/answer")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> answer(@QueryParam("agentId") String agentId)
	{
		LOG.info(LogUtils.METHOD_IN, LogUtils.encodeForLog(agentId));
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.answer();
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 内部求助接口
	 * @param agentId
	 * @param innerHelpParam
	 * @return Map<String, Object>
	 */
	@PUT
	@Path("/innerhelp")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> innerHelp(
			@QueryParam("agentId") String agentId,
			@Valid InnerHelpParam innerHelpParam)
	{
		LOG.info(LogUtils.METHOD_IN + ",innerCallParam:{}", LogUtils.encodeForLog(agentId), innerHelpParam);
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.innerHelp(innerHelpParam);
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 静音接口
	 * @param agentId
	 * @return Map<String, Object>
	 */
	@PUT
	@Path("/beginmute")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> beginMute(@QueryParam("agentId") String agentId)
	{
		LOG.info(LogUtils.METHOD_IN, LogUtils.encodeForLog(agentId));
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.beginMute();
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 取消静音接口
	 * @param agentId
	 * @return Map<String, Object>
	 */
	@PUT
	@Path("/endmute")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> endMute(@QueryParam("agentId") String agentId)
	{
		LOG.info(LogUtils.METHOD_IN, LogUtils.encodeForLog(agentId));
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.endMute();
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 保持接口
	 * @param agentId
	 * @return Map<String, Object>
	 */
	@PUT
	@Path("/hold")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> hold(@QueryParam("agentId") String agentId)
	{
		LOG.info(LogUtils.METHOD_IN, LogUtils.encodeForLog(agentId));
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.hold();
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 取保持接口
	 * @param agentId
	 * @param getHoldParam
	 * @return Map<String, Object>
	 */
	@PUT
	@Path("/gethold")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> getHold(
			@QueryParam("agentId") String agentId, 
			@Valid GetHoldParam getHoldParam)
	{
		LOG.info(LogUtils.METHOD_IN + ",getHoldParam:{}", LogUtils.encodeForLog(agentId), getHoldParam);
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.getHold(getHoldParam);
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 呼叫转移接口
	 * @param agentId      
	 * @param transferParam
	 * @return Map<String, Object>
	 */
	@PUT
	@Path("/transfer")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> transfer(
			@QueryParam("agentId") String agentId, 
			@Valid TransferParam transferParam)
	{
		LOG.info(LogUtils.METHOD_IN + ",transferParam:{}", LogUtils.encodeForLog(agentId), transferParam);
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.transfer(transferParam);
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 取消转移接口
	 * @param agentId    
	 * @return Map<String, Object>
	 */
	@PUT
	@Path("/canceltransfer")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> cancelTransfer(@QueryParam("agentId") String agentId)
	{
		LOG.info(LogUtils.METHOD_IN, LogUtils.encodeForLog(agentId));
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.cancelTransfer();
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 二次拨号接口
	 * @param agentId
	 * @param secondDialeParam
	 * @return Map<String, Object>
	 */
	@PUT
	@Path("/seconddial")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> secondDial(
			@QueryParam("agentId") String agentId,
			@Valid SecondDialParam secondDialParam)
	{
		LOG.info(LogUtils.METHOD_IN, LogUtils.encodeForLog(agentId));
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.secondDial(secondDialParam);
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 拆除指定callid呼叫接口
	 * @param agentId
	 * @param dropCallParam
	 * @return Map<String, Object>
	 */
	@PUT
	@Path("/dropcall")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> dropCall(
			@QueryParam("agentId") String agentId, 
			@Valid DropCallParam dropCallParam)
	{
		LOG.info(LogUtils.METHOD_IN + ",dropCallParam:{}", LogUtils.encodeForLog(agentId), dropCallParam);
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.dropCall(dropCallParam);
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}

	/**
	 * 通话挂断接口
	 * @param agentId
	 * @return Map<String, Object>
	 */
	@PUT
	@Path("/release")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> release(@QueryParam("agentId") String agentId)
	{
		LOG.info(LogUtils.METHOD_IN, LogUtils.encodeForLog(agentId));
		VoiceCallService service = new VoiceCallService(agentId);
		Map<String, Object> result = service.release();
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}
	
	/**
	 * 释放指定号码或座席的呼叫
	 * @param agentId
	 * @param disconnectNumberParam
	 * @return
	 */
	@PUT
    @Path("/disconnect")
    @Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> disconnect(@QueryParam("agentId") String agentId,
	        @Valid DisconnectNumberParam disconnectNumberParam)
	{
	    LOG.info(LogUtils.METHOD_IN + ",disconnectNumberParam:{}",
	            LogUtils.encodeForLog(agentId), disconnectNumberParam);
        VoiceCallService service = new VoiceCallService(agentId);
        Map<String, Object> result = service.disconnect(disconnectNumberParam);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
        return result;
	}
	
	/**
	 * 获取试用被叫号码
	 * @param agentId
	 * @return
	 */
	@GET
	@Path("/trialcalled")
    @Produces(MediaType.APPLICATION_JSON)
	@NoCache
    public Map<String, Object> getTrialCalled(@QueryParam("agentId") String agentId)
    {
        BpoConfigService service = new BpoConfigService(agentId);
        RestResponse result = service.getTrialCalledNumber();
        return result.returnResult();
    }

}
