package com.fjjj0001.tfg.solidaridad_digital.util.exceptions

class InvalidPasswordException: Exception() {
    override val message: String
        get() = "Password must contain at least one letter, one number and one special character"
}