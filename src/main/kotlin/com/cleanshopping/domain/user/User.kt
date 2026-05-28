package com.cleanshopping.domain.user

import com.cleanshopping.domain.user.vo.Address
import com.cleanshopping.domain.user.vo.Email
import com.cleanshopping.domain.user.vo.Profile

class User private constructor(
    val id: Long? = null,
    val email: Email,
    val profile: Profile,
    val address: Address

) {

    companion object {
        fun create(address: Address, profile: Profile, email: Email): User {
            return User(address = address, profile = profile, email = email)
        }

        fun reconstitute(id: Long, address: Address, profile: Profile, email: Email): User {
            return User(id = id, email = email, profile = profile, address = address)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        if (id == null || other.id == null) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }
}