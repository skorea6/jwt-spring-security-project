package com.example.demo.common.listener

import com.example.demo.common.annotation.CreatedIp
import com.example.demo.common.annotation.LastModifiedIp
import com.example.demo.util.BrowserInfo
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class IpAddressListener {

    @PrePersist
    fun setCreatedIp(target: Any) {
        setIp(target, CreatedIp::class.java)
    }

    @PreUpdate
    fun setLastModifiedIp(target: Any) {
        setIp(target, LastModifiedIp::class.java)
    }

    private fun setIp(target: Any, annotationClass: Class<out Annotation>) {
        val ip: String = getCurrentIp()
//        val filterIsInstance: List<KMutableProperty1<Any, *>> = target::class.memberProperties
//            .filterIsInstance<KMutableProperty1<Any, *>>()
//
//        target::class.memberProperties.forEach {
//            val findAnnotation: LastModifiedIp? = it.findAnnotation<LastModifiedIp>()
//            println(findAnnotation)
//        }
//
//        for (kMutableProperty1 in filterIsInstance) {
//            println(kMutableProperty1)
//        }

        target::class.memberProperties
            .filterIsInstance<KMutableProperty1<Any, *>>()
            .firstOrNull { it.findAnnotation(annotationClass) != null }
            ?.let {
                it.isAccessible = true
                it.setter.call(target, ip)
            }
    }

    private fun KMutableProperty1<Any, *>.findAnnotation(annotationClass: Class<out Annotation>): Annotation? {
        return this.annotations.firstOrNull { annotationClass.isInstance(it) }
    }

    private fun getCurrentIp(): String {
        val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes ?: return ""
        val ipAddress: String = BrowserInfo().getIpAddress(attrs.request)
        return ipAddress
    }
}