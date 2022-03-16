package com.cyberace.ticaphub.model

data class Ticap(
    val id: Int,
    val name: String,
    val invitation_is_set: Int,
    val election_has_started: Int,
    val election_review: Int,
    val has_new_election: Int,
    val election_finished: Int,
    val awards_is_set: Int,
    val evaluation_finished: Int,
    val is_done: Int,
    val created_at: String,
    val updated_at: String,
    val finalize_award: Int
)