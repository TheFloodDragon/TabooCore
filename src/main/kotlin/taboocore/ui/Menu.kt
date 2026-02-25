package taboocore.ui

import java.util.concurrent.ConcurrentHashMap

/**
 * 菜单顶层接口
 * 与 TabooLib bukkit-ui 的 Menu 接口保持一致
 */
interface Menu {

    /** 标题 */
    var title: String

    /** 构建菜单（返回内部容器对象） */
    fun build(): Any

    companion object {

        private val impl = ConcurrentHashMap<Class<*>, Class<*>>()

        init {
            impl[Chest::class.java] = taboocore.ui.impl.ChestImpl::class.java
            impl[PageableChest::class.java] = taboocore.ui.impl.PageableChestImpl::class.java
            impl[StorableChest::class.java] = taboocore.ui.impl.StorableChestImpl::class.java
        }

        /** 注册实现 */
        fun registerImplementation(clazz: Class<*>, implementation: Class<*>) {
            impl[clazz] = implementation
        }

        /** 获取实现 */
        fun getImplementation(clazz: Class<*>): Class<*> {
            return impl[clazz] ?: error("未能找到 ${clazz.name} 的实现")
        }
    }
}
