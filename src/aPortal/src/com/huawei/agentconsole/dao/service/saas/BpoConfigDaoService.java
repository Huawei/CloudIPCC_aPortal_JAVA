
package com.huawei.agentconsole.dao.service.saas;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huawei.agentconsole.bean.CallerNumber;
import com.huawei.agentconsole.bean.DBOperResult;
import com.huawei.agentconsole.bean.TenantInfo;
import com.huawei.agentconsole.common.exception.CommonException;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.dao.init.MybatisForMySql;
import com.huawei.agentconsole.dao.intf.saas.BpoConfigDao;




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
public class BpoConfigDaoService
{
    private static final Logger LOG = LoggerFactory.getLogger(BpoConfigDaoService.class);


    private String agentId;
    
    private int ccId;
    
    private int vdnId;
    
    public BpoConfigDaoService(String agentId, int ccId, int vdnId)
    {
        this.agentId = agentId;
        this.ccId = ccId;
        this.vdnId = vdnId;
    }
    
    /**
     * 获取租户信息
     * @return
     */
    public DBOperResult<TenantInfo> getTenantInfo()
    {
        SqlSessionFactory factory = MybatisForMySql.getSYSDBSqlSessionFactory();       
        if (null == factory)
        {
            return new DBOperResult<TenantInfo>(null, false);
        }
        SqlSession sqlSession = null;        
        try
        {
            try
            {
                sqlSession = factory.openSession();
                BpoConfigDao dao = sqlSession.getMapper(BpoConfigDao.class);
                TenantInfo tenantInfo = dao.getTenantInfo(ccId, vdnId);
                return new DBOperResult<TenantInfo>(tenantInfo, true);
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
            LOG.error(LogUtils.AGENT_ID + "getTenantInfo from db failed. The exception is \r\n {}", 
                    LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
            return new DBOperResult<TenantInfo>(null, false);
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
     * 获取可以试呼的被叫号码
     * @return
     */
    public DBOperResult<List<String>> getTrialCalledList()
    {
        SqlSessionFactory factory = MybatisForMySql.getSYSDBSqlSessionFactory();       
        if (null == factory)
        {
            return new DBOperResult<List<String>>(null, false);
        }
        SqlSession sqlSession = null;        
        try
        {
            try
            {
                sqlSession = factory.openSession();
                BpoConfigDao dao = sqlSession.getMapper(BpoConfigDao.class);
                List<String> calledList = dao.getTrialCalledList(ccId, vdnId);
                return new DBOperResult<List<String>>(calledList, true);
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
            LOG.error(LogUtils.AGENT_ID + "getCalledList from db failed. The exception is \r\n {}", 
                    LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
            return new DBOperResult<List<String>>(null, false);
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
     * 查询指定被叫号码的记录数
     * @return
     */
    public DBOperResult<Integer> getTrialCalledCountByPhoneNumber(String phoneNumber)
    {
        SqlSessionFactory factory = MybatisForMySql.getSYSDBSqlSessionFactory();       
        if (null == factory)
        {
            return new DBOperResult<Integer>(null, false);
        }
        SqlSession sqlSession = null;        
        try
        {
            try
            {
                sqlSession = factory.openSession();
                BpoConfigDao dao = sqlSession.getMapper(BpoConfigDao.class);
                int count = dao.getTrialCalledCountByPhoneNumber(ccId, vdnId, phoneNumber);
                return new DBOperResult<Integer>(count, true);
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
            LOG.error(LogUtils.AGENT_ID + "getCalledList from db failed. The exception is \r\n {}", 
                    LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
            return new DBOperResult<Integer>(null, false);
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
     * 查询当前VDN下的外呼号码列表
     * @return
     */
    public DBOperResult<List<CallerNumber>> queryCallerNumbers(){
        
        SqlSessionFactory factory = MybatisForMySql.getSYSDBSqlSessionFactory();       
        if (null == factory)
        {
            return new DBOperResult<List<CallerNumber>>(null, false);
        }
        SqlSession sqlSession = null; 
        
        try
        {
            try
            {
                sqlSession = factory.openSession();
                BpoConfigDao dao = sqlSession.getMapper(BpoConfigDao.class);
                List<CallerNumber> callerNumbers = dao.queryCallerNumbers(ccId, vdnId);
                return new DBOperResult<List<CallerNumber>>(callerNumbers, true);
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
            LOG.error(LogUtils.AGENT_ID + "queryCallerNumbers from db failed. The exception is \r\n {}", 
                    LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
            return new DBOperResult<List<CallerNumber>>(null, false);
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
