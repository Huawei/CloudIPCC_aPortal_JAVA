package com.huawei.agentconsole.bean;

/**
 * 
 * <p>Title:  技能队列统计信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年8月4日
 * @since
 */
public class SkillStatisticBean
{
	/**
	 * 技能队列id
	 */
	private String skillId;
	
	/**
	 * 技能名称
	 */
	private String skillName;
	
	/**
	 * 当前排队数
	 */
	private String queueSize;
	
	/**
	 * 当前签入座席数
	 */
	private String loggedOnAgents;
	
	/**
	 * 当前可用座席数
	 */
	private String availAgents;
	
	/**
	 * 队列最大可以排队人数
	 */
	private String maxQueueSize;

	public String getSkillId() {
		return skillId;
	}

	public void setSkillId(String skillId) {
		this.skillId = skillId;
	}

	public String getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(String queueSize) {
		this.queueSize = queueSize;
	}

	public String getLoggedOnAgents() {
		return loggedOnAgents;
	}

	public void setLoggedOnAgents(String loggedOnAgents) {
		this.loggedOnAgents = loggedOnAgents;
	}

	public String getAvailAgents() {
		return availAgents;
	}

	public void setAvailAgents(String availAgents) {
		this.availAgents = availAgents;
	}

	public String getMaxQueueSize() {
		return maxQueueSize;
	}

	public void setMaxQueueSize(String maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	public String getSkillName() {
		return skillName;
	}

	public void setSkillName(String skillName) {
		this.skillName = skillName;
	}

}
