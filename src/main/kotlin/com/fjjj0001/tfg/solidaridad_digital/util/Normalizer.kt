package com.fjjj0001.tfg.solidaridad_digital.util

import java.text.Normalizer

fun String.normalize(): String {
    return Normalizer.normalize(this.lowercase().replace(" ", ""), Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}
