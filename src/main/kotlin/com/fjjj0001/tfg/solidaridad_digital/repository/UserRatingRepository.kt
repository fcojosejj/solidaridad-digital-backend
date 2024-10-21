package com.fjjj0001.tfg.solidaridad_digital.repository;

import com.fjjj0001.tfg.solidaridad_digital.model.UserRating
import org.springframework.data.jpa.repository.JpaRepository

interface UserRatingRepository : JpaRepository<UserRating, Long> {
}