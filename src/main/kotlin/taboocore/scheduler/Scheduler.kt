package taboocore.scheduler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import taboocore.platform.TabooCoreExecutor
import taboolib.common.platform.service.PlatformExecutor

/**
 * TabooCore 协程调度 DSL
 *
 * 使用示例：
 * ```kotlin
 * // 异步延迟执行
 * submit(async = true, delay = 20L) { ... }
 *
 * // 同步周期执行（每 20 tick）
 * submit(period = 20L) { cancel() }
 *
 * // 挂起函数支持
 * launchAsync { delay(1000); doSomething() }
 * ```
 */

/**
 * 提交一个任务
 * @param async 是否异步执行（默认 false = 同步，通过服务端 tick 队列执行）
 * @param delay 延迟 ticks（1 tick = 50ms）
 * @param period 周期 ticks，> 0 表示重复执行，0 表示只执行一次
 * @param task 任务体，可通过 [SchedulerTask.cancel] 取消自身
 * @return 任务句柄
 */
fun submit(
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    task: SchedulerTask.() -> Unit
): SchedulerTask {
    val schedulerTask = SchedulerTask()
    val delayMs = delay * 50L
    val periodMs = period * 50L
    if (async) {
        schedulerTask.job = TabooCoreScope.launch {
            if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
            if (period > 0) {
                while (isActive && !schedulerTask.cancelled) {
                    schedulerTask.task()
                    kotlinx.coroutines.delay(periodMs)
                }
            } else {
                schedulerTask.task()
            }
        }
    } else {
        // 同步任务通过 PlatformExecutor 提交到主线程队列
        val executor = TabooCoreExecutor.instance ?: error("TabooCoreExecutor 尚未初始化")
        val runnable = PlatformExecutor.PlatformRunnable(
            now = false,
            async = false,
            delay = delay,
            period = period,
            executor = {
                if (!schedulerTask.cancelled) {
                    schedulerTask.task()
                    if (schedulerTask.cancelled) {
                        cancel()
                    }
                }
            }
        )
        val platformTask = executor.submit(runnable)
        schedulerTask.job = (platformTask as? TabooCoreExecutor.TabooCorePlatformTask)?.job
    }
    return schedulerTask
}

/**
 * 在协程上下文中异步执行挂起函数
 * @param delay 延迟 ticks（1 tick = 50ms）
 * @param block 协程体
 * @return 协程 Job
 */
fun launchAsync(
    delay: Long = 0,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val delayMs = delay * 50L
    return TabooCoreScope.launch {
        if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
        block()
    }
}

/**
 * 在下一个服务端 tick 执行（同步）
 * @param task 要执行的操作
 */
fun nextTick(task: () -> Unit) {
    val executor = TabooCoreExecutor.instance ?: error("TabooCoreExecutor 尚未初始化")
    val runnable = PlatformExecutor.PlatformRunnable(
        now = false,
        async = false,
        delay = 0,
        period = 0,
        executor = { task() }
    )
    executor.submit(runnable)
}

/**
 * 挂起等待指定 ticks（在协程中使用）
 * @param ticks 等待的 tick 数（1 tick = 50ms）
 */
suspend fun waitTick(ticks: Long) {
    kotlinx.coroutines.delay(ticks * 50L)
}
