package com.cyberace.ticaphub

import com.cyberace.ticaphub.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface TaskApi {

    ///////////////////////////////////////////
    //NEW APIs USING PATH AND FROM THE SERVER//
    ///////////////////////////////////////////

    /////////////AUTHENTICATION/USER DETAILS API////////////////
    @FormUrlEncoded
    @POST("login")
    suspend fun getUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<UserClass>

    @Headers("Content-Type: application/json")
    @POST("logout")
    suspend fun logoutUser(
        @Header("Authorization") authHeader: String
    ): Response<ResponseClass>

    @Multipart
    @POST("user/{userID}")
    suspend fun updateUser(
        @Header("Authorization") authHeader: String,
        @Path("userID") taskID: Int,
        @Part("first_name") first_name: RequestBody,
        @Part("middle_name") middle_name: RequestBody,
        @Part("last_name") last_name: RequestBody,
        @Part profile_picture: MultipartBody.Part?

    ): Response<ResponseClass>

    /////////////EVENTS API////////////////
    @GET("events")
    suspend fun getEvents(
        @Header("Authorization") authHeader: String
    ): Response<List<EventClass>>

    @FormUrlEncoded
    @POST("events")
    suspend fun addEvent(
        @Header("Authorization") authHeader: String,
        @Field("name") eventName: String
    ): Response<ResponseClass>

    @FormUrlEncoded
    @PUT("events/{eventID}")
    suspend fun updateEvent(
        @Header("Authorization") authHeader: String,
        @Path("eventID") eventID: Int,
        @Field("name") eventName: String
    ): Response<ResponseClass>

    @DELETE("events/{eventID}")
    suspend fun deleteEvent(
        @Header("Authorization") authHeader: String,
        @Path("eventID") eventID: Int
    ): Response<ResponseClass>

    @GET("home/{taskID}") //Add the uri segment to access the tasks from db
    suspend fun getEventID(
        @Header("Authorization") authHeader: String,
        @Path("taskID") taskID: Int
    ): Response<ResponseClass>

    /////////////BOARDS/TASK LIST API////////////////

    //getBoards fetches the task list from the specified event ID
    @GET("events/{eventID}")
    suspend fun getBoards(
        @Header("Authorization") authHeader: String,
        @Path("eventID") eventID: Int
    //Response Class has been edited to list (Original Response<TaskListClass> -> Response<EventClass>)
    //kase array yung binabato ni peter kahit isang value lang naman need ko
    //And binabato niya yung buong event imbes na tasklist lang, such a waste of memory
    ): Response<EventClass>

    @FormUrlEncoded
    @POST("events/{eventID}")
    suspend fun addBoard(
        @Header("Authorization") authHeader: String,
        @Path("eventID") eventID: Int,
        @Field("title") boardName: String
    ): Response<ResponseClass>

    @FormUrlEncoded
    @PUT("events/0/lists/{listID}")
    suspend fun updateList(
        @Header("Authorization") authHeader: String,
        @Path("listID") listID: Int,
        @Field("title") listName: String
    ): Response<ResponseClass>

    @DELETE("events/0/lists/{listID}")
    suspend fun deleteList(
        @Header("Authorization") authHeader: String,
        @Path("listID") listID: Int
    ): Response<ResponseClass>

    /////////////TASKS API////////////////
    @GET("events/0/lists/0/tasks/{taskID}")
    suspend fun getTask(
        @Header("Authorization") authHeader: String,
        @Path("taskID") taskID: Int
    ): Response<TaskCardClass>

    @GET("committees/0/tasks/{taskID}")
    suspend fun getCommitteeTask(
        @Header("Authorization") authHeader: String,
        @Path("taskID") taskID: Int
    ): Response<TaskCardClass>

    @GET("home") //Add the uri segment to access the tasks from db
    suspend fun getAssignedTasks(
        @Header("Authorization") authHeader: String
    ): Response<User>

    @FormUrlEncoded
    @POST("events/{eventID}/lists/{listID}")
    suspend fun addTask(
        @Header("Authorization") authHeader: String,
        @Path("eventID") eventID: Int,
        @Path("listID") listID: Int,
        @Field("title") title: String,
        @Field("description") description: String,
//        @Field("members[0]") members: Int,
    ): Response<ResponseClass>

    @GET("officers") //Add the uri segment to access the tasks from db
    suspend fun getOfficers(
        @Header("Authorization") authHeader: String
    ): Response<List<User>>

    @FormUrlEncoded
    @PUT("events/0/lists/0/tasks/{taskID}")
    suspend fun updateTask(
        @Header("Authorization") authHeader: String,
        @Path("taskID") taskID: Int,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("members[]") members: List<Int>,
        @Field("list") boardID: Int
    ): Response<ResponseClass>

    @DELETE("events/0/lists/0/tasks/{taskID}")
    suspend fun deleteTask(
        @Header("Authorization") authHeader: String,
        @Path("taskID") listID: Int
    ): Response<ResponseClass>

    @Multipart
    @POST("events/{eventID}/lists/{listID}/tasks/{taskID}")
    suspend fun addActivity(
        @Header("Authorization") authHeader: String,
        @Path("eventID") eventID: Int,
        @Path("listID") listID: Int,
        @Path("taskID") taskID: Int,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part?
    ): Response<ActivityClass>

    @GET("committees/{committeeID}")
    suspend fun getCommittee(
        @Header("Authorization") authHeader: String,
        @Path("committeeID") committeeID: Int
    ): Response<List<CommitteeClass>>
}