
package com.huawei.agentconsole.dao.intf;

import java.util.List;
import java.util.Map;


import com.huawei.agentconsole.bean.RecordInfoBean;

/**
 * 
 * <p>Title: 查询录音的dao层 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author l00357702
 * @version V1.0 2018年8月6日
 * @since
 */
public interface RecordDao
{
    List<RecordInfoBean> queryRecordInfo(Map<String,Object> param);
}
