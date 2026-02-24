package taboocore.event

import taboolib.common.event.InternalEvent
import taboolib.common.platform.ProxyPlayer

/**
 * 玩家加入服务器事件
 *
 * @property player 加入的玩家
 */
class PlayerJoinEvent(val player: ProxyPlayer) : InternalEvent()

/**
 * 玩家退出服务器事件
 *
 * @property player 退出的玩家
 */
class PlayerQuitEvent(val player: ProxyPlayer) : InternalEvent()

/**
 * 服务器 Tick 事件
 *
 * @property tickCount 当前 Tick 计数
 */
class ServerTickEvent(val tickCount: Int) : InternalEvent()
