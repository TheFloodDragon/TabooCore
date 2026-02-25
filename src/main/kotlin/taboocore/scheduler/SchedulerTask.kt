package taboocore.scheduler

import kotlinx.coroutines.Job

/**
 * 调度任务句柄
 * 持有协程 Job 引用，支持取消和状态查询
 */
class SchedulerTask {

    /** 关联的协程 Job */
    var job: Job? = null

    /** 任务是否已被取消 */
    var cancelled: Boolean = false
        private set

    /** 取消此任务 */
    fun cancel() {
        cancelled = true
        job?.cancel()
    }

    /** 任务是否还在运行 */
    val isRunning: Boolean
        get() = !cancelled && (job?.isActive ?: false)
}
