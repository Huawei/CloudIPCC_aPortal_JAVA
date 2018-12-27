
package com.huawei.agentconsole.ws.param;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.huawei.agentconsole.common.constant.ParamPatternConstant;

/**
 * 
 * <p>
 * Title: 用页分页的参数
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * <pre></pre>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Huawei Technologies Co.
 * </p>
 * 
 * @author l00357702
 * @version V1.0 2018年8月6日
 * @since
 */
public class PageParam
{

    /**
     * 当前页数
     */
    @Pattern(regexp = ParamPatternConstant.RECORDQUERY_PAGENO_PATTERN)
    @NotBlank
    private String curPage;

    /**
     * 每页多少条记录
     */
    @Pattern(regexp = ParamPatternConstant.RECORDQUERY_PAGESIZE_PATTERN)
    @NotBlank
    private String pageSize;

    /**
     * @return the curPage
     */
    public String getCurPage()
    {
        return curPage;
    }

    /**
     * @param curPage
     *            the curPage to set
     */
    public void setCurPage(String curPage)
    {
        this.curPage = curPage;
    }

    /**
     * @return the pageSize
     */
    public String getPageSize()
    {
        return pageSize;
    }

    /**
     * @param pageSize
     *            the pageSize to set
     */
    public void setPageSize(String pageSize)
    {
        this.pageSize = pageSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("PageParam [curPage=");
        builder.append(curPage);
        builder.append(", pageSize=");
        builder.append(pageSize);
        builder.append("]");
        return builder.toString();
    }

}
