
package com.huawei.agentconsole.common.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





public class JsonUtils
{
    private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);
    
    /**
     * json to map method
     * @param json
     * @return map
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> jsonToMap(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            HashMap<String, Object> result = objectMapper.readValue(json, HashMap.class);
            return result;
        }
        catch (IOException e)
        {
            LOG.error("String to object failed. the error is {}", e.getMessage());
            return null;
        }
        
    }
    
    
    /**
     * Object to json
     * @param object object
     * @return json Json String
     * @throws IOException 
     */
    public static String beanToJson(Object object)
    {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        JsonGenerator gen = null;
        String json = "";
        try 
        {
            gen = new JsonFactory().createJsonGenerator(writer);
            mapper.writeValue(gen, object);
            json = writer.toString();
        } 
        catch (IOException e) 
        {
            LOG.error("object to json string failed. the error is {}", e.getMessage());
        }
        finally
        {
            if(gen != null)
            {
                try 
                {
                    gen.close();
                } 
                catch (IOException e) 
                {
                    LOG.error("close Json generator failed. the error is {}", e.getMessage());
                }
            }
            try 
            {
                writer.close();
            } catch (IOException e) 
            {
                LOG.error("close StringWriter failed. the error is {}", e.getMessage());
            }
        }
        return json;
    }
}
