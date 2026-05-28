package com.cleanshopping.application.user

import com.cleanshopping.domain.user.User

interface UserRepository {

    fun save(user: User)
    fun findById(id: Long): User?
}