package taboocore.reflect

import net.minecraft.server.level.ServerPlayer
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

/**
 * NMS 反射扩展
 * 提供针对 Minecraft 服务端常用对象的反射快捷方法
 */

/**
 * 获取 ServerPlayer 的 locale 语言字段
 * 该字段在部分版本中为 private，需通过反射访问
 */
fun ServerPlayer.getLanguage(): String {
    return try {
        getPrivate<String>("language")
    } catch (_: Exception) {
        "en_us"
    }
}

/**
 * 获取 ServerPlayer 的网络连接对象
 * @return Connection 实例
 */
fun ServerPlayer.getConnection(): Any? {
    return try {
        val connKlass = this.connection::class
        val prop = connKlass.memberProperties.find { it.name == "connection" }
            ?: this.connection::class.java.superclass.kotlin.memberProperties.find { it.name == "connection" }
        prop?.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (prop as? KProperty1<Any, *>)?.get(this.connection)
    } catch (_: Exception) {
        null
    }
}

/**
 * 从 KClass 获取静态属性值（Kotlin companion object 属性或 Java 静态字段）
 * @param klass 目标 KClass
 * @param name 属性名
 * @return 属性值
 */
@Suppress("UNCHECKED_CAST")
fun <T> getStaticProperty(klass: KClass<*>, name: String): T {
    // 优先查找 companion object 中的属性
    val companion = klass.companionObjectInstance
    if (companion != null) {
        val prop = klass.companionObject?.memberProperties?.find { it.name == name }
        if (prop != null) {
            prop.isAccessible = true
            return (prop as KProperty1<Any, T>).get(companion)
        }
    }
    // 回退：在类的成员属性中查找并通过 javaField 读取静态字段
    val prop = klass.memberProperties.find { it.name == name }
    if (prop != null) {
        prop.isAccessible = true
        val field = prop.javaField
        if (field != null) {
            field.isAccessible = true
            return field.get(null) as T
        }
    }
    throw IllegalArgumentException("静态属性不存在: ${klass.qualifiedName}.$name")
}

/**
 * 设置 KClass 的静态属性值
 * @param klass 目标 KClass
 * @param name 属性名
 * @param value 要设置的值
 */
@Suppress("UNCHECKED_CAST")
fun setStaticProperty(klass: KClass<*>, name: String, value: Any?) {
    // 优先查找 companion object 中的可变属性
    val companion = klass.companionObjectInstance
    if (companion != null) {
        val prop = klass.companionObject?.memberProperties?.find { it.name == name }
        if (prop is KMutableProperty1<*, *>) {
            prop.isAccessible = true
            (prop as KMutableProperty1<Any, Any?>).set(companion, value)
            return
        }
    }
    // 回退：在类的成员属性中查找并通过 javaField 写入静态字段
    val prop = klass.memberProperties.find { it.name == name }
    if (prop != null) {
        prop.isAccessible = true
        if (prop is KMutableProperty1<*, *>) {
            (prop as KMutableProperty1<Any?, Any?>).set(null, value)
            return
        }
        val field = prop.javaField
        if (field != null) {
            field.isAccessible = true
            field.set(null, value)
            return
        }
    }
    throw IllegalArgumentException("静态属性不存在或不可写: ${klass.qualifiedName}.$name")
}

/**
 * 调用 KClass 上的静态方法（companion object 函数）
 * @param klass 目标 KClass
 * @param name 函数名
 * @param args 参数
 * @return 返回值
 */
@Suppress("UNCHECKED_CAST")
fun <T> invokeStaticFunction(klass: KClass<*>, name: String, vararg args: Any?): T? {
    val companion = klass.companionObjectInstance
        ?: throw IllegalArgumentException("${klass.qualifiedName} 没有 companion object")
    val func = klass.companionObject?.memberFunctions?.find {
        it.name == name && it.parameters.size - 1 == args.size
    } ?: throw IllegalArgumentException(
        "函数不存在: ${klass.qualifiedName}.Companion.$name (参数数量=${args.size})"
    )
    func.isAccessible = true
    return func.call(companion, *args) as T?
}
