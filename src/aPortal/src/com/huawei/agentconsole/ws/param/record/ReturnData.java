
package com.huawei.agentconsole.ws.param.record;

import java.util.List;


public class ReturnData
{

    private PageVO pageVO;

    private List<Result> result;
    
    /**
     * @return the pageVO
     */
    public PageVO getPageVO()
    {
        return pageVO;
    }

    /**
     * @param pageVO the pageVO to set
     */
    public void setPageVO(PageVO pageVO)
    {
        this.pageVO = pageVO;
    }

    public List<Result> getResult()
    {
        return result;
    }

    public void setResult(List<Result> result)
    {
        this.result = result;
    }

}
