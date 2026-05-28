package com.cleanshopping.presentation

import com.cleanshopping.application.user.UserService
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {



}