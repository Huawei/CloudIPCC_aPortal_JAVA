package com.huawei.agentconsole.service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.bean.CallerNumber;
import com.huawei.agentconsole.bean.TenantInfo;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.http.AgentRequest;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.RestUtils;
import com.huawei.agentconsole.common.util.StringUtils;
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
 * 
 * <p>Title: 语音通话服务 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年7月30日
 * @since
 */
public class VoiceCallService
{
	private static final Logger LOG = LoggerFactory.getLogger(VoiceCallService.class);
	
	private String agentId;
	
	private AgentBaseInfoBean agentBaseInfoBean;
	
	public VoiceCallService(String agentId)
	{
		this.agentId = agentId;
		this.agentBaseInfoBean = GlobalObject.getAgentBaseInfo(agentId);
	}
	
	/**
	 * 外呼
	 * @param outCallParam
	 * @return
	 */
	public Map<String, Object> callOut(OutCallParam outCallParam)
	{
		StringBuffer url = new StringBuffer();
		url.append(GlobalObject.getAgentServerUrl());
		url.append("/voicecall/").append(agentId).append("/callout");
		
		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("called", outCallParam.getCalled());
		if (!calledIsValid(outCallParam.getCalled()))
		{
		    RestResponse restResponse = new RestResponse();
            restResponse.setReturnCode(AgentErrorCode.CALLED_ISINVALID);
            restResponse.setMessage("called is invalid");
            return restResponse.returnResult();
		}
		
		if (!StringUtils.isNullOrBlank(outCallParam.getCaller()))
		{
		    //设置了主叫号码，需要读取VDNID
		    if (!checkCaller(outCallParam.getCaller()))
		    {
		        //主叫号码不合法
		        RestResponse restResponse = new RestResponse();
	            restResponse.setReturnCode(AgentErrorCode.CALLER_ISINVALID);
	            restResponse.setMessage("caller is invalid");
	            return restResponse.returnResult();
		    }
		    requestMap.put("caller", outCallParam.getCaller());
		}
		return AgentRequest.put(agentId, url.toString(), requestMap);
	}
	
	/**
	 * 检查被叫号码是否合法
	 * @param called
	 * @return
	 */
	private boolean calledIsValid(String called)
	{
	    TenantInfo tenantInfo = agentBaseInfoBean.getTenantInfo();
	    if (null == tenantInfo)
	    {
	        //没有租户信息，则不校验外呼被叫号码，防止外呼不能使用
	        return true;
	    }
	    
	    if (tenantInfo.getTrial() == CommonConstant.TRIAL_TENANT)
	    {
	        //试用租户需要校验被叫号码
	        List<String> calledList = agentBaseInfoBean.getTrialCalledList();
	        if (null != calledList && calledList.contains(called))
	        {
	            //是试用的被叫号码
	            return true;
	        }
	        return false;
	    }
	    else
	    {
	        //商用租户不要校验被叫号码
	        return true;
	    }
	}
	
	
	/**
	 * 检查主叫号码是否合法
	 * @param caller
	 * @return
	 */
	private boolean checkCaller(String caller)
	{
	    //获取当前可用的主叫号码
	    List<CallerNumber> list = agentBaseInfoBean.getCallerNumbers();
	    if (null == list || list.isEmpty())
	    {
	        return false;
	    }
	    for (CallerNumber callerNumber : list)
	    {
	        if (caller.equals(callerNumber.getPhoneNumber()))
	        {
	            return true;
	        }
	    }
	    return false;
	}
	
	/**
	 * 内部呼叫
	 * @param innerCallParam
	 * @return
	 */
	public Map<String, Object> callInner(InnerCallParam innerCallParam)
	{
		StringBuffer url = new StringBuffer();
		url.append(GlobalObject.getAgentServerUrl());
		url.append("/voicecall/").append(agentId).append("/callinner");
		
		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("called", innerCallParam.getCalled());
		
		return AgentRequest.put(agentId, url.toString(), requestMap);
	}
	
	
    /**
     * 应答
     * @return
     */
    public Map<String, Object> answer()
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/voicecall/").append(agentId).append("/answer");
        return AgentRequest.put(agentId, url.toString(), null);
    }
    
    /**
	 * 内部求助
	 * @param innerHelpParam
	 * @return
	 */
	public Map<String, Object> innerHelp(InnerHelpParam innerHelpParam)
	{
		StringBuffer url = new StringBuffer();
		url.append(GlobalObject.getAgentServerUrl());
		url.append("/voicecall/").append(agentId).append("/innerhelp");
		
		Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("dstaddress", innerHelpParam.getDstAddress());
		requestMap.put("devicetype", innerHelpParam.getDeviceType());
		requestMap.put("mode", innerHelpParam.getMode());
		return AgentRequest.post(agentId, url.toString(), requestMap);
	}
	
    /**
     * 静音
     * @return
     */
    public Map<String, Object> beginMute()
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/voicecall/").append(agentId).append("/beginmute");
        return AgentRequest.post(agentId, url.toString(), null);
    }
    
    /**
     * 取消静音
     * @return
     */
    public Map<String, Object> endMute()
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/voicecall/").append(agentId).append("/endmute");
        return AgentRequest.post(agentId, url.toString(), null);
    }
    
   
    /**
     * 保持
     * @return
     */
    public Map<String, Object> hold()
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/voicecall/").append(agentId).append("/hold");
        return AgentRequest.post(agentId, url.toString(), null);
    }
    
   
    /**
     * 取保持
     * @param getHoldParam
     * @return
     */
    public Map<String, Object> getHold(GetHoldParam getHoldParam)
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/voicecall/").append(agentId);
        url.append("/gethold?callid=").append(getHoldParam.getCallId());
        return AgentRequest.post(agentId, url.toString(), null);
    }
    
    
    /**
     * 呼叫转移
     * @param transferParam
     * @return
     */
    public Map<String, Object> transfer(TransferParam transferParam)
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/voicecall/").append(agentId);
        url.append("/transfer");
        
        Map<String, Object> requestMap = new HashMap<String, Object>();
		requestMap.put("devicetype", transferParam.getDeviceType());
		requestMap.put("address", transferParam.getAddress());
		requestMap.put("mode", transferParam.getMode());
		
		return AgentRequest.post(agentId, url.toString(), requestMap);
    }
    
    /**
     * 取消转移
     * @return
     */
    public Map<String, Object> cancelTransfer()
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/voicecall/").append(agentId).append("/canceltransfer");
        return AgentRequest.post(agentId, url.toString(), null);
    }
    
   
    /**
     * 二次拨号
     * @param secondDialParam
     * @return
     */
    public Map<String, Object> secondDial(SecondDialParam secondDialParam)
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/voicecall/").append(agentId);
        try {
			url.append("/seconddialex?number=").append(java.net.URLEncoder.encode(secondDialParam.getNumber(), CommonConstant.UTF_8));
		} catch (UnsupportedEncodingException e) {
			LOG.error(LogUtils.AGENT_ID + " encode the secondnumber failed, the error is \r\n {}", LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
			RestResponse responseBean = RestUtils.getErrorResult(AgentErrorCode.AGENT_REST_INVALID);
			responseBean.setMessage("encode the secondnumber failed");
			return responseBean.returnResult();
		}
        return AgentRequest.post(agentId, url.toString(), null);
    }
    
    /**
     * 拆除指定callid呼叫
     * @param dropCallParam
     * @return
     */
    public Map<String, Object> dropCall(DropCallParam dropCallParam)
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/voicecall/").append(agentId);
        url.append("/dropcall/").append(dropCallParam.getCallId());
        return AgentRequest.post(agentId, url.toString(), null);
    }
    
	/**
     * 挂断
     * @return
     */
    public Map<String, Object> release()
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/voicecall/").append(agentId).append("/release");
        return AgentRequest.delete(agentId, url.toString());
    }

    /**
     * 挂断
     * @return
     */
    public Map<String, Object> disconnect(DisconnectNumberParam disconnectNumberParam)
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/voicecall/").append(agentId).append("/disconnect/").append(disconnectNumberParam.getNumber());
        return AgentRequest.post(agentId, url.toString(), null);
    }
}
