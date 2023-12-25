package com.example.demo.common.redis.repository

import com.example.demo.common.redis.entity.RefreshTokenInfoRedis
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenInfoRepositoryRedis : CrudRepository<RefreshTokenInfoRedis, String>{
    fun deleteByUserId(userId: String): RefreshTokenInfoRedis?
}