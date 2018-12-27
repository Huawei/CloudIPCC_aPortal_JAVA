
package com.huawei.agentconsole.ws.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.bean.AgentBaseInfoBean;
import com.huawei.agentconsole.common.constant.AgentErrorCode;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.global.GlobalObject;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.common.util.StringUtils;


public class AuthFilter implements Filter
{
    private static final Logger LOG = LoggerFactory.getLogger(AuthFilter.class);
    
    private static final int AGENT_ID_LENGTH = 5;
    
    private List<String> noAuthUrlForLogin = new ArrayList<String>();
    private List<String> noAuthUrlForEvent = new ArrayList<String>();
    
    @Override
    public void destroy()
    {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse) res;
        request.setCharacterEncoding(CommonConstant.UTF_8);
        response.setCharacterEncoding(CommonConstant.UTF_8);
        String path = request.getPathInfo();
        String requestURI = request.getRequestURI();
        if (StringUtils.isNullOrBlank(path))
        {
            //异常url
            writeErrorResponse(response, makeErrorResponseString(AgentErrorCode.AGENT_REST_INVALID, "No agentId on the url"));
            return;
        }
        //过滤直接访问临时文件夹访问缓存录音文件的请求
        if (!StringUtils.isNullOrBlank(requestURI) && requestURI.contains(CommonConstant.TEMPFILE_FOLDER_NAME))
        {
            writeErrorResponse(response, makeErrorResponseString(AgentErrorCode.AGENT_REST_NORIGHT, "no right get the file"));
            return;
        }
        
        if (isNoNeedAuthUrl(request.getMethod(), path))
        {
            chain.doFilter(request, response);
            return;
        }
        

        
        String agentId = request.getParameter("agentId");
        if (StringUtils.isNullOrBlank(agentId) 
                || agentId.length() > AGENT_ID_LENGTH)
        {
            writeErrorResponse(response, 
                    makeErrorResponseString(AgentErrorCode.AGENT_REST_INVALID, "No agentId on the url"));
            return;
        }
        AgentBaseInfoBean agentBaseInfoBean = GlobalObject.getAgentBaseInfo(agentId);
        if (null == agentBaseInfoBean)
        {
            //没有在当前服务器登录
            writeErrorResponse(response,
                    makeErrorResponseString(AgentErrorCode.AGENT_NOT_LOGIN, "Agent not login"));
            return; 
        }
     
        if (agentBaseInfoBean.getPortalToken().equals(request.getSession().getId()))
        {
            //对查询报表的请求进行过滤拦截，如果不是质检员，不允许查询,查询报表的rest接口中含有report关键字，此处通过判断path中是否含有report进行监测，
            if (path.contains(CommonConstant.REPORT_REST_PATH) && !agentBaseInfoBean.isCensor())
            {
                writeErrorResponse(response,
                        makeErrorResponseString(AgentErrorCode.AGENT_REST_NOQCRIGHT, "Agent is not qc role"));
                return;
            }
            
            chain.doFilter(request, response); 
            return;
        }
        else
        {
            //权限不一致
            writeErrorResponse(response, 
                    makeErrorResponseString(AgentErrorCode.AGENT_REST_NORIGHT, "Agent not right"));
            return;  
        }
    }
    
    private boolean isNoNeedAuthUrl(String method, String path)
    {
        if ("GET".equalsIgnoreCase(method) && "/onlineagent/verifycode".contains(path))
        {
            //验证码请求不校验
            return true;
        }
        else if ("PUT".equalsIgnoreCase(method) && noAuthUrlForLogin.contains(path))
        {
            return true;
        }
        else if ("POST".equalsIgnoreCase(method) && noAuthUrlForEvent.contains(path))
        {
            return true;
        }
        else
        {
            return false;
        }
           
    }
    
    /**
     * 没有权限
     * @param response response对象
     * @param error 错误消息
     */
    private void writeErrorResponse(HttpServletResponse response, String error)
    {
        try
        {
            response.addHeader("Cache-Control", "no-cache");
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.addHeader("JSON-Type", "application/json");
            response.getWriter().write(error);
            response.getWriter().close();
            return;
        }
        catch (IOException e)
        {
            LOG.error(LogUtils.encodeForLog(e.getMessage()));
        }
    }
    
    /**
    * 根据参数构造返回信息
    * @return
    */
   private String makeErrorResponseString(String retCode, String message)
   {
       StringBuilder json = new StringBuilder();
       json.append("{\"retcode\":\"");
       json.append(retCode);
       json.append("\",\"message\":\"");
       json.append(message);
       json.append("\",\"result\":");
       json.append("\"\"");
       json.append("}");
       return json.toString();
   }

    @Override
    public void init(FilterConfig arg0) throws ServletException
    {
              
        noAuthUrlForLogin.add("/onlineagent/login");
        noAuthUrlForLogin.add("/onlineagent/forcelogin");
        noAuthUrlForLogin.add("/onlineagent/loginbyaccount");
        noAuthUrlForEvent.add("/eventreceiver/event");
    }

}
