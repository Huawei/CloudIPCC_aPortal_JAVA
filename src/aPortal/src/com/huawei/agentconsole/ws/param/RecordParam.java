package com.huawei.agentconsole.ws.param;

/**
 * <p>Title: 录音文件生成及下载参数</p>
 * <p>Description: </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author y84103593
 * @version V1.0 2018年8月2日
 * @since
 */
public class RecordParam
{
	private int format;
	
	private String recordId;
	
	private String recordFileName;

	public int getFormat()
	{
		return format;
	}

	public void setFormat(int format)
	{
		this.format = format;
	}

	public String getRecordId()
	{
		return recordId;
	}

	public void setRecordId(String recordId)
	{
		this.recordId = recordId;
	}

	public String getRecordFileName()
	{
		return recordFileName;
	}

	public void setRecordFileName(String recordFileName)
	{
		this.recordFileName = recordFileName;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("format:").append(format);      
        sb.append(",").append("recordId:").append(recordId);
        sb.append(",").append("recordFileName").append(recordFileName);
        sb.append("}");
        return sb.toString();
	}

}
