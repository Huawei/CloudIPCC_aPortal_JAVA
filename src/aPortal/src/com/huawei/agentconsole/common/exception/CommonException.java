
package com.huawei.agentconsole.common.exception;



public class CommonException extends Exception
{

    private static final long serialVersionUID = 1L;

    public CommonException()
    {
        
    }
    
    public CommonException(String message)
    {
        super(message);
    }
    
    public CommonException(Throwable throwable)
    {
        super(throwable);
    }
    
    public CommonException(String message, Throwable throwable)
    {
        super(message, throwable);
    }
}
