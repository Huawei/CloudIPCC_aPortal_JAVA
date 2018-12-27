package com.huawei.agentconsole.ws.rest;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import com.huawei.agentconsole.service.QueueDeviceService;


/**
 * <p>Title: 查询队列设备功能</p>
 * <p>Description: </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年8月2日
 * @since
 */
@Path("/queuedevice")
public class QueueDevice
{
	/**
	 * 获取所有语音技能队列的统计信息
	 * @param agentId
	 * @return Map<String, Object>
	 */
	@GET
	@Path("/voiceskillstatistic")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> getVoiceSkillStatistic(
			@QueryParam("agentId") String agentId)
	{
		QueueDeviceService service = new QueueDeviceService(agentId);
		Map<String, Object> result = service.getVoiceSkillInfo();
		return result;
	}
	
	/**
     * 获取指定VDN语音技能队列信息
     * @param agentId
     * @return Map<String, Object>
     */
    @GET
    @Path("/voiceskills")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getVoiceSkill(
            @QueryParam("agentId") String agentId)
    {
        QueueDeviceService service = new QueueDeviceService(agentId);
        Map<String, Object> result = service.getVdnVoiceSkills();
        return result;
    }


}
