package com.cleanshopping.infrastructure.user

import com.cleanshopping.domain.user.User
import com.cleanshopping.domain.user.vo.Address
import com.cleanshopping.domain.user.vo.Email
import com.cleanshopping.domain.user.vo.Profile
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id @GeneratedValue val id: Long? = null,
    val email: String,
    val name: String,
    val introduction: String,
    val zipCode: String,
    val roadAddress: String,
    val detailAddress: String
) {
    fun toDomain(): User = User.reconstitute(
        id = id!!,
        email = Email(email),
        profile = Profile(name, introduction),
        address = Address(zipCode, roadAddress, detailAddress)
    )

    companion object {
        fun from(user: User) = UserJpaEntity(
            id = user.id,
            email = user.email.value,
            name = user.profile.name,
            introduction = user.profile.introduction,
            zipCode = user.address.zipCode,
            roadAddress = user.address.roadAddress,
            detailAddress = user.address.detailAddress
        )
    }
}