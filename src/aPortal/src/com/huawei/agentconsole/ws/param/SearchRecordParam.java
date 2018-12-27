
package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.NotNull;

/**
 * 
 * <p>Title: 查询结果，分为分页信息和数据信息 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author l00357702
 * @version V1.0 2018年8月10日
 * @since
 */
public class SearchRecordParam
{
    /**
     * 分页信息
     */
    @NotNull
    private PageParam page;
    
    /**
     * 请求参数
     */
    @NotNull
    private RequestParamForRecordBean requestParam;


    /**
     * @return the page
     */
    public PageParam getPage()
    {
        return page;
    }

    /**
     * @param page the page to set
     */
    public void setPage(PageParam page)
    {
        this.page = page;
    }

 
    public RequestParamForRecordBean getRequestParam()
    {
        return requestParam;
    }

    public void setRequestParam(RequestParamForRecordBean requestParam)
    {
        this.requestParam = requestParam;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SearchRecordParam [page=");
        builder.append(page);
        builder.append(", requestParam=");
        builder.append(requestParam);
        builder.append("]");
        return builder.toString();
    }
    
    
}
