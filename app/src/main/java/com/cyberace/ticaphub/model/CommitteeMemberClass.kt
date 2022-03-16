package com.cyberace.ticaphub.model

data class CommitteeMemberClass(
    val id: Int,
    val committee_id: Int,
    val user_id: Int,
    val created_at: String,
    val updated_at: String,
    val user: User
)