package com.cyberace.ticaphub.model

data class TaskListClass(
    val id: Int,
    val title: String,
    val user_id: Int,
    val event_id: Int,
    val created_at: String,
    val updated_at: String,
    val tasks: List<TaskCardClass>
)
