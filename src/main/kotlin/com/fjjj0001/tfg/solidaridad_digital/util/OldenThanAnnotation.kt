package com.fjjj0001.tfg.solidaridad_digital.util

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.LocalDate
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [OlderThanValidator::class])
annotation class OlderThan(
    val message: String = "The date is not older than the specified years!",
    val age: Int = 18,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class OlderThanValidator : ConstraintValidator<OlderThan, LocalDate> {
    private var age: Int = 18

    override fun initialize(constraintAnnotation: OlderThan?) {
        if (constraintAnnotation != null) {
            age = constraintAnnotation.age
        }
    }

    override fun isValid(value: LocalDate?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return false
        }

        return value.isBefore(LocalDate.now().minusYears(age.toLong()))
    }

}
