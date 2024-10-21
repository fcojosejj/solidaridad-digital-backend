package com.fjjj0001.tfg.solidaridad_digital.repository;

import com.fjjj0001.tfg.solidaridad_digital.model.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long> {
}