package com.example.demo.common.domain

import com.example.demo.common.annotation.CreatedIp
import com.example.demo.common.annotation.LastModifiedIp
import com.example.demo.common.listener.IpAddressListener
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass


@EntityListeners(IpAddressListener::class) // auditing
@MappedSuperclass
abstract class IpAuditingFields {
    @CreatedIp
    @Column(updatable = false, length = 50)
    open var createdIp: String? = null
        protected set

    @LastModifiedIp
    @Column(length = 50)
    open var lastModifiedIp: String? = null
        protected set
}
