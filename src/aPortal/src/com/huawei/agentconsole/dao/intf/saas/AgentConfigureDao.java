package com.huawei.agentconsole.dao.intf.saas;

import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import com.huawei.agentconsole.bean.AgentConfigureInfoBean;

public interface AgentConfigureDao
{
    /**
     * 查询坐席个人配置
     * @return
     */
    @MapKey("propType")
    Map<String, AgentConfigureInfoBean> queryAgentConfigure(@Param("ccId") int ccId, @Param("vdnId") int vdnId, @Param("agentId") String agentId);
    
    /**
     * 保存坐席个人配置
     * @return
     */
    int insertAgentConfigure(@Param("ccId") int ccId, @Param("vdnId") int vdnId, @Param("agentId") String agentId, @Param("propType") String propType, @Param("propValue") String propValue);
}
