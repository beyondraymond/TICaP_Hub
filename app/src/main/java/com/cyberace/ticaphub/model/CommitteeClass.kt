package com.cyberace.ticaphub.model

data class CommitteeClass(
    val id: Int,
    val name: String,
    val user_id: Int,
    val created_at: String,
    val updated_at: String,
    val user: User,
    val committee_members: List<CommitteeMemberClass>,
    val tasks: List<TaskCardClass>
)