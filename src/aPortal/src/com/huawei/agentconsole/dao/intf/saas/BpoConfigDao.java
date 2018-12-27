
package com.huawei.agentconsole.dao.intf.saas;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.huawei.agentconsole.bean.CallerNumber;
import com.huawei.agentconsole.bean.TenantInfo;

/**
 * 
 * <p>Title:获取t_saas_bpo_XXX的数据  </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年11月2日
 * @since
 */
public interface BpoConfigDao
{
    /**
     * 获取指定租户信息
     * @param ccId
     * @param vdnId
     * @return
     */
    public TenantInfo getTenantInfo(@Param("ccId")int ccId, @Param("vdnId")int vdnId);
    
    /**
     * 获取可以试呼的被叫号码
     * @param ccId
     * @param vdnId
     * @return
     */
    public List<String> getTrialCalledList(@Param("ccId")int ccId, @Param("vdnId")int vdnId);
    
    /**
     * 查询指定被叫号码的记录数
     * @param ccId
     * @param vdnId
     * @param phoneNumber
     * @return
     */
    public int getTrialCalledCountByPhoneNumber(@Param("ccId")int ccId, @Param("vdnId")int vdnId, @Param("phoneNumber")String phoneNumber);
    
    /**
     * 查询外呼号码列表
     * @return
     */
    public List<CallerNumber> queryCallerNumbers(@Param("ccId") int ccId, @Param("vdnId")int vdnId);
}
