
package com.huawei.agentconsole.bean;


import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.huawei.agentconsole.ws.param.EventParam;

/**
 * 
 * <p>Title: 座席鉴权信息 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月24日
 * @since
 */
public class AgentBaseInfoBean
{
    private String agentId;
    
    private String account;
    
    private String accessToken;
    
    private String guid;
    
    private String cookie;
    
    private String pushUrlToken;
    
    private String portalToken;
    
    /**
     * 心跳检测失败次数
     */
    private int failedHeartTimes = 0;
    
    private Queue<EventParam> eventQueue = new ConcurrentLinkedQueue<EventParam>();


    private int lastCallTimes = 0;

    
    private String vdnId;
    
    /**
     * 判断是否为质检员
     */
    private boolean isCensor;
    
    /**
     * 租户信息
     */
    private TenantInfo tenantInfo;
    
    /**
     * 试用被叫号码
     */
    private List<String> trialCalledList;

    /**
     * 主叫号码列表
     */
    private List<CallerNumber> callerNumbers;
    
   

    public AgentBaseInfoBean()
    {
        
    }
    public AgentBaseInfoBean(String agentId)
    {
        this.agentId = agentId;
    }
    
    public String getAgentId()
    {
        return agentId;
    }

    public String getGuid()
    {
        return guid;
    }
    
    public String getCookie()
    {
        return cookie;
    }

    public String getPushUrlToken()
    {
        return pushUrlToken;
    }

    public void setPushUrlToken(String pushUrlToken)
    {
        this.pushUrlToken = pushUrlToken;
    }

    public String getPortalToken()
    {
        return portalToken;
    }

    public void setPortalToken(String portalToken)
    {
        this.portalToken = portalToken;
    }

    public void setGuid(String guid)
    {
        this.guid = guid;
    }

    public void setCookie(String cookie)
    {
        this.cookie = cookie;
    }

    public Queue<EventParam> getEventQueue()
    {
        return eventQueue;
    }

    public void setEventQueue(Queue<EventParam> eventQueue)
    {
        this.eventQueue = eventQueue;
    }
    
    public void resetHeartTimes()
    {
        this.failedHeartTimes = 0;
    }
    
    public int addFailedHeartTimes()
    {
        this.failedHeartTimes++;
        return failedHeartTimes;
    }

    public int getLastCallTimes()
    {
        return lastCallTimes;
    }

    public void setLastCallTimes(int lastCallTimes)
    {
    	this.lastCallTimes = lastCallTimes;
    }

	public String getVdnId() {
		return vdnId;
	}

	public void setVdnId(String vdnId) {
		this.vdnId = vdnId;
	}

    public boolean isCensor()
    {
        return isCensor;
    }

    public void setCensor(boolean isCensor)
    {
        this.isCensor = isCensor;
    }

    /**
     * @return the account
     */
    public String getAccount()
    {
        return account;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(String account)
    {
        this.account = account;
    }

    /**
     * @return the accessToken
     */
    public String getAccessToken()
    {
        return accessToken;
    }

    /**
     * @param accessToken the accessToken to set
     */
    public void setAccessToken(String accessToken)
    {
        this.accessToken = accessToken;
    }

    /**
     * @param agentId the agentId to set
     */
    public void setAgentId(String agentId)
    {
        this.agentId = agentId;
    }
    
    public TenantInfo getTenantInfo()
    {
        return tenantInfo;
    }
    
    public void setTenantInfo(TenantInfo tenantInfo)
    {
        this.tenantInfo = tenantInfo;
    }
   
    
    public List<String> getTrialCalledList()
    {
        return trialCalledList;
    }
    public void setTrialCalledList(List<String> trialCalledList)
    {
        this.trialCalledList = trialCalledList;
    }
    public List<CallerNumber> getCallerNumbers()
    {
        return callerNumbers;
    }
    public void setCallerNumbers(List<CallerNumber> callerNumbers)
    {
        this.callerNumbers = callerNumbers;
    }
    

}
