package com.huawei.agentconsole.service;

import java.util.Map;

import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.http.AgentRequest;
import com.huawei.agentconsole.ws.param.QualityControlParam;


/**
 * <p>Title: 实时质检</p>
 * <p>Description: </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年8月2日
 * @since
 */
public class QualityControlService
{
	
	
    private String agentId;
	
	public QualityControlService(String agentId)
	{
		this.agentId = agentId;
	}
	/**
	 * 插入
	 * @param qualityControlParam
	 * @return
	 */
	public Map<String, Object> addInsert(QualityControlParam qualityControlParam)
	{
		StringBuffer url = new StringBuffer();
		url.append(GlobalObject.getAgentServerUrl());
		url.append("/qualitycontrol/").append(agentId).append("/addinsert/");
		url.append(qualityControlParam.getWorkNo());

		if(CommonConstant.MEDIA_TYPE_TEXT.equals(qualityControlParam.getMediaType()))

		{
			url.append("?mediaType=1");
		}
		
		Map<String, Object> result = AgentRequest.put(agentId, url.toString(), null);
		
		return result;
	}
	
	/**
	 * 侦听
	 * @param qualityControlParam
	 * @return
	 */
	public Map<String, Object> addSupervise(QualityControlParam qualityControlParam)
	{
		StringBuffer url = new StringBuffer();
		url.append(GlobalObject.getAgentServerUrl());
		url.append("/qualitycontrol/").append(agentId).append("/addsupervise/");
		url.append(qualityControlParam.getWorkNo());

		if(CommonConstant.MEDIA_TYPE_TEXT.equals(qualityControlParam.getMediaType()))

		{
			url.append("?mediaType=1");
		}

		Map<String, Object> result = AgentRequest.put(agentId, url.toString(), null);
		
		return result;
	}
	
	/**
	 * 拦截
	 * @param qualityControlParam
	 * @return
	 */
	public Map<String, Object> intercept(QualityControlParam qualityControlParam)
	{
		StringBuffer url = new StringBuffer();
		url.append(GlobalObject.getAgentServerUrl());
		url.append("/qualitycontrol/").append(agentId).append("/intercept/");
		url.append(qualityControlParam.getWorkNo());

		if(CommonConstant.MEDIA_TYPE_TEXT.equals(qualityControlParam.getMediaType()))

		{
			url.append("?mediaType=1");
			url.append("&callId=").append(qualityControlParam.getCallId());
		}
		
		Map<String, Object> result = AgentRequest.put(agentId, url.toString(), null);
		
		return result;
	}
	
	/**
     * 取消侦听和插入
     * @return
     */
    public Map<String, Object> cancelAdd(QualityControlParam qualityControlParam)
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/qualitycontrol/").append(agentId).append("/");
        url.append(qualityControlParam.getWorkNo());

		if(CommonConstant.MEDIA_TYPE_TEXT.equals(qualityControlParam.getMediaType()))

		{
			url.append("?mediaType=1");
		}
		
        Map<String, Object> result = AgentRequest.delete(agentId, url.toString());
        
        return result; 
    }
    
    
}
