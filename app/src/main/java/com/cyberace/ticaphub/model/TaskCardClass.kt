package com.cyberace.ticaphub.model

data class TaskCardClass(
    val id: Int,
    val title: String,
    val description: String,
    val list_id: Int,
    val user_id: Int,
    val created_at: String,
    val updated_at: String,
)