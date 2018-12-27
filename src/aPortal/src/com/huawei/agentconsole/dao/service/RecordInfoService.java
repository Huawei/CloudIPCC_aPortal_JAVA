
package com.huawei.agentconsole.dao.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.RecordInfoBean;
import com.huawei.agentconsole.common.exception.CommonException;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.dao.init.Mybatis;
import com.huawei.agentconsole.dao.intf.RecordDao;
import com.huawei.agentconsole.ws.param.PageParam;
import com.huawei.agentconsole.ws.param.RequestParamForRecordBean;

public class RecordInfoService
{

    private static final Logger log = LoggerFactory
            .getLogger(RecordInfoService.class);
    
    private static RecordInfoService instance = new RecordInfoService();
    
    public RecordInfoService()
    {
        
    }
    
    public static RecordInfoService getInstance()
    {
        return instance;
    }
    
    /**
     * 查询数据库录音记录，暂时以 map形式返回，可以存储多个键值对
     * @param page
     * @param searchParam
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String,Object> queryRecordInfo(PageParam page,RequestParamForRecordBean searchParam)
    {
        SqlSessionFactory factory = Mybatis.getUIDBSqlSessionFactory();        
        Map<String,Object> returnResult = new HashMap<String,Object>();
        List<RecordInfoBean> result = null;
        if (null == factory)
        {
            result = new ArrayList<RecordInfoBean>();
            returnResult.put("result", result);
            return returnResult;
        }
        SqlSession sqlSession = null;        
        try
        {
            try
            {
                sqlSession = factory.openSession();
                
                //此处主要为存储过程参数赋值
                Map<String,Object> param = new HashMap<String,Object>();
                
                param.put("i_beginDate", searchParam.getBegin());
                
                param.put("i_endDate", searchParam.getEnd());
                
                param.put("i_pageSize", page.getPageSize());
                
                param.put("io_pageNo", page.getCurPage());
                
                param.put("i_ccId", searchParam.getCcId());
                
                param.put("i_vdnId", searchParam.getVdnId());
                
                param.put("i_callerno", searchParam.getCaller());
                
                param.put("i_calleeno", searchParam.getCalled());
                
                param.put("i_agentIds", searchParam.getAgentId());
                
                RecordDao dao = sqlSession.getMapper(RecordDao.class);
                
                result = dao.queryRecordInfo(param);
                
                if (null != param.get("o_cursor"))
                {
                    result = (List<RecordInfoBean>)param.get("o_cursor");
                }
                
                if (null != param.get("o_totalCount"))
                {
                    String totalCounts = param.get("o_totalCount").toString();
                    returnResult.put("totalRows", totalCounts);
                }
            }
            catch (RuntimeException e)
            {
                throw new CommonException(e);
            }
            catch (Exception e)
            {
                throw new CommonException(e);
            }
            
            
        }
        catch (CommonException e) 
        {
            log.error("Db checked failed! The error is {}", 
                    LogUtils.encodeForLog(e.getMessage()));
        }
        finally 
        {
            if (null != sqlSession)
            {
                sqlSession.close();
            }
        }
        
        if (null == result)
        {
            result = new ArrayList<RecordInfoBean>();
        }
        
        returnResult.put("result", result);
        return returnResult;
    } 
}
