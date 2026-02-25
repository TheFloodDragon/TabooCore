package taboocore.scheduler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 协程扩展：在 TabooCore scope 中运行协程任务
 */

/**
 * 在当前 CoroutineScope 中启动协程，延迟指定 ticks 后执行
 * @param ticks 延迟的 tick 数（1 tick = 50ms）
 * @param block 协程体
 * @return 协程 Job
 */
fun CoroutineScope.launchDelayed(ticks: Long, block: suspend CoroutineScope.() -> Unit): Job {
    return launch {
        kotlinx.coroutines.delay(ticks * 50L)
        block()
    }
}

/**
 * 挂起等待指定 ticks
 * @param ticks 等待的 tick 数（1 tick = 50ms）
 */
suspend fun tickDelay(ticks: Long) {
    kotlinx.coroutines.delay(ticks * 50L)
}

/**
 * 在当前 CoroutineScope 中周期性执行任务
 * @param period 周期 ticks（1 tick = 50ms）
 * @param initialDelay 首次执行前的延迟 ticks
 * @param block 每次执行的协程体，返回 false 停止周期
 * @return 协程 Job
 */
fun CoroutineScope.launchTimer(
    period: Long,
    initialDelay: Long = 0,
    block: suspend CoroutineScope.() -> Boolean
): Job {
    return launch {
        if (initialDelay > 0) kotlinx.coroutines.delay(initialDelay * 50L)
        while (true) {
            if (!block()) break
            kotlinx.coroutines.delay(period * 50L)
        }
    }
}
