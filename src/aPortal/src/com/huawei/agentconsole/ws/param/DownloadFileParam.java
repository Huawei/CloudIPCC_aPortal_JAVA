package com.huawei.agentconsole.ws.param;

import javax.ws.rs.FormParam;

import org.hibernate.validator.constraints.NotBlank;
/**
 * 
 * <p>Title:  下载报表和录音临时文件所需的路径参数 </p>
 * <p>Description:  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author w00466288
 * @version V1.0 2018年10月31日
 * @since
 */
public class DownloadFileParam
{
    /**
     *临时文件路径
     */
    @NotBlank
    @FormParam("desFilePath")
    private String desFilePath;

    public String getDesFilePath()
    {
        return desFilePath;
    }

    public void setDesFilePath(String desFilePath)
    {
        this.desFilePath = desFilePath;
    }

}
