package com.huawei.agentconsole.bean;

import java.util.Map;

public class AgentConfigureInfoBean
{
    private int ccId;
    
    private int vdnId;
    
    private int agentId;
    
    private String propType;
    
    private String propValue;
    
    private Map<String, Object> propConfigure;

    public int getCcId()
    {
        return ccId;
    }

    public void setCcId(int ccId)
    {
        this.ccId = ccId;
    }

    public int getVdnId()
    {
        return vdnId;
    }

    public void setVdnId(int vdnId)
    {
        this.vdnId = vdnId;
    }

    public int getAgentId()
    {
        return agentId;
    }

    public void setAgentId(int agentId)
    {
        this.agentId = agentId;
    }

    public Map<String, Object> getPropConfigure()
    {
        return propConfigure;
    }

    public void setPropConfigure(Map<String, Object> propConfigure)
    {
        this.propConfigure = propConfigure;
    }

    public String getPropType()
    {
        return propType;
    }

    public void setPropType(String propType)
    {
        this.propType = propType;
    }

    public String getPropValue()
    {
        return propValue;
    }

    public void setPropValue(String propValue)
    {
        this.propValue = propValue;
    }
    
}
