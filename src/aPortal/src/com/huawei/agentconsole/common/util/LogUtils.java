
package com.huawei.agentconsole.common.util;


import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class LogUtils
{
    /**
     * method in log prefix for rest
     */
    public static final String METHOD_IN  = "AgentId = {} received message:";
    
    /**
     * method out log prefix for rest 
     */
    public static final String METHOD_OUT = "AgentId = {} return   message:{}";
    
    public static final String AGENT_ID = "AgentId = {} ";
    
    public static final String ACCOUNT = "Account = {} ";
    
    
    /**
     * number 1
     */
    private static final int NUM_1 = 1;
    
    /**
     * number 2
     */
    private static final int NUM_2 = 2;
   
    /**
     * 4位的号码长度
     */
    private final static int PHONE_LEN_4 = 4;
    
    
    /**
     * 最大显示日志长度
     */
    private static final int MAX_STINRG = 100;
    
    /**
     * 格式化电话号码
     * @param phoneNumber phoneNumber
     * @return      phone.length<8 : phone, phone.length>=8 : 用 '*' 替换中间4位
     */
    public static String formatPhoneNumber(String phoneNumber)
    {
        if (null == phoneNumber || phoneNumber.isEmpty())
        {
            return "";
        }
        
        if (phoneNumber.length() <= PHONE_LEN_4)
        {
            return "****";
        }
        else
        {
            int length = phoneNumber.length();
            int begin = length / PHONE_LEN_4;
            int end = begin + PHONE_LEN_4;
            StringBuffer tempValue = new StringBuffer();
            tempValue.append(phoneNumber.substring(0, begin));
            tempValue.append("****");
            tempValue.append(phoneNumber.substring(end, length));
            return tempValue.toString();
        }    
    }
    
    
    /**
     * 格式化 map 对象, 对其中的主叫被叫进行 '*' 号处理
     * @param map map
     * @return    json string
     */
    @SuppressWarnings("unchecked")
    public static String formatMap(Map<String, Object> map) 
    {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        if (map == null)
        {
            sb.append('}');
            return sb.toString();
        }
        
        String[] phoneFields = {"caller", "called", "otherPhone", "number", "address", "callerNo", "talkerPhoneDn", "curUserDn"};
        
        String[] otherFields = {"attachData", "chatContent", "context", "confinfo", "sipInfo", "callData", "token", "confirmer","agentAsrResult", "tiket"}; 
        
        
        String[] mailFields = {"mailcaller"}; 
        
        Iterator<Entry<String, Object>> it = map.entrySet().iterator();
        Entry<String, Object> entry;
        String key;
        Object value;
        while (it.hasNext())
        {
            entry = it.next();
            key = entry.getKey();
            value = entry.getValue();
            if (value instanceof Map)
            {
                value = formatMap((Map<String, Object>)value);
            }
            if (existInArray(phoneFields, key))
            {
                // 对主叫被叫的号码进行 '*' 处理
                value = (value != null) ? formatPhoneNumber(String.valueOf(value)) : null;
            }
            if (existInArray(otherFields, key))
            {
                //对文字交谈内容和随路数据， 便签和公告内容进行 '*' 处理
                value = (value != null) ? String.valueOf(value).length() : null;
            }
            if (existInArray(mailFields, key))
            {
                //对邮件主叫进行匿名化
                value = (value != null) ? formatEmail(String.valueOf(value)) : null;
            }
            sb.append(key).append("=").append(value);
            sb.append(", ");
        }
        
        // 删除最后一个 ','
        int length = sb.length();
        if (length >= NUM_2 && sb.charAt(length - NUM_1) == ' ' && sb.charAt(length - NUM_2) == ',')
        {
            sb.deleteCharAt(length - NUM_2);
        }
        sb.append('}');
        return sb.toString();
    }
    
    /**
     * 邮箱匿名化
     * @return
     */
    private static String formatEmail(String value)
    {
        int length = value.length();
        int position = value.indexOf("@");
        if (-1 == position)
        {
            //表示没有@
            return value;
        }
        else
        {
            int begin;
            int end;
            if (3 > position)
            {
                //如果@前只有3个字符，则@前都用***匿名化
                begin = 0;
                end = position;
                return "***" + value.substring(end, length);
            }
            else
            {
                begin = position - 3;
                end = position;
                StringBuffer tempValue = new StringBuffer();
                tempValue.append(value.substring(0, begin)); 
                tempValue.append("***");  
                tempValue.append(value.substring(end, length));
                return tempValue.toString();
            }
        }
    }
   
    
    /**
     * 判断 value 是否在 array 中
     * @param array
     * @param value
     * @return
     */
    private static boolean existInArray(String[] array, String value)
    {
        if (array == null || array.length == 0)
        {
            return false;
        }
        
        boolean exist = false;
        for (String tmpValue : array)
        {
            if (tmpValue != null && tmpValue.equals(value))
            {
                exist = true;
                break;
            }
        }
        return exist;
    }
    
    /**
     * 对用户输入内容进行编码
     * @param obj obj
     * @return    result
     */
    public static String encodeForLog(Object obj)
    {
        if (obj == null)
        {
            return "null";
        }
        String msg = obj.toString();
        int length = msg.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
        {
            char ch = msg.charAt(i);
            
            // 将\r\n替换成'_'
            if (ch == '\r' || ch == '\n')
            {
                ch = '_';
            }
            sb.append(Character.valueOf(ch));
        }
        return sb.toString();
    }
    
   
    /**
     * 限制日志中最大打印100
     * @param str
     * @return
     */
    public static String limit100FowShow(String str)
    {
        if (null == str)
        {
            return "null";
        }
        if (str.length() > MAX_STINRG)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(str.substring(0, MAX_STINRG));
            sb.append("...");
            return encodeForLog(sb.toString());
        }
        return encodeForLog(str);
        
    }

    /**
     * IP替换
     * @param ip
     * @return
     */
    public static String ipReplace(String ip)
    {
        if (null == ip)
        {
            return "null";
        }
        
        String[] ipArray = ip.split("\\.");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ipArray.length; i++)
        {
            if (i != 0)
            {
                sb.append(".");
            }
            
            if (i < 2)
            {
                sb.append("***");
            }
            else
            {
                sb.append(ipArray[i]);
            }
           
        }
        
        return sb.toString();
    }
    
    
}
