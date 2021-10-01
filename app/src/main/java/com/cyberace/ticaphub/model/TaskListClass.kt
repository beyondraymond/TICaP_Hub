package com.cyberace.ticaphub.model

data class TaskListClass(
    val id: Int,
    val title: String,
    val tasks: List<TaskCardClass>
)
