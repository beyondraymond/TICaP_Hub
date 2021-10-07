package com.cyberace.ticaphub.model

data class ActivityClass(
    val id: Int,
    val description: String,
    val user_id: Int,
    val created_at: String,
    val `file`: String
)