
package com.huawei.agentconsole.ws.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * <p>Title: 临时文件夹权限控制过滤器 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author l00357702
 * @version V1.0 2018年9月28日
 * @since
 */
public class TempFilter implements Filter
{

    @Override
    public void destroy()
    {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        HttpServletResponse res = (HttpServletResponse) response;
        res.setStatus(404);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException
    {
        
    }

}
