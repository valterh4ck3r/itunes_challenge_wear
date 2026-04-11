package com.valternegreiros.itunes_challenge_wear.ui.core.util

import java.util.Base64

object Base64Utils {
    fun encode(data: String): String {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data.toByteArray())
    }

    fun decode(data: String): String {
        return String(Base64.getUrlDecoder().decode(data))
    }
}
