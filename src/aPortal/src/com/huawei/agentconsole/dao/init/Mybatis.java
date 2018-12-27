
package com.huawei.agentconsole.dao.init;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.config.ConfigList;
import com.huawei.agentconsole.common.config.ConfigProperties;
import com.huawei.agentconsole.common.constant.CommonConstant;
import com.huawei.agentconsole.common.exception.CommonException;
import com.huawei.agentconsole.common.util.LogUtils;

/**
 * 
 * <p>Title:初始化mybatis的数据库连接  </p>
 * <p>Description:初始化mybatis的数据库连接  </p>
 * <pre>  </pre>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Huawei Technologies Co.</p>
 * @author l00357702
 * @version V1.0 2018年7月31日
 * @since
 */
public class Mybatis
{

    private static final Logger LOG = LoggerFactory.getLogger(Mybatis.class);
    
    /**
     * UIDB数据库连接工厂
     */
    private static SqlSessionFactory uidbSqlSessionFactory = null;
    
    /**
     * mybatis的配置文件所在路径
     */
    private static String uidbMybatisConfigfilePath = "";
    
    
    /**
     * 初始化uidb是否成功
     */
    private static boolean isInitDBOk = false;
    
    /**
     * 初始化UIDB数据库配置信息
     */
    public static void initUIDBSqlSessionFactory()
    {
        //获取文件
        uidbMybatisConfigfilePath = "/uidbMybatisConfiguration.xml";
        
        LOG.info(LogUtils.encodeForLog("mybatis file path is " + uidbMybatisConfigfilePath));
        
        establishUIDBSqlSessionFactory(); 
    }
    
    
    /**
     * 初始化数据库配置信息 UIDBti
     */
    private static void establishUIDBSqlSessionFactory()
    {
        LOG.info("begin to establishUIDBSqlSessionFactory.");
        
        Reader reader = null;
        
        Properties properties = new Properties();
        
        /**
         * 针对不同类型数据库进行初始化配置目前支持oracle，db2，其他暂时不支持
         */
        if (CommonConstant.DB_TYPE_ORACLE.equalsIgnoreCase(ConfigProperties.getKey(ConfigList.UIDB,
                "UIDB_DBTYPE")))
        {
            properties.put("UIDB_DB_CONNECT_DRIVER", CommonConstant.ORACLE_DRIVER);
        }
        else if (CommonConstant.DB_TYPE_DB2.equalsIgnoreCase(ConfigProperties.getKey(ConfigList.UIDB, 
                "UIDB_DBTYPE")))
        {
            properties.put("UIDB_DB_CONNECT_DRIVER", CommonConstant.DB2_DRIVER);
        }
        else
        {
            LOG.error("This kind of DB not support");
            isInitDBOk = false;
            return;
        }
        
        //如果数据库类型匹配，读取配置文件中的账户密码连接字符串等信息
        properties.put("UIDB_DB_CONNECT_URL",
                ConfigProperties.getKey(ConfigList.UIDB, "UIDB_DB_CONNECT_URL"));
        
        properties.put("UIDB_DB_CONNECT_NAME",
                ConfigProperties.getKey(ConfigList.UIDB,
                        "UIDB_DB_CONNECT_NAME"));
        properties.put("UIDB_DB_CONNECT_PASSWORD",
                ConfigProperties.getKey(ConfigList.UIDB,
                        "UIDB_DB_CONNECT_PASSWORD"));
        properties.put("UIDB_DBTYPE",
                ConfigProperties.getKey(ConfigList.UIDB, "UIDB_DBTYPE"));
        try
        {
            try
            {
                //读取配置进行初始化
                reader = Resources.getResourceAsReader(uidbMybatisConfigfilePath);
                uidbSqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "uidb", properties);
                LOG.info("UIDB init uidbMybatisConfiguration.xml success.");
                isInitDBOk = true;
            }  
            catch (IOException ex)
            {
                throw new CommonException(ex);
            } 
            catch (Exception e)
            {
                throw new CommonException(e);
            }
        }
        catch (CommonException ex)
        {
            LOG.error("UIDB init uidbMybatisConfiguration.xml Failed. IOException : {}", LogUtils.encodeForLog(ex.getMessage()));
            isInitDBOk = false;
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    LOG.error("close reader has exception: {}", LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
    }
    
    /**
     * uidb是否初始化成功
     * @return
     */
    public static boolean isInitDBOk()
    {
        return isInitDBOk;
    }
    
    /**
     * get uidb数据库回话工厂对象
     * @return
     */
    public static SqlSessionFactory getUIDBSqlSessionFactory()
    {
        if (null == uidbSqlSessionFactory)
        {
            establishUIDBSqlSessionFactory();
        }
        return uidbSqlSessionFactory;
    }
}
