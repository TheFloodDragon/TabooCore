package taboocore.reflect

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

/**
 * Kotlin 原生反射工具集（基于 kotlin-reflect）
 *
 * 使用示例：
 * ```kotlin
 * // 读取私有属性
 * val value = player.getPrivate<Int>("someField")
 *
 * // 设置私有属性
 * player.setPrivate("someField", 42)
 *
 * // 调用私有方法
 * player.invokeMethod<String>("privateMethod", arg1, arg2)
 *
 * // 获取 class 的所有属性（含父类）
 * MyClass::class.allProperties
 *
 * // 获取 class 的所有函数（含父类）
 * MyClass::class.allFunctions
 * ```
 */

// ========================= Any 扩展 =========================

/**
 * 获取私有属性的值
 * @param name 属性名
 * @return 属性值
 * @throws IllegalArgumentException 属性不存在
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.getPrivate(name: String): T {
    val prop = ReflectCache.getProperty(this::class, name)
    return (prop as KProperty1<Any, T>).get(this)
}

/**
 * 设置私有属性的值
 * @param name 属性名
 * @param value 要设置的值
 * @throws IllegalArgumentException 属性不存在或属性不可变
 */
@Suppress("UNCHECKED_CAST")
fun Any.setPrivate(name: String, value: Any?) {
    val prop = ReflectCache.getProperty(this::class, name)
    if (prop is KMutableProperty1<*, *>) {
        (prop as KMutableProperty1<Any, Any?>).set(this, value)
    } else {
        // 对于 val 属性，回退到 Java 反射的 javaField 来强制写入
        val javaField = prop.javaField
            ?: throw IllegalStateException("属性 ${this::class.qualifiedName}.$name 不可变且无 backing field")
        javaField.isAccessible = true
        javaField.set(this, value)
    }
}

/**
 * 调用私有方法（自动匹配参数数量）
 * @param name 方法名
 * @param args 方法参数
 * @return 方法返回值
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.invokeMethod(name: String, vararg args: Any?): T? {
    val func = ReflectCache.getFunction(this::class, name, args.size)
    return func.call(this, *args) as T?
}

// ========================= KClass 扩展 =========================

/**
 * 获取所有属性（包含父类属性）
 */
val KClass<*>.allProperties: Collection<KProperty1<*, *>>
    get() = memberProperties

/**
 * 获取所有函数（包含父类函数）
 */
val KClass<*>.allFunctions: Collection<KFunction<*>>
    get() = memberFunctions

/**
 * 按名称查找属性（包含父类）
 * @param name 属性名
 * @return 找到的属性，不存在则返回 null
 */
fun KClass<*>.findProperty(name: String): KProperty1<*, *>? {
    return memberProperties.find { it.name == name }?.apply { isAccessible = true }
}

/**
 * 按名称查找函数（包含父类）
 * @param name 函数名
 * @return 找到的函数，不存在则返回 null
 */
fun KClass<*>.findFunction(name: String): KFunction<*>? {
    return memberFunctions.find { it.name == name }?.apply { isAccessible = true }
}

/**
 * 按名称和参数数量查找函数（包含父类）
 * @param name 函数名
 * @param paramCount 参数数量（不含 receiver）
 * @return 找到的函数，不存在则返回 null
 */
fun KClass<*>.findFunction(name: String, paramCount: Int): KFunction<*>? {
    // KFunction.parameters 包含 instance receiver，所以实际参数数 = parameters.size - 1
    return memberFunctions.find {
        it.name == name && it.parameters.size - 1 == paramCount
    }?.apply { isAccessible = true }
}

// ========================= 缓存反射 =========================

/**
 * 反射缓存
 * 缓存 KProperty / KFunction 对象以提升重复反射操作的性能
 */
object ReflectCache {

    private val propertyCache = ConcurrentHashMap<String, KProperty1<*, *>>()
    private val functionCache = ConcurrentHashMap<String, KFunction<*>>()

    /**
     * 获取属性（带缓存，含父类搜索）
     * @param klass 目标 KClass
     * @param name 属性名
     * @return 可访问的 KProperty1 对象
     * @throws IllegalArgumentException 属性在类层级中不存在
     */
    fun getProperty(klass: KClass<*>, name: String): KProperty1<*, *> {
        val key = "${klass.qualifiedName}#$name"
        return propertyCache.getOrPut(key) {
            val prop = klass.memberProperties.find { it.name == name }
                ?: throw IllegalArgumentException("属性不存在: ${klass.qualifiedName}.$name")
            prop.isAccessible = true
            prop
        }
    }

    /**
     * 获取函数（带缓存，含父类搜索）
     * @param klass 目标 KClass
     * @param name 函数名
     * @param paramCount 参数数量（不含 receiver）
     * @return 可访问的 KFunction 对象
     * @throws IllegalArgumentException 函数在类层级中不存在
     */
    fun getFunction(klass: KClass<*>, name: String, paramCount: Int): KFunction<*> {
        val key = "${klass.qualifiedName}#$name/$paramCount"
        return functionCache.getOrPut(key) {
            val func = klass.memberFunctions.find {
                it.name == name && it.parameters.size - 1 == paramCount
            } ?: throw IllegalArgumentException(
                "函数不存在: ${klass.qualifiedName}.$name (参数数量=$paramCount)"
            )
            func.isAccessible = true
            func
        }
    }

    /** 清除所有缓存 */
    fun clearCache() {
        propertyCache.clear()
        functionCache.clear()
    }
}
