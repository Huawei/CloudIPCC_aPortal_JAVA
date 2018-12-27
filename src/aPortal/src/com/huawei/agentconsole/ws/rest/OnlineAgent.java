
package com.huawei.agentconsole.ws.rest;


import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;
import com.huawei.agentconsole.service.OnlineAgentService;
import com.huawei.agentconsole.service.VerifyCodeService;
import com.huawei.agentconsole.ws.param.AutoAnswerParam;
import com.huawei.agentconsole.ws.param.LoginByAccountParam;
import com.huawei.agentconsole.ws.param.LoginParam;

/**
 * 
 * <p>Title: 座席登录和登出服务  </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author j00204006
 * @version V1.0 2018年7月25日
 * @since
 */
@Path("/onlineagent")
public class OnlineAgent
{
    private static final Logger LOG = LoggerFactory.getLogger(OnlineAgent.class);
    
    /**
     * 获取验证码
     * @param request
     * @param response
     * @param agentId
     * @return
     */
    @GET
    @Path("/verifycode")
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Map<String, Object> getVerifycode(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @QueryParam("agentId")String agentId)
    {
        VerifyCodeService verifyCodeService = new VerifyCodeService(agentId);
        return verifyCodeService.getVerifyCode(request).returnResult();
    }
    
    /**
     * 座席登录接口
     * @param request 请求对象
     * @param response 请求响应对象
     * @param loginParam 签入参数
     * @return Map<String, Object>
     */
    @PUT
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> login(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @Valid LoginParam loginParam)
    {
        
        LOG.info(LogUtils.METHOD_IN + ",loginParam:{}", 
                LogUtils.encodeForLog(loginParam.getAgentId()), loginParam);
        Map<String, Object> result = doLogin(request, response, loginParam, false);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(loginParam.getAgentId()), LogUtils.formatMap(result));
        return result;
    }
    
    private Map<String, Object> doLogin(HttpServletRequest request, HttpServletResponse response, 
            LoginParam loginParam, boolean isForceLogin)
    {
        
        OnlineAgentService service = new OnlineAgentService(loginParam.getAgentId());
        Map<String, Object> result = service.login(request, loginParam, isForceLogin);
        if (AgentErrorCode.SUCCESS.equals(StringUtils.getRetCode(result)))
        {
            addAuth(request, response, loginParam.getAgentId());
        }
        return result;
    }
    
    /**
     * 账号登录使用
     * @param request
     * @param response
     * @param loginParam
     * @param isForceLogin
     * @return
     */
    private Map<String, Object> doLoginByAccount(HttpServletRequest request, HttpServletResponse response, 
            LoginByAccountParam loginParam, boolean isForceLogin)
    {
        
        OnlineAgentService service = new OnlineAgentService(loginParam.getAgentAccount(),true);
        Map<String, Object> result = service.loginByAccount(request, loginParam, isForceLogin);
        if (AgentErrorCode.SUCCESS.equals(StringUtils.getRetCode(result)))
        {

            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) result.get("result");
            if (null != content)
            {
                String workno = String.valueOf(content.get("workno"));
                addAuth(request, response, workno);
            }
        }
        return result;
    }
    private void addAuth(HttpServletRequest request, HttpServletResponse response, String agentId)
    {
        AgentBaseInfoBean agentBaseInfoBean = GlobalObject.getAgentBaseInfo(agentId);
        if (null == agentBaseInfoBean)
        {
            return;
        }
        request.getSession().invalidate();
        agentBaseInfoBean.setPortalToken(request.getSession().getId());
      /*  Cookie cookie = new Cookie(CommonConstant.COOKIE_PORTAL_TOKEN_NAME, agentBaseInfoBean.getPortalToken());
        cookie.setPath("/aConsole"); //此次的path不能设置为 cookie.setPath("/aConsole")；会导致http无法访问
        if (CommonConstant.HTTPS.equals(request.getScheme()))
        {
            //采用https时，需要设置cookie的secure为true
            cookie.setSecure(true);
        }
        cookie.setHttpOnly(true);
        response.addCookie(cookie);*/
    }
    
    /**
     * 座席强制登录接口
     * @param request 请求对象
     * @param response 请求响应对象
     * @param loginParam 签入参数
     * @return Map<String, Object>
     */
    @PUT
    @Path("/forcelogin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> forceLogin(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @Valid LoginParam loginParam)
    {
        
        LOG.info(LogUtils.METHOD_IN + ",loginParam:{}", 
                LogUtils.encodeForLog(loginParam.getAgentId()), loginParam);
        Map<String, Object> result = doLogin(request, response, loginParam, true);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(loginParam.getAgentId()), LogUtils.formatMap(result));
        return result;
    }
    
    @PUT
    @Path("/loginbyaccount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> loginByAccount(
            @Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @Valid LoginByAccountParam loginParam)
    {
        
        LOG.info(LogUtils.METHOD_IN + ",loginParam:{}", 
                LogUtils.encodeForLog(loginParam.getAgentAccount()), loginParam);
        Map<String, Object> result = doLoginByAccount(request, response, loginParam, false);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(loginParam.getAgentAccount()), LogUtils.formatMap(result));
        return result;
    }
    
    
    /**
     * 座席登出接口
     * @param agentId 座席工号
     * @return Map<String, Object>
     */
    @DELETE
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> logout(@QueryParam("agentId") String agentId)
    {
        
        LOG.info(LogUtils.METHOD_IN,  LogUtils.encodeForLog(agentId));
        OnlineAgentService service = new OnlineAgentService(agentId);
        Map<String, Object> result = service.logout();
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
        return result;
    }
    

    /**
     * 示闲
     * @param agentId
     * @return
     */
    @PUT
    @Path("/sayfree")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> sayFree(
            @QueryParam("agentId") String agentId)
    {
        
        LOG.info(LogUtils.METHOD_IN,  LogUtils.encodeForLog(agentId));
        OnlineAgentService service = new OnlineAgentService(agentId);
        Map<String, Object> result = service.sayFree();
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
        return result;
    }
    
    /**
     * 示忙
     * @param agentId
     * @return
     */
    @PUT
    @Path("/saybusy")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> sayBusy(
            @QueryParam("agentId") String agentId)
    {
        
        LOG.info(LogUtils.METHOD_IN,  LogUtils.encodeForLog(agentId));
        OnlineAgentService service = new OnlineAgentService(agentId);
        Map<String, Object> result = service.sayBusy();
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
        return result;
    }
    
    /**
     * 设置是否自应答
     * @param agentId
     * @param autoAnswerParam
     * @return
     */
    @PUT
    @Path("/autoanswer")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> setAutoAnswer(
            @QueryParam("agentId") String agentId,
            @Valid AutoAnswerParam autoAnswerParam)
    {
        
        LOG.info(LogUtils.METHOD_IN + " AutoAnswerParam :{}",  LogUtils.encodeForLog(agentId), autoAnswerParam);
        OnlineAgentService service = new OnlineAgentService(agentId);
        Map<String, Object> result = service.setAutoAnswer(autoAnswerParam);
        LOG.info(LogUtils.METHOD_OUT, LogUtils.encodeForLog(agentId), LogUtils.encodeForLog(result));
        return result;
    }

    
    public Map<String, Object> getTrialCallList(@QueryParam("agentId") String agentId)
    {
        return null;
    }
}


