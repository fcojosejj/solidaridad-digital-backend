package com.fjjj0001.tfg.solidaridad_digital.model.dto

import java.io.Serializable

data class HelpPublicationSearchFilterDto(
    val userUsername: String? = null,
    var title: String? = null,
    val tags: MutableList<String>? = null,
    val initialDate: String? = null,
    val finalDate: String? = null,
) : Serializable