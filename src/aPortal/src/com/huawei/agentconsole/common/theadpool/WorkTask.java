
package com.huawei.agentconsole.common.theadpool;

public interface WorkTask extends Runnable
{

    /**
     * 如果存在相同键值的任务则交由同一线程处理.
     * @return 任务键值
     */
    int getKey();

}
