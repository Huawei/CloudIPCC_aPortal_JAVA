
package com.huawei.agentconsole.bean;

import java.util.Date;

/**
 * 
 * <p>Title:  租户信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年10月10日
 * @since
 */
public class TenantInfo
{   
    /**
     * 租户创建时间
     */
    private Date createTime;
    
    /**
     * 是否试用， 1表示试用
     */
    private int trial;

    public Date getCreateTime()
    {
        return (null == createTime) ? null : (Date) createTime.clone();
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = (null == createTime) ? null : (Date) createTime.clone();
    }

    public int getTrial()
    {
        return trial;
    }

    public void setTrial(int trial)
    {
        this.trial = trial;
    }
    
}
