
package com.huawei.agentconsole.dao.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.agentconsole.common.exception.CommonException;
import com.huawei.agentconsole.common.util.LogUtils;
import com.huawei.agentconsole.dao.init.Mybatis;
import com.huawei.agentconsole.dao.init.MybatisForMySql;
import com.huawei.agentconsole.dao.intf.ServerStatusDao;
import com.huawei.agentconsole.dao.intf.saas.MysqlServerStatusDao;

public class ServerStatusService
{

    private static final Logger log = LoggerFactory
            .getLogger(ServerStatusService.class);

    private static ServerStatusService instance = new ServerStatusService();

    ServerStatusService()
    {

    }

    public static ServerStatusService getInstance()
    {
        return instance;
    }

    /**
     * 检测数据库连接状态
     * 
     * @return true代表连接 false代表断连
     */
    public boolean checkDBStatus()
    {
        SqlSessionFactory factory = Mybatis.getUIDBSqlSessionFactory();

        if (null == factory)
        {
            return false;
        }
        SqlSession sqlSession = null;

        try
        {
            try
            {
                sqlSession = factory.openSession();

                ServerStatusDao dao = sqlSession
                        .getMapper(ServerStatusDao.class);

                int result = dao.checkDBStatus();

                if (result > 0)
                {
                    return true;
                }
            }
            catch (RuntimeException e)
            {
                throw new CommonException(e);
            }
            catch (Exception e)
            {
                throw new CommonException(e);
            }

        }
        catch (CommonException e)
        {
            log.error("Db checked failed! The error is {}",
                    LogUtils.encodeForLog(e.getMessage()));
        }
        finally
        {
            if (null != sqlSession)
            {
                sqlSession.close();
            }
        }

        return false;
    }

    /**
     * 检测mysql数据库连接状态
     * 
     * @return
     */
    public boolean checkMysqlDBStatus()
    {
        SqlSessionFactory factory = MybatisForMySql.getSYSDBSqlSessionFactory();
        if (null == factory)
        {
            return false;
        }
        SqlSession sqlSession = null;

        try
        {
            try
            {
                sqlSession = factory.openSession();
                MysqlServerStatusDao dao = sqlSession.getMapper(MysqlServerStatusDao.class);

                int result = dao.checkDBStatus();
                if (result > 0)
                {
                    return true;
                }
            }
            catch (RuntimeException e)
            {
                throw new CommonException(e);
            }
            catch (Exception e)
            {
                throw new CommonException(e);
            }

        }
        catch (CommonException e)
        {
            log.error("Db checked failed! The error is {}",
                    LogUtils.encodeForLog(e.getMessage()));
        }
        finally
        {
            if (null != sqlSession)
            {
                sqlSession.close();
            }
        }
        return false;
    }

}
