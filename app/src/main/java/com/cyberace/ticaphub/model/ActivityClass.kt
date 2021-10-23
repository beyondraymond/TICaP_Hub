package com.cyberace.ticaphub.model

data class ActivityClass(
    val id: Int,
    val description: String,
    val user_id: Int,
    val task_id: Int,
    val created_at: String,
    val updated_at: String,
    val user: User,
    val files: List<FileClass>
)