package com.cyberace.ticaphub

import com.cyberace.ticaphub.model.*
import retrofit2.Response
import retrofit2.http.*

interface TaskApi {

    @GET("tasks.php") //Add the uri segment to access the tasks from db
    suspend fun getTasks(): Response<List<TaskCardClass>>

    //Add a PHP to only retrieve tasks concerning the user account only
    @GET("task.php") //Add the uri segment to access the tasks from db
    suspend fun getTask(@Query(value = "taskID") taskID: Int): Response<TaskCardClass>

    @GET("boards.php") //Add the uri segment to access the tasks from db
    suspend fun getBoards(@Query(value = "eventID") eventID: Int): Response<List<TaskListClass>>

    @GET("events.php")
    suspend fun getEvents(): Response<List<EventClass>>

    @FormUrlEncoded
    @POST("user-login.php")
    suspend fun getUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<UserClass>

    //Add TICAP ID id soon here kapag may laravel na
    @FormUrlEncoded
    @POST("add-board.php")
    suspend fun addBoard(
        @Field("board_name") boardName: String,
        @Field("event_id") eventID: Int,
        @Field("user_id") userID: Int
    ): Response<ResponseClass>

    //Add user id soon here and other data structure siguro?
    //Wala pang php eto, sksksks gawa ka swiss
    @FormUrlEncoded
    @POST("add-task.php")
    suspend fun addTask(
        @Field("task_title") taskName: String,
        @Field("board_id") boardID: Int,
        @Field("user_id") userID: Int
    ): Response<ResponseClass>

    //Add TICAP ID mare galing sa user login pero later na kapag may laravel implementations na
    @FormUrlEncoded
    @POST("add-event.php")
    suspend fun addEvent(
        @Field("event_name") eventName: String
    ): Response<ResponseClass>

    @FormUrlEncoded
    //Create PHP for this later sksksksks
    @POST("move-task.php")
    suspend fun moveTask(
        @Field("task_id") taskID: Int,
        @Field("board_id") boardID: Int,
    ): Response<ResponseClass>

    @GET("get-event-id.php") //Add the uri segment to access the tasks from db
    suspend fun getEventID(@Query(value = "listID") listID: Int): Response<ResponseClass>


}