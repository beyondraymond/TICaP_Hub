package com.cyberace.ticaphub.model

data class EventClass(
    val id: Int,
    val name: String,
    val created_at: String,
    val updated_at: String,
    val ticap_id: Int,
    val lists: List<TaskListClass>
)