package com.huawei.agentconsole.ws.rest;

import java.util.Map;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.service.QualityControlService;
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
@Path("/qualitycontrol")
public class QualityControl
{
    private static final Logger LOG = LoggerFactory.getLogger(QualityControl.class);
	
	/**
	 * 插入
	 * @param agentId
	 * @param qualityControlParam
	 * @return Map<String, Object>
	 */
	@POST
	@Path("/addinsert")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> addInsert(
			@QueryParam("agentId") String agentId, 
			@Valid QualityControlParam qualityControlParam)
	{

		LOG.info(LogUtils.METHOD_IN + ",qualityControlParam:{}", 
				 LogUtils.encodeForLog(agentId), qualityControlParam);
		QualityControlService service = new QualityControlService(agentId);
		Map<String, Object> result = service.addInsert(qualityControlParam);
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}
	
	/**
	 * 侦听
	 * @param agentId
	 * @param qualityControlParam
	 * @return Map<String, Object>
	 */
	@POST
	@Path("/addsupervise")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> addSupervise(
			@QueryParam("agentId") String agentId, 
			@Valid QualityControlParam qualityControlParam)
	{

		LOG.info(LogUtils.METHOD_IN + ",qualityControlParam:{}", 
				 LogUtils.encodeForLog(agentId), qualityControlParam);
		QualityControlService service = new QualityControlService(agentId);
		Map<String, Object> result = service.addSupervise(qualityControlParam);
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}
	
	/**
	 * 拦截
	 * @param agentId
	 * @param qualityControlParam
	 * @return Map<String, Object>
	 */
	@POST
	@Path("/intercept")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> intercept(
			@QueryParam("agentId") String agentId, 
			@Valid QualityControlParam qualityControlParam)
	{

		LOG.info(LogUtils.METHOD_IN + ",qualityControlParam:{}", 
				 LogUtils.encodeForLog(agentId), qualityControlParam);
		QualityControlService service = new QualityControlService(agentId);
		Map<String, Object> result = service.intercept(qualityControlParam);
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}
	
	/**
	 * 取消侦听和插入
	 * @param agentId
	 * @param qualityControlParam
	 * @return Map<String, Object>
	 */
	@DELETE
	@Path("/canceladd")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> cancelAdd(
			@QueryParam("agentId") String agentId, 
			@Valid QualityControlParam qualityControlParam)
	{

		LOG.info(LogUtils.METHOD_IN + ",qualityControlParam:{}", 
				 LogUtils.encodeForLog(agentId), qualityControlParam);
		QualityControlService service = new QualityControlService(agentId);
		Map<String, Object> result = service.cancelAdd(qualityControlParam);
		LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
		return result;
	}
	
}
