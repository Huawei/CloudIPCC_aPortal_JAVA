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

public class MybatisForMySql
{
 private static final Logger LOG = LoggerFactory.getLogger(Mybatis.class);
    
    /**
     * SYSDB数据库连接工厂
     */
    private static SqlSessionFactory sysdbSqlSessionFactory = null;
    
    /**
     * mybatis的配置文件所在路径
     */
    private static String sysdbMybatisConfigfilePath = "";
    
    
    /**
     * 初始化sysdb是否成功
     */
    private static boolean isInitDBOk = false;
    
    /**
     * 初始化SYSDB数据库配置信息
     */
    public static void initSYSDBSqlSessionFactory()
    {
        //获取文件
        sysdbMybatisConfigfilePath = "/sysdbMybatisConfiguration.xml";
        
        LOG.info(LogUtils.encodeForLog("[aconsole] mybatis file path is " + sysdbMybatisConfigfilePath));
        
        establishSYSDBSqlSessionFactory(); 
    }
    
    
    /**
     * 初始化数据库配置信息 SYSDBti
     */
    private static void establishSYSDBSqlSessionFactory()
    {
        LOG.info("[aconsole] begin to establishSYSDBSqlSessionFactory.");
        
        Reader reader = null;
        
        Properties properties = new Properties();
        
        /**
         * 针对不同类型数据库进行初始化配置目前支持oracle，db2，其他暂时不支持
         */
        if (CommonConstant.DB_TYPE_MYSQL.equalsIgnoreCase(ConfigProperties.getKey(ConfigList.SYSDB,
                "SYSDB_DBTYPE")))
        {
            properties.put("SYSDB_DB_CONNECT_DRIVER", CommonConstant.MYSQL_DRIVER);
        }
        else
        {
            LOG.error("[aconsole] This kind of DB not support");
            isInitDBOk = false;
            return;
        }
        
        //如果数据库类型匹配，读取配置文件中的账户密码连接字符串等信息
        properties.put("SYSDB_DB_CONNECT_URL",
                ConfigProperties.getKey(ConfigList.SYSDB, "SYSDB_DB_CONNECT_URL"));
        
        properties.put("SYSDB_DB_CONNECT_NAME",
                ConfigProperties.getKey(ConfigList.SYSDB,
                        "SYSDB_DB_CONNECT_NAME"));
        properties.put("SYSDB_DB_CONNECT_PASSWORD",
                ConfigProperties.getKey(ConfigList.SYSDB,
                        "SYSDB_DB_CONNECT_PASSWORD"));
        properties.put("SYSDB_DBTYPE",
                ConfigProperties.getKey(ConfigList.SYSDB, "SYSDB_DBTYPE"));
        try
        {
            try
            {
                //读取配置进行初始化
                reader = Resources.getResourceAsReader(sysdbMybatisConfigfilePath);
                sysdbSqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "sysdb", properties);
                LOG.info("[aconsole] SYSDB init sysdbMybatisConfiguration.xml success.");
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
            LOG.error("[aconsole] SYSDB init sysdbMybatisConfiguration.xml Failed. IOException : {}", LogUtils.encodeForLog(ex.getMessage()));
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
                    LOG.error("[aconsole] close reader has exception: {}", LogUtils.encodeForLog(e.getMessage()));
                }
            }
        }
    }
    
    /**
     * sysdb是否初始化成功
     * @return
     */
    public static boolean isInitDBOk()
    {
        return isInitDBOk;
    }
    
    /**
     * get sysdb数据库回话工厂对象
     * @return
     */
    public static SqlSessionFactory getSYSDBSqlSessionFactory()
    {
        if (null == sysdbSqlSessionFactory)
        {
            establishSYSDBSqlSessionFactory();
        }
        return sysdbSqlSessionFactory;
    }
}
