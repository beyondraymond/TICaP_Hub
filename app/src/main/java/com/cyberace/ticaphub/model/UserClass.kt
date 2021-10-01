package com.cyberace.ticaphub.model

data class UserClass(
    val user_id: Int,
    val full_name: String,
    val email: String,
    val password: String
    //full_name not applicable since dissected yung name sa db
    //add ticap ID for reference mare, hindi pa siya ma-implement rn kasi wala pa laravel
)