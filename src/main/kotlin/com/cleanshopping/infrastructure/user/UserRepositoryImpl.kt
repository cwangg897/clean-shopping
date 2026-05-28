package com.cleanshopping.infrastructure.user

import com.cleanshopping.application.user.UserRepository
import com.cleanshopping.domain.user.User

class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {

    override fun save(user: User) {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): User? {
        TODO("Not yet implemented")
    }
}