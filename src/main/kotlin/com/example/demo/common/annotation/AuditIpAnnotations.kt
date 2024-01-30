package com.example.demo.common.annotation

import java.lang.annotation.Inherited


// property, field 둘다 붙여주어야함. property 붙이니까 listener에서 가져오는 로직 성공
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class CreatedIp

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class LastModifiedIp
