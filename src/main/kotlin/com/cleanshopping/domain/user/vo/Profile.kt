package com.cleanshopping.domain.user.vo

class Profile(
    val name: String,
    val introduction: String
) {
    init {
        require(name.isNotBlank()) {
            "이름은 비어 있을 수 없습니다"
        }
    }
}