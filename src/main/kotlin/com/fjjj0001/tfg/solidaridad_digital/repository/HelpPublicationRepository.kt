package com.fjjj0001.tfg.solidaridad_digital.repository;

import com.fjjj0001.tfg.solidaridad_digital.model.HelpPublication
import com.fjjj0001.tfg.solidaridad_digital.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

interface HelpPublicationRepository : JpaRepository<HelpPublication, Long> {
    @Query("SELECT h FROM HelpPublication h JOIN h.tags t WHERE t IN :tags")
    fun findByTags(@Param("tags") tags: List<String>): List<HelpPublication>

    @Query("SELECT h FROM HelpPublication h WHERE DATE(h.publicationDate) BETWEEN :startDate AND :endDate")
    fun findByPublicationDate(@Param("startDate") startDate: LocalDate, @Param("endDate") endDate: LocalDate): List<HelpPublication>


    fun findByUser(user: User): List<HelpPublication>

    @Query("SELECT h FROM HelpPublication h WHERE (:userUsername IS NULL OR h.user.username = :userUsername) AND (:title IS NULL OR h.title LIKE %:title%) AND (:tags IS NULL OR h.id IN (SELECT h2.id FROM HelpPublication h2 JOIN h2.tags t WHERE t IN :tags)) AND (:initialDate IS NULL OR h.publicationDate >= :initialDate) AND (:finalDate IS NULL OR h.publicationDate <= :finalDate)")
    fun findBySearchFilter(
        @Param("userUsername") userUsername: String?,
        @Param("title") title: String?,
        @Param("tags") tags: List<String>?,
        @Param("initialDate") initialDate: LocalDateTime?,
        @Param("finalDate") finalDate: LocalDateTime?
    ): List<HelpPublication>



}