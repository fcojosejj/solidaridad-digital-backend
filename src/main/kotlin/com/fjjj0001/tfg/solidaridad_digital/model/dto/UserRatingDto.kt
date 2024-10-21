package com.fjjj0001.tfg.solidaridad_digital.model.dto

import com.fjjj0001.tfg.solidaridad_digital.model.UserRating
import java.io.Serializable


data class UserRatingDto(
    var ratedByUsername: String? = null,
    var ratingUserUsername: String? = null,
    var rating: Int? = null,
    var message: String? = null,
    var id: Long? = null
) : Serializable {
    companion object{
        fun fromUserRating(rating: UserRating): UserRatingDto {
            return UserRatingDto(
                ratedByUsername = rating.ratedBy.username,
                rating = rating.rating,
                message = rating.message,
                id = rating.id,
            )
        }
    }
}