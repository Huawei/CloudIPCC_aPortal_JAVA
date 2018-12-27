package com.huawei.agentconsole.common.theadpool;


public interface WorkThreadMBean
{

    /**
     * 获取队列中等待处理的任务数.
     * @return 队列中等待处理的任务数
     */
    int getQueueSize();

    /**
     * 获取已经处理完成的任务数.
     * @return 已经处理完成的任务数
     */
    int getFinishedTask();

    /**
     * 获取关联的键的数量.
     * @return 关联的键的数量
     * @since 3.6C10
     */
    int getReferenceKeySize();

}
