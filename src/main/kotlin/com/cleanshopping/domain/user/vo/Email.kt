package com.cleanshopping.domain.user.vo

@JvmInline
value class Email(val value: String) {
    init {
        require(value.matches(EMAIL_REGEX)) {
            "올바른 이메일 형식이 아닙니다."
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
    }
}