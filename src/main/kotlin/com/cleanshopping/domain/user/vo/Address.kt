package com.cleanshopping.domain.user.vo

class Address(
    val zipCode: String,
    val roadAddress: String,
    val detailAddress: String
) {
    init {
        require(zipCode.matches(ZIP_CODE_REGEX)) {
            "올바른 우편번호 형식이 아닙니다. (5자리 숫자)"
        }
        require(roadAddress.isNotBlank()) {
            "기본 주소는 비어 있을 수 없습니다."
        }
    }

    companion object {
        private val ZIP_CODE_REGEX = Regex("^\\d{5}$")
    }
}
