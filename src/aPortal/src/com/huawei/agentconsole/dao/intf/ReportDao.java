package com.huawei.agentconsole.dao.intf;

import java.util.HashMap;
import java.util.List;

import com.huawei.agentconsole.bean.AgentCallOutBriefBean;
import com.huawei.agentconsole.bean.AgentTraffic;
import com.huawei.agentconsole.bean.AgentWork;
import com.huawei.agentconsole.bean.SkillTraffic;
import com.huawei.agentconsole.bean.VdnTraffic;

public interface ReportDao
{

    List<SkillTraffic> querySkillTraffic(HashMap<String, Object> hashMap);

    List<VdnTraffic> queryVdntraffic(HashMap<String, Object> hashMap);
    
    List<AgentTraffic> queryAgentTraffic(HashMap<String, Object> hashMap);
    
    List<AgentWork> queryAgentWork(HashMap<String, Object> queryParamMap);
    
    List<AgentCallOutBriefBean> queryAgentCallOutBrief(HashMap<String, Object> hashMap);

}
