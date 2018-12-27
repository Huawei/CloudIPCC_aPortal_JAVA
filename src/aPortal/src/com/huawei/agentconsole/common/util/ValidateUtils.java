
package com.huawei.agentconsole.common.util;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.constant.ParamPatternConstant;

public class ValidateUtils
{

    
    /**
     * 校验坐席id
     * @param agentId
     * @return
     */
    public static boolean isAgentId(String agentId)
    {
        if(StringUtils.isNullOrBlank(agentId))
        {
            return true;
        }
        else
        {
            String tempStr = Normalizer.normalize(agentId, Form.NFKC);
            Pattern pattern = Pattern.compile(ParamPatternConstant.RECORDQUERY_AGENTID_PATTERN);
            return pattern.matcher(tempStr).matches();
        }
    }
  
    
   
   
    /**
     * 校验recordid  因为 agw会校验 此处指校验下长度
     * @param recordId
     * @return
     */
    public static boolean isRecordId(String recordId)
    {
        if (StringUtils.isNullOrBlank(recordId))
        {
            return false;
        }
        if(recordId.length() > CommonConstant.MAX_RECORDID_LENGTH)
        {
            return false;
        }
        return true;
    }
}
