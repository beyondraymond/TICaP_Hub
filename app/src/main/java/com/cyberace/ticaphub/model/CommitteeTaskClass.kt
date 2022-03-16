package com.cyberace.ticaphub.model

data class CommitteeTaskClass(
    val id: Int,
    val title: String,
    val description: String,
    val committee_id: Int,
    val status: String,
    val created_at: String,
    val updated_at: String,
    val pivot: PivotX
    //TODO MIGHT DELETE KAPAG GUMANA YUNG TASKCARDCLASS
)