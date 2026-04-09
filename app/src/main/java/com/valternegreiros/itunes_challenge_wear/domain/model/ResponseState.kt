package com.valternegreiros.itunes_challenge_wear.domain.model

sealed class ResponseState<out T> {
    data class Success<out T>(val data: T) : ResponseState<T>()
    object Loading : ResponseState<Nothing>()
    data class Error(val statusCode: Int? = null, val message: String) : ResponseState<Nothing>()
}


