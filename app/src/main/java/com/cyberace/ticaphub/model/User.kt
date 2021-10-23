package com.cyberace.ticaphub.model

data class User(
    val id: Int,
    val first_name: String,
    val middle_name: String,
    val last_name: String,
    val email: String,
    val email_verified: Int,
    val created_at: String,
    val updated_at: String,
    val ticap_id: Int,
    val tasks: List<TaskCardClass>,
    val roles: List<RolesClass>,
    val message: String
)