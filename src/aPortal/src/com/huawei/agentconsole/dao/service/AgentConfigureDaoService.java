package com.huawei.agentconsole.dao.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentConfigureInfoBean;
import com.huawei.agentconsole.bean.DBOperResult;
import com.huawei.agentconsole.common.exception.CommonException;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.dao.init.MybatisForMySql;
import com.huawei.agentconsole.dao.intf.saas.AgentConfigureDao;
import com.huawei.agentconsole.ws.param.AgentConfigureParam;

public class AgentConfigureDaoService
{
    private static final Logger LOG = LoggerFactory.getLogger(AgentConfigureDaoService.class);
    
    private String agentId;
    
    private int vdnId;
    
    private int ccId;
    
    public AgentConfigureDaoService(String agentId, int ccId, int vdnId)
    {
        this.agentId = agentId;
        this.vdnId = vdnId;
        this.ccId = ccId;
    }
    
    
    /**
     * @author l00467145
     * 查询当前agent的配置信息
     * @return
     */
    public DBOperResult<Map<String, AgentConfigureInfoBean>> queryAgentConfigure()
    {
        Map<String, AgentConfigureInfoBean> agentConfigure = new HashMap<String, AgentConfigureInfoBean>();
        SqlSessionFactory factory = MybatisForMySql.getSYSDBSqlSessionFactory();       
        if (null == factory)
        {
            return new DBOperResult<Map<String, AgentConfigureInfoBean>>(agentConfigure, false);
        }
        SqlSession sqlSession = null; 
        
        try
        {
            try
            {
                sqlSession = factory.openSession();
                AgentConfigureDao dao = sqlSession.getMapper(AgentConfigureDao.class);
                agentConfigure = dao.queryAgentConfigure(ccId, vdnId, agentId);
                return new DBOperResult<Map<String, AgentConfigureInfoBean>>(agentConfigure, true);
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
            LOG.error(LogUtils.AGENT_ID + "queryAgentConfigure from db failed. The exception is \r\n {}", 
                    LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
            return new DBOperResult<Map<String, AgentConfigureInfoBean>>(agentConfigure, false);
        }
        finally 
        {
            if (null != sqlSession)
            {
                sqlSession.close();
            }
        }
    }
    
    /**
     * @author l00467145
     * 修改当前agent的配置信息
     * @return
     */
    public DBOperResult<Integer> setAgentConfigure(AgentConfigureParam agentConfigureParam)
    {
        
        SqlSessionFactory factory = MybatisForMySql.getSYSDBSqlSessionFactory();       
        if (null == factory)
        {
            return new DBOperResult<Integer>(-1, false);
        }
        SqlSession sqlSession = null; 
        
        try
        {
            try
            {
                sqlSession = factory.openSession();
                AgentConfigureDao dao = sqlSession.getMapper(AgentConfigureDao.class);
                dao.insertAgentConfigure(ccId, vdnId, agentId, CommonConstant.PROPKEY_AUTOANSWER, agentConfigureParam.getIsAutoAnswer());
                dao.insertAgentConfigure(ccId, vdnId, agentId, CommonConstant.PROPKEY_MAXWORKTIME, agentConfigureParam.getMaxWorkTime());
                dao.insertAgentConfigure(ccId, vdnId, agentId, CommonConstant.PROPKEY_OUTCALLERNO, agentConfigureParam.getOutCallerNo());
                sqlSession.commit();
                
                return new DBOperResult<Integer>(0, true);
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
            if (null != sqlSession)
            {
                sqlSession.rollback();
            }
            LOG.error(LogUtils.AGENT_ID + "setAgentConfigure from db failed. The exception is \r\n {}", 
                    LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
            return new DBOperResult<Integer>(-1, false);
        }
        finally 
        {
            if (null != sqlSession)
            {
                sqlSession.close();
            }
        }
        
    }
}
