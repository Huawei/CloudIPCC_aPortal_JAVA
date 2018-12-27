package com.huawei.agentconsole.dao.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentCallOutBriefBean;
import com.huawei.agentconsole.bean.AgentTraffic;
import com.huawei.agentconsole.bean.AgentWork;
import com.huawei.agentconsole.bean.SkillTraffic;
import com.huawei.agentconsole.bean.VdnTraffic;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.dao.init.Mybatis;
import com.huawei.agentconsole.dao.intf.ReportDao;
import com.huawei.agentconsole.ws.param.RestResponse;

public class ReportDaoService {
    
    private static final Logger LOG = LoggerFactory.getLogger(ReportDaoService.class);
    
    private static ReportDaoService instance = new ReportDaoService();
    
    ReportDaoService()
    {
        
    }
    
    public static ReportDaoService getInstance()
    {
        return instance;
    }

    /**
     * VDN报表查询
     * @param HashMap<String, Object> vdn报表查询结果参数
     * @param agentId 座席工号
     * @return RestResponse 返回结果
     */
    @SuppressWarnings("unchecked")
    public RestResponse queryVdnTraffic(HashMap<String, Object> hashMap, String agentId)
    {
        RestResponse restResponse = new RestResponse();
        SqlSessionFactory factory = Mybatis.getUIDBSqlSessionFactory();
        ArrayList<VdnTraffic> list = new ArrayList<VdnTraffic>();
        if (null == factory)
        {
            LOG.error(LogUtils.AGENT_ID + "sqlSessionFactory is not init.", LogUtils.encodeForLog(agentId));
            restResponse = makeResponse(restResponse, "SqlSessionFactory is not init. ", AgentErrorCode.REPORT_SQLSESSIONFACTORY_UNINIT, list);
            return restResponse;
        }
        SqlSession sqlSession = null;
        try
        {
            sqlSession = factory.openSession();
            ReportDao reportDao = sqlSession.getMapper(ReportDao.class);
            reportDao.queryVdntraffic(hashMap);
            list = (ArrayList<VdnTraffic>) hashMap.get("result");
            if (null != list && !list.isEmpty())
            {
                if (null == list.get(0))
                {
                    list = null;
                }
            }
            restResponse = makeResponse(restResponse, null, AgentErrorCode.SUCCESS, list);
        }
        catch (Exception e)
        {
            LOG.error(LogUtils.AGENT_ID + "queryVdnTraffic failed, recieve message:{}", LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
            restResponse = makeResponse(restResponse, e.getMessage(), AgentErrorCode.REPROT_QUERY_FAILED, list);
        }
        finally
        {
            if (sqlSession != null)
            {
                sqlSession.close();
            }
        }
        return restResponse;
    }

     /**
      * 技能话务量报表查询
      * @param HashMap<String, Object> 技能话务量报表查询参数
      * @param agentId 座席工号
      * @return RestResponse 返回结果
      */
     @SuppressWarnings("unchecked")
     public RestResponse querySkillTraffic(HashMap<String, Object> hashMap, String agentId)
     {
         RestResponse restResponse = new RestResponse();
         SqlSessionFactory factory = Mybatis.getUIDBSqlSessionFactory();
         ArrayList<SkillTraffic> list = new ArrayList<SkillTraffic>();
         
         if (null == factory)
         {
             LOG.error(LogUtils.AGENT_ID + "sqlSessionFactory is not init.", LogUtils.encodeForLog(agentId));
             restResponse = makeResponse(restResponse, "SqlSessionFactory is not init. ", AgentErrorCode.REPORT_SQLSESSIONFACTORY_UNINIT, list);
             return restResponse;
         }
         SqlSession sqlSession = null;
         try
         {
             sqlSession = factory.openSession();
             ReportDao reportDao = sqlSession.getMapper(ReportDao.class);
             reportDao.querySkillTraffic(hashMap);
             list = (ArrayList<SkillTraffic>) hashMap.get("result");
             if (null != list && !list.isEmpty())
             {
                 if (null == list.get(0))
                 {
                     list = null;
                 }
             }
             restResponse = makeResponse(restResponse, null, AgentErrorCode.SUCCESS, list);
         }
         catch (Exception e)
         {
             LOG.error(LogUtils.AGENT_ID + "querySkillTraffic failed , recieve message:{} ", LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
             restResponse = makeResponse(restResponse, e.getMessage(), AgentErrorCode.REPROT_QUERY_FAILED, list);
         }
         finally
         {
             if (sqlSession != null)
             {
                 sqlSession.close();
             }
         }
         return restResponse;
     }
     
     /**
      * 话务员话务量报表查询
      * @param HashMap<String, Object> 技能话务量报表查询参数
      * @param agentId 座席工号
      * @return RestResponse 返回结果
      */
     @SuppressWarnings("unchecked")
     public RestResponse queryAgentraffic(HashMap<String, Object> hashMap, String agentId)
     {
         RestResponse restResponse = new RestResponse();
         SqlSessionFactory factory = Mybatis.getUIDBSqlSessionFactory();
         ArrayList<AgentTraffic> list = new ArrayList<AgentTraffic>();
         
         if (null == factory)
         {
             LOG.error(LogUtils.AGENT_ID + "sqlSessionFactory is not init.", LogUtils.encodeForLog(agentId));
             restResponse = makeResponse(restResponse, "SqlSessionFactory is not init. ", AgentErrorCode.REPORT_SQLSESSIONFACTORY_UNINIT, list);
             return restResponse;
         }
         SqlSession sqlSession = null;
         try
         {
             sqlSession = factory.openSession();
             ReportDao reportDao = sqlSession.getMapper(ReportDao.class);
             reportDao.queryAgentTraffic(hashMap);
             list = (ArrayList<AgentTraffic>) hashMap.get("result");
             if (null != list && !list.isEmpty())
             {
                 if (null == list.get(0))
                 {
                     list = null;
                 }
             }
             restResponse = makeResponse(restResponse, null, AgentErrorCode.SUCCESS, list);
         }
         catch (Exception e)
         {
             LOG.error(LogUtils.AGENT_ID + "queryAgentTraffic failed , recieve message:{} ", LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
             restResponse = makeResponse(restResponse, e.getMessage(), AgentErrorCode.REPROT_QUERY_FAILED, list);
         }
         finally
         {
             if (sqlSession != null)
             {
                 sqlSession.close();
             }
         }
         return restResponse;
     }
     
     /**
      * 话务员接续报表查询
      * @param queryParamMap
      * @param agentId
      * @return
      */
     @SuppressWarnings("unchecked")
     public RestResponse queryAgentWork(HashMap<String, Object> queryParamMap, String agentId)
     {
         RestResponse restResponse = new RestResponse();
         SqlSessionFactory factory = Mybatis.getUIDBSqlSessionFactory();
         ArrayList<AgentWork> list = new ArrayList<AgentWork>();
         //初始化数据库连接
         if (null == factory)
         {
             LOG.error(LogUtils.AGENT_ID + "sqlSessionFactory is not init.", LogUtils.encodeForLog(agentId));
             restResponse = makeResponse(restResponse, "SqlSessionFactory is not init. ", AgentErrorCode.REPORT_SQLSESSIONFACTORY_UNINIT, list);
             return restResponse;
         }
         SqlSession sqlSession = null;
         try
         {
             sqlSession = factory.openSession();
             ReportDao reportDao = sqlSession.getMapper(ReportDao.class);
             reportDao.queryAgentWork(queryParamMap);
             list = (ArrayList<AgentWork>) queryParamMap.get("result");
             if (null != list && !list.isEmpty())
             {
                 if (null == list.get(0))
                 {
                     list = null;
                 }
             }
             restResponse = makeResponse(restResponse, null, AgentErrorCode.SUCCESS, list);
         }
         catch (Exception e)
         {
             LOG.error(LogUtils.AGENT_ID + "queryAgentWork failed , recieve message:{} ", LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
             restResponse = makeResponse(restResponse, e.getMessage(), AgentErrorCode.REPROT_QUERY_FAILED, list);
         }
         finally
         {
             if (sqlSession != null)
             {
                 sqlSession.close();
             }
         }
         return restResponse;
     }     
    
     /**
      * 话务员呼出报表查询
      * @param HashMap<String, Object> 话务员呼出报表查询参数
      * @param agentId 座席工号
      * @return RestResponse 返回结果
      */
     @SuppressWarnings("unchecked")
     public RestResponse queryAgentCallOutBrief(HashMap<String, Object> hashMap, String agentId)
     {
         RestResponse restResponse = new RestResponse();
         SqlSessionFactory factory = Mybatis.getUIDBSqlSessionFactory();
         ArrayList<AgentCallOutBriefBean> list = new ArrayList<AgentCallOutBriefBean>();
         
         if (null == factory)
         {
             LOG.error(LogUtils.AGENT_ID + "sqlSessionFactory is not init.", LogUtils.encodeForLog(agentId));
             restResponse = makeResponse(restResponse, "SqlSessionFactory is not init. ", AgentErrorCode.REPORT_SQLSESSIONFACTORY_UNINIT, list);
             return restResponse;
         }
         SqlSession sqlSession = null;
         try
         {
             sqlSession = factory.openSession();
             ReportDao reportDao = sqlSession.getMapper(ReportDao.class);
             reportDao.queryAgentCallOutBrief(hashMap);
             list = (ArrayList<AgentCallOutBriefBean>) hashMap.get("result");
             if (null != list && !list.isEmpty())
             {
                 if (null == list.get(0))
                 {
                     list = null;
                 }
             }
             restResponse = makeResponse(restResponse, null, AgentErrorCode.SUCCESS, list);
         }
         catch (Exception e)
         {
             LOG.error(LogUtils.AGENT_ID + "queryAgentCallOutBrief failed , recieve message:{} ", LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(e.getMessage()));
             restResponse = makeResponse(restResponse, e.getMessage(), AgentErrorCode.REPROT_QUERY_FAILED, list);
         }
         finally
         {
             if (sqlSession != null)
             {
                 sqlSession.close();
             }
         }
         return restResponse;
     }
     
    /**
     * 设置返回结果对象
     * @param restResponse 返回结果对象
     * @param message 消息
     * @param retCode 错误码
     * @param result  结果对象
     * @return RestResponse
     */
    public RestResponse makeResponse(RestResponse restResponse, String message, String retCode, Object result) 
    {
        restResponse.setMessage(message);
        restResponse.setReturnCode(retCode);
        restResponse.setRetObject("result", result);
        return restResponse;
    }
    
    

}
