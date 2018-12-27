
package com.huawei.agentconsole.bean;

import com.huawei.agentconsole.common.util.LogUtils;

/**
 * 
 * <p>Title: 主叫号码  </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年9月20日
 * @since
 */
public class CallerNumber
{
    
    private String remark;
    
    private String phoneNumber;
    

    public String getRemark()
    {
        return remark;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    
    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CallerNumber {'remark ': '");
        builder.append(remark);
        builder.append("', phoneNumber ': '");
        builder.append(LogUtils.formatPhoneNumber(phoneNumber));
        builder.append("}");
        return builder.toString();
    }
    
    

}
