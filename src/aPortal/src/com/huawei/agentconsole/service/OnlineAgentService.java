
package com.huawei.agentconsole.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.bean.CallerNumber;
import com.huawei.agentconsole.bean.DBOperResult;
import com.huawei.agentconsole.bean.TenantInfo;
import com.huawei.agentconsole.common.config.ConfigList;
import com.huawei.agentconsole.common.config.ConfigProperties;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.http.AgentRequest;
import com.huawei.agentconsole.common.util.JsonUtils;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;
import com.huawei.agentconsole.dao.service.saas.BpoConfigDaoService;
import com.huawei.agentconsole.ws.param.AutoAnswerParam;
import com.huawei.agentconsole.ws.param.LoginByAccountParam;
import com.huawei.agentconsole.ws.param.LoginParam;
import com.huawei.agentconsole.ws.param.RestResponse;

/**
 * 
 * <p>Title: 座席的登录和登出 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月25日
 * @since
 */
public class OnlineAgentService
{
    private static final Logger LOG = LoggerFactory.getLogger(OnlineAgentService.class);
    
    private String agentId;
    
    private String verifyCodeKey;
    
    private String account;
    
    public OnlineAgentService(String agentId)
    {
        this.agentId = agentId;
        this.verifyCodeKey = agentId + CommonConstant.IS_NEED_VERIFY;
    }
    
    public OnlineAgentService(String agent,boolean isAccount)
    {
        if(isAccount)
        {
            this.account = agent;
        }
        else
        {

            this.agentId = agent; 
        }
        this.verifyCodeKey = agent + CommonConstant.IS_NEED_VERIFY;
    }
    /**
     * 进行验证码校验
     * @param request
     * @param loginParam
     * @return
     */
    private boolean checkVerifyCode(HttpServletRequest request, String inputVerifyCode)
    {
        ServletContext servletCtx = request.getSession().getServletContext();
       
        boolean needVerifyCode = false;
        Date lastUpdateTime = (Date)servletCtx.getAttribute(verifyCodeKey);
        if (null != lastUpdateTime) 
        {
            needVerifyCode = true;
        }
        
        if (needVerifyCode)
        {
            //需要进行验证码校验
            String verifyCode = (String) request.getSession().getAttribute("verifyCode");
            if (null == verifyCode 
                    || !verifyCode.equalsIgnoreCase(inputVerifyCode))
            {
                return false;
            }
        }
        return true;
    }
    
    
    
    /**
     * 登录
     * @param request
     * @param loginParam 登录参数
     * @param isForceLogin 是否强制登录
     * @return
     */
    public Map<String, Object> login(HttpServletRequest request, LoginParam loginParam, boolean isForceLogin)
    {
        if (!checkVerifyCode(request, loginParam.getVerifyCode()))
        {
            RestResponse restResponse = new RestResponse();
            restResponse.setReturnCode(AgentErrorCode.VERIFYCODE_ISINVALID);
            restResponse.setMessage("verifycode is invalid");
            return restResponse.returnResult();
        }
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        if (isForceLogin)
        {
            url.append("/onlineagent/").append(agentId).append("/forcelogin");
        }
        else
        {
            url.append("/onlineagent/").append(agentId);
        }
       
        StringBuffer pushUrl = new StringBuffer();
        pushUrl.append(GlobalObject.getEventPushUrl()).append("?");
        String pushUrlToken = StringUtils.generateToken();
        pushUrl.append("token=").append(pushUrlToken);
        
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("password", loginParam.getPassword());
        requestMap.put("phonenum", loginParam.getPhonenum());
        requestMap.put("autoanswer", false);
        requestMap.put("status", CommonConstant.WORK_STATUS_AFTER_LOGIN);
        requestMap.put("autoenteridle", CommonConstant.IS_IDLE_AFTER_TALKING);
        requestMap.put("pushUrl", pushUrl.toString());
        requestMap.put("checkInWebm", false);
        
        AgentBaseInfoBean agentAuthInfoBean = new AgentBaseInfoBean(agentId);
        agentAuthInfoBean.setPushUrlToken(pushUrlToken);
        GlobalObject.addTempAgentInfoByLogin(pushUrlToken, agentAuthInfoBean);
        Map<String, Object> result = AgentRequest.loginPut(agentId, url.toString(), requestMap, agentAuthInfoBean);
        if (AgentErrorCode.SUCCESS.equals(StringUtils.getRetCode(result)))
        {
            //登录成功, 进行签入技能
        	@SuppressWarnings("unchecked")
			Map<String, Object> content = (Map<String, Object>) result.get("result");
        	if (null != content)
        	{
        		String vdnId = String.valueOf(content.get("vdnid"));
        		agentAuthInfoBean.setVdnId(vdnId);
        		//获取主叫号码和租户信息
        		getBpoConfigInfo(vdnId, content, agentAuthInfoBean);       
        		
        		//判断是否是质检员
        		boolean isCensor = isCensor(agentId);
                agentAuthInfoBean.setCensor(isCensor);
                content.put("is_censor", isCensor);
        	}
        	
            resetSkill();
            request.getServletContext().removeAttribute(verifyCodeKey);
            request.getSession().setAttribute("verifyCode", "");
            LOG.info(LogUtils.AGENT_ID + "Remove agent verifycode info {}", 
                    LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(verifyCodeKey));
        }
        else
        {
            if (!AgentErrorCode.AGENT_HAS_LOGIN.equals(StringUtils.getRetCode(result)))
            {
                request.getSession().setAttribute("verifyCode", "");
                if ("true".equalsIgnoreCase(ConfigProperties.getKey(ConfigList.VERIFY, "VERIFYCODE_ISUSED")))
                {
                    //需要输入验证码进行校验
                    request.getServletContext().setAttribute(verifyCodeKey, new Date());
                    LOG.info(LogUtils.AGENT_ID + "Add agent verifycode info {}", 
                            LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(verifyCodeKey));
                }
            }
        }
            
        GlobalObject.delTempAgentInfoByLogin(pushUrlToken);
        return result;
        
    }
    
    
    public Map<String, Object> loginByAccount(HttpServletRequest request, LoginByAccountParam loginParam, boolean isForceLogin)
    {
        if (!checkVerifyCode(request, loginParam.getVerifyCode()))
        {
            RestResponse restResponse = new RestResponse();
            restResponse.setReturnCode(AgentErrorCode.VERIFYCODE_ISINVALID);
            restResponse.setMessage("verifycode is invalid");
            return restResponse.returnResult();
        }
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
     
        url.append("/onlineagent/").append("loginbyaccount");
       
        StringBuffer pushUrl = new StringBuffer();
        pushUrl.append(GlobalObject.getEventPushUrl()).append("?");
        String pushUrlToken = StringUtils.generateToken();
        pushUrl.append("token=").append(pushUrlToken);
        
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.put("account", loginParam.getAgentAccount());
        requestMap.put("password", loginParam.getPassword());
        requestMap.put("autoanswer", false);
        requestMap.put("status", CommonConstant.WORK_STATUS_AFTER_LOGIN);
        requestMap.put("autoenteridle", CommonConstant.IS_IDLE_AFTER_TALKING);
        requestMap.put("pushUrl", pushUrl.toString());
        requestMap.put("checkInWebm", false);
        
        AgentBaseInfoBean agentAuthInfoBean = new AgentBaseInfoBean();
        agentAuthInfoBean.setAccount(account);
        agentAuthInfoBean.setPushUrlToken(pushUrlToken);
        GlobalObject.addTempAgentInfoByLogin(pushUrlToken, agentAuthInfoBean);
        Map<String, Object> result = AgentRequest.loginPutByAccount(account, url.toString(), requestMap, agentAuthInfoBean);
        if (AgentErrorCode.SUCCESS.equals(StringUtils.getRetCode(result)))
        {
            //登录成功, 进行签入技能
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) result.get("result");
            if(null != content)
            {
                this.agentId = agentAuthInfoBean.getAgentId();
                String vdnId = String.valueOf(content.get("vdnid"));
                agentAuthInfoBean.setVdnId(vdnId);
                //获取主叫号码、配置信息
                getBpoConfigInfo(vdnId, content, agentAuthInfoBean); 
                
                //判断是否是质检员
                boolean isCensor = isCensor(agentAuthInfoBean.getAgentId());
                agentAuthInfoBean.setCensor(isCensor);
                content.put("is_censor", isCensor);
                
            }
            resetSkill();
            request.getServletContext().removeAttribute(verifyCodeKey);
            request.getSession().setAttribute("verifyCode", "");
            LOG.info(LogUtils.AGENT_ID + "Remove agent verifycode info {}", LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(verifyCodeKey));
        }
        else
        {
            if (!AgentErrorCode.AGENT_HAS_LOGIN.equals(StringUtils.getRetCode(result)))
            {
                request.getSession().setAttribute("verifyCode", "");
                if ("true".equalsIgnoreCase(ConfigProperties.getKey(ConfigList.VERIFY, "VERIFYCODE_ISUSED")))
                {
                    //需要输入验证码进行校验
                    request.getServletContext().setAttribute(verifyCodeKey, new Date());
                    LOG.info(LogUtils.ACCOUNT +  "Add agent verifycode info {}", 
                            LogUtils.encodeForLog(loginParam.getAgentAccount()), LogUtils.encodeForLog(verifyCodeKey));
                }
            }
        }
            
        GlobalObject.delTempAgentInfoByLogin(pushUrlToken);
        return result;
        
    }
    /**
     * 登出
     * @return
     */
    public Map<String, Object> logout()
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/onlineagent/").append(agentId).append("/forcelogout");
        Map<String, Object> result = AgentRequest.delete(agentId, url.toString());
        if (AgentErrorCode.SUCCESS.equals(StringUtils.getRetCode(result)))
        {
            //登出成功
            GlobalObject.delAgentBaseInfo(agentId);
        }
        return result; 
    }
    
    /**
     * 签入技能
     */
    private void resetSkill()
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/onlineagent/").append(agentId);
        url.append("/resetskill/true");
        Map<String, Object> result = AgentRequest.post(agentId, url.toString(), null);
        if (!AgentErrorCode.SUCCESS.equals(StringUtils.getRetCode(result)))
        {
            //重置技能成功
            LOG.error(LogUtils.AGENT_ID + "reset skill failed. Result is {}",  new Object[] {agentId, JsonUtils.beanToJson(result)});
                    
        }
    }
    
    /**
     * 示闲
     * @return
     */
    public Map<String, Object> sayFree()
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/onlineagent/").append(agentId).append("/sayfree");
        return AgentRequest.post(agentId, url.toString(), null);
    }
    
    /**
     * 示忙
     * @return
     */
    public Map<String, Object> sayBusy()
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/onlineagent/").append(agentId).append("/saybusy");
        return AgentRequest.post(agentId, url.toString(), null);
    }
    
    /**
     * 设置是否自动应答
     * @return
     */
    public Map<String, Object> setAutoAnswer(AutoAnswerParam autoAnswerParam)
    {
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/onlineagent/").append(agentId).append("/autoanswer/").append(autoAnswerParam.getAutoAnswer());
        return AgentRequest.post(agentId, url.toString(), null);
    }
    
    /**
     * 判断是否为质检员，预登陆接口一并返回去，用于前端进行处理
     * @param agentId
     * @return
     */
    private boolean isCensor(String agentId)
    {
        boolean isCensor = false;
        StringBuffer url = new StringBuffer();
        url.append(GlobalObject.getAgentServerUrl());
        url.append("/agentgroup/").append(agentId).append("/iscensor/");
        
        Map<String, Object> result  = AgentRequest.get(agentId, url.toString());
        if (AgentErrorCode.SUCCESS.equals(StringUtils.getRetCode(result)))
        {
            
            isCensor =  Boolean.valueOf(result.get("result").toString());
        }
        
        return isCensor;
    }
    
    /**
     * 获取配置信息(主叫号码或者租户信息)
     * @param vdnId
     * @param resultMap
     * @param agentBaseInfo
     */
    private void getBpoConfigInfo(String vdnId, Map<String, Object> resultMap, AgentBaseInfoBean agentBaseInfo)
    {
        int ccId =  Integer.valueOf(ConfigProperties.getKey(ConfigList.BASIC, "CALLCENTER_ID"));
        BpoConfigDaoService bpoConfigDaoService = new BpoConfigDaoService(agentId, ccId, Integer.valueOf(vdnId));
        /**
         * 获取主叫信息
         */
        DBOperResult<List<CallerNumber>> callerNumbersResult = bpoConfigDaoService.queryCallerNumbers();                
        if (callerNumbersResult.isSucess())
        {
            List<CallerNumber> callerList = callerNumbersResult.getResult();
            agentBaseInfo.setCallerNumbers(callerList);
            resultMap.put("callerList", callerList);
        }
        else 
        {
            //获取主叫号码失败
            LOG.error(LogUtils.AGENT_ID + "database error when queryCallerNumbers", LogUtils.encodeForLog(agentId)); 
        }
        
        /**
         * 获取租户信息
         */
        DBOperResult<TenantInfo> tenatInfoResult = bpoConfigDaoService.getTenantInfo();
        if (tenatInfoResult.isSucess())
        {
            agentBaseInfo.setTenantInfo(tenatInfoResult.getResult());
        }
        else 
        {

            //获取租户信息失败
            LOG.error(LogUtils.AGENT_ID + "database error when getTenantInfo", LogUtils.encodeForLog(agentId)); 
        }
        
        TenantInfo tenantInfo = agentBaseInfo.getTenantInfo();
        if (null == tenantInfo || CommonConstant.TRIAL_TENANT != tenantInfo.getTrial())
        {
            //查询不到租户信息时，默认是商用客户
            resultMap.put("isTrial", "0");
        }
        else
        {
            resultMap.put("isTrial", CommonConstant.TRIAL_TENANT);
        }
    }
  
   
    
}
