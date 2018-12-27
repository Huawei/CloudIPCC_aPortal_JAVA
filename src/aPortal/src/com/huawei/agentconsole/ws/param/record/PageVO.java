
package com.huawei.agentconsole.ws.param.record;


/**
 * 
 * <p>Title:  分页信息</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author l00357702
 * @version V1.0 2018年8月10日
 * @since
 */
public class PageVO
{

    /**
     * 当前页数
     */
    private int curPage;
    
    /**
     * 每页数据多少
     */
    private int pageSize;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 总数据行数
     */
    private int totalRows;

    /**
     * @return the curPage
     */
    public int getCurPage()
    {
        return curPage;
    }

    /**
     * @param curPage the curPage to set
     */
    public void setCurPage(int curPage)
    {
        this.curPage = curPage;
    }

    /**
     * @return the pageSize
     */
    public int getPageSize()
    {
        return pageSize;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    /**
     * @return the totalPages
     */
    public int getTotalPages()
    {
        return totalPages;
    }

    /**
     * @param totalPages the totalPages to set
     */
    public void setTotalPages(int totalPages)
    {
        this.totalPages = totalPages;
    }

    /**
     * @return the totalRows
     */
    public int getTotalRows()
    {
        return totalRows;
    }

    /**
     * @param totalRows the totalRows to set
     */
    public void setTotalRows(int totalRows)
    {
        this.totalRows = totalRows;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("PageVO [curPage=");
        builder.append(curPage);
        builder.append(", pageSize=");
        builder.append(pageSize);
        builder.append(", totalPages=");
        builder.append(totalPages);
        builder.append(", totalRows=");
        builder.append(totalRows);
        builder.append("]");
        return builder.toString();
    }
    
    
    
}
