package com.example.demo.common.domain

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime


@EntityListeners(AuditingEntityListener::class) // auditing
@MappedSuperclass
abstract class AuditingFields : IpAuditingFields() {
    /**
     * [abstract class에 맞게 멤버변수들은 `protected`여야 한다]
     * 회원 엔티티가 이 부분을 직접 참조해야 하므로 protected
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @CreatedDate
    @Column(nullable = false, updatable = false)
    open var createdAt: LocalDateTime? = null // 생성일시
        protected set

    @CreatedBy
    @Column(updatable = false, length = 100)
    open var createdBy: String? = null // 생성자
        protected set

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @LastModifiedDate
    @Column(nullable = false)
    open var modifiedAt: LocalDateTime? = null // 수정일시
        protected set

    @LastModifiedBy
    @Column(length = 100)
    open var modifiedBy: String? = null // 수정자
        protected set
}