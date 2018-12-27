package com.huawei.agentconsole.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.bean.SkillStatisticBean;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.http.AgentRequest;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;

/**
 * <p>
 * Title: 查询队列设备功能
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * <pre></pre>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Huawei Technologies Co.
 * </p>
 * 
 * @author y84103593
 * @version V1.0 2018年8月2日
 * @since
 */
public class QueueDeviceService
{
    private static final Logger LOG = LoggerFactory
            .getLogger(QueueDeviceService.class);

    private static final int MAX_BATCH_QUERY_SKILL = 90; // 批量查询技能统计数
    
    
    /**
     * BPOMS配置的默认技能队列名称, 该技能不应该显示
     */
    private static final String DEAULT_SKILL_NAME = "default";

    private String agentId;

    private AgentBaseInfoBean agentBaseInfoBean;

    public QueueDeviceService(String agentId)
    {
        this.agentId = agentId;
        agentBaseInfoBean = GlobalObject.getAgentBaseInfo(agentId);
    }

    /**
     * 获取指定VDN技能队列信息
     * 
     * @param vdnSkillParam
     * @return
     */
    private Map<String, Object> getVdnSkill()
    {
        if (null == agentBaseInfoBean)
        {
            LOG.error(LogUtils.AGENT_ID + "has logout",
                    LogUtils.encodeForLog(agentId));
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("retcode", AgentErrorCode.AGENT_NOT_LOGIN);
            return resultMap;
        }
        StringBuffer url = new StringBuffer();
        AgentBaseInfoBean agentBaseInfoBean = GlobalObject
                .getAgentBaseInfo(agentId);
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/queuedevice/").append(agentId);
        url.append("/vdnskill/").append(agentBaseInfoBean.getVdnId());
        return AgentRequest.get(agentId, url.toString());
    }

    /**
     * 获取指定VDN的语音技能队列信息
     * 
     * @return
     */
    public Map<String, Object> getVdnVoiceSkills()
    {
        Map<String, Object> allSkillResult = getVdnSkill();
        // 检查请求是否成功
        if (!AgentErrorCode.SUCCESS
                .equals(StringUtils.getRetCode(allSkillResult)))
        {
            LOG.error(
                    LogUtils.AGENT_ID
                            + "query the vdn skills failed, the error is {} ",
                    LogUtils.encodeForLog(agentId),
                    LogUtils.encodeForLog(allSkillResult));
            return allSkillResult;
        }
        // 获取请求返回的结果数据
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allSkillMap = (List<Map<String, Object>>) allSkillResult.get("result");
        if (null == allSkillMap || 0 == allSkillMap.size())
        {
            return allSkillResult;
        }
        // 筛选语音技能
        List<Map<String, Object>> voiceSkillList = new ArrayList<>();
        for (Map<String, Object> skillMap : allSkillMap)
        {
            if (null != skillMap.get("mediatype")
                    && CommonConstant.MEDIA_TYPE_VOICE
                            .equals(String.valueOf(skillMap.get("mediatype"))))
            {
                voiceSkillList.add(skillMap);
            }
        }
        allSkillResult.put("result", voiceSkillList);        
        return allSkillResult;
    }

    /**
     * 获取语音技能队列信息
     * 
     * @return
     */
    public Map<String, Object> getVoiceSkillInfo()
    {
        Map<String, Object> allSkillResult = getVdnSkill();
        if (!AgentErrorCode.SUCCESS
                .equals(StringUtils.getRetCode(allSkillResult)))
        {
            LOG.error(
                    LogUtils.AGENT_ID
                            + "query the vdn skills failed, the error is {} ",
                    LogUtils.encodeForLog(agentId),
                    LogUtils.encodeForLog(allSkillResult));
            return allSkillResult;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allSkillMap = (List<Map<String, Object>>) allSkillResult
                .get("result");
        if (null == allSkillMap || 0 == allSkillMap.size())
        {
            return allSkillResult;
        }

        List<String> skillIds = new ArrayList<String>();
        Map<String, Object> statisticMap;
        List<SkillStatisticBean> list = new ArrayList<SkillStatisticBean>();
        for (Map<String, Object> skillMap : allSkillMap)
        {
            if (null != skillMap.get("mediatype")
                    && CommonConstant.MEDIA_TYPE_VOICE
                            .equals(String.valueOf(skillMap.get("mediatype")))
                            && !DEAULT_SKILL_NAME.equalsIgnoreCase(String.valueOf(skillMap.get("name"))))
            {
                skillIds.add(String.valueOf(skillMap.get("id")));
                if (MAX_BATCH_QUERY_SKILL == skillIds.size())
                {
                    statisticMap = getSkillStatis(skillIds);
                    if (!AgentErrorCode.SUCCESS
                            .equals(StringUtils.getRetCode(statisticMap)))
                    {
                        LOG.error(LogUtils.AGENT_ID
                                + "query the skill statistic failed, the error is {} ",
                                LogUtils.encodeForLog(agentId),
                                LogUtils.encodeForLog(statisticMap));
                        return statisticMap;
                    }
                    parseSkillStatisMap(list, statisticMap);
                    skillIds = new ArrayList<String>();
                }
            }
        }
        if (0 != skillIds.size())
        {
            statisticMap = getSkillStatis(skillIds);
            if (!AgentErrorCode.SUCCESS
                    .equals(StringUtils.getRetCode(statisticMap)))
            {
                LOG.error(LogUtils.AGENT_ID
                        + "query the skill statistic failed, the error is {} ",
                        LogUtils.encodeForLog(agentId),
                        LogUtils.encodeForLog(statisticMap));
                return statisticMap;
            }
            parseSkillStatisMap(list, statisticMap);
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("retcode", AgentErrorCode.SUCCESS);
        result.put("result", list);
        return result;

    }

    /**
     * 解析技能队列统计信息的查询结果
     * 
     * @param list
     * @param statisticMap
     */
    private void parseSkillStatisMap(List<SkillStatisticBean> list,
            Map<String, Object> statisticMap)
    {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> allSkillMap = (List<Map<String, Object>>) statisticMap
                .get("result");
        if (null == allSkillMap || 0 == allSkillMap.size())
        {
            return;
        }
        SkillStatisticBean bean;
        for (Map<String, Object> skillMap : allSkillMap)
        {
            bean = new SkillStatisticBean();
            bean.setAvailAgents(String.valueOf(skillMap.get("availAgents")));
            bean.setLoggedOnAgents(
                    String.valueOf(skillMap.get("loggedOnAgents")));
            bean.setMaxQueueSize(String.valueOf(skillMap.get("maxQueueSize")));
            bean.setQueueSize(String.valueOf(skillMap.get("queueSize")));
            bean.setSkillId(String.valueOf(skillMap.get("deviceNo")));
            bean.setSkillName(String.valueOf(skillMap.get("skillDescrip")));
            list.add(bean);
        }
    }

    /**
     * 批量查询技能队列统计信息
     * 
     * @param skillIds
     * @return
     */
    private Map<String, Object> getSkillStatis(List<String> skillIds)
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/queuedevice/").append(agentId);
        url.append("/queryacdstat");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("skills", skillIds);
        return AgentRequest.post(agentId, url.toString(), resultMap);
    }
}
