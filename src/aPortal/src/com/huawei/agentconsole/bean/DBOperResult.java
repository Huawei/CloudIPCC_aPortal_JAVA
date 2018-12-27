package com.huawei.agentconsole.bean;

/**
 * <p>Title:  数据库操作结果</p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author w00466288
 * @version V1.0 2018年10月15日
 * @since
 */
public class DBOperResult<T>
{
    public DBOperResult(T result, boolean isSucess)
    {
        this.result = result;
        this.isSucess = isSucess;
    }
    
    private T result;
    
    /**
     * 是否成功
     */
    private boolean isSucess;

    public T getResult()
    {
        return result;
    }
    
    public boolean isSucess()
    {
        return isSucess;
    }

}
