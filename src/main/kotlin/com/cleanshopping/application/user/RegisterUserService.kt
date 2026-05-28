package com.cleanshopping.application.user

import com.cleanshopping.application.user.dto.RegisterUserCommand
import com.cleanshopping.application.user.dto.RegisterUserResult
import com.cleanshopping.domain.user.User
import com.cleanshopping.domain.user.vo.Address
import com.cleanshopping.domain.user.vo.Email
import com.cleanshopping.domain.user.vo.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {


    @Transactional
    fun register(request: RegisterUserCommand): RegisterUserResult {
        if(userRepository.existsByEmail(request.email)){
            throw
        }


        val user = User.create(address = Address(request.zipCode, request.roadAddress, request.detailAddress),
            Profile(request.name, request.introduction), email = Email(request.email)
        )

        val save = userRepository.save(user)
        return RegisterUserResult(save.id, save.email.value)
    }
}
