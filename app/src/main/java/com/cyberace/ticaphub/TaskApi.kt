package com.cyberace.ticaphub

import com.cyberace.ticaphub.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface TaskApi {

    //APIs CREATED IN MY LOCAL DB, MIGHT DELETE LATER

    //Add a PHP to only retrieve tasks concerning the user account only
//    @GET("tasks.php") //Add the uri segment to access the tasks from db
//    suspend fun getTasks(): Response<List<TaskCardClass>>

//    @GET("task.php") //Add the uri segment to access the tasks from db
//    suspend fun getTask(@Query(value = "taskID") taskID: Int): Response<TaskCardClass>

//    @GET("boards.php") //Add the uri segment to access the tasks from db
//    suspend fun getBoards(@Query(value = "eventID") eventID: Int): Response<List<TaskListClass>>

//    @GET("events.php")
//    suspend fun getEvents(): Response<List<EventClass>>

//    @FormUrlEncoded
//    @POST("user-login.php")
//    suspend fun getUser(
//        @Field("email") email: String,
//        @Field("password") password: String
//    ): Response<UserClass>

//    //Add TICAP ID id soon here kapag may laravel na
//    @FormUrlEncoded
//    @POST("add-board.php")
//    suspend fun addBoard(
//        @Field("board_name") boardName: String,
//        @Field("event_id") eventID: Int,
//        @Field("user_id") userID: Int
//    ): Response<ResponseClass>

//    @FormUrlEncoded
//    @POST("add-task.php")
//    suspend fun addTask(
//        @Field("task_title") taskName: String,
//        @Field("board_id") boardID: Int,
//        @Field("user_id") userID: Int
//    ): Response<ResponseClass>

//    //Add TICAP ID mare galing sa user login pero later na kapag may laravel implementations na
//    @FormUrlEncoded
//    @POST("add-event.php")
//    suspend fun addEvent(
//        @Field("event_name") eventName: String
//    ): Response<ResponseClass>

//    @FormUrlEncoded
//    @POST("move-task.php")
//    suspend fun moveTask(
//        @Field("task_id") taskID: Int,
//        @Field("board_id") boardID: Int,
//    ): Response<ResponseClass>

    //Hindi ko na siya isasama sa updated api, kinuha ko na lang yung eventID from repeated intent pass; update: nvm HAHAHAHA
    //Sa response class ko na lang siya nilagay since 1 value lang naman need ko kunin
//    @GET("get-event-id.php") //Add the uri segment to access the tasks from db
//    suspend fun getEventID(@Query(value = "listID") listID: Int): Response<ResponseClass>

//    @GET("activity.php") //Add the uri segment to access the tasks from db
//    suspend fun getActivities(@Query(value = "taskID") taskID: Int): Response<List<ActivityClass>>

    //What I did is that "if comments doesn't file attached -> use addActivity" else -> use addActivityWithFile
//    @FormUrlEncoded
//    @POST("add-activity.php")
//    suspend fun addActivity(
//        @Field("description") description: String,
//        @Field("user_id") userID: Int,
//        @Field("task_id") taskID: Int
//    ): Response<ResponseClass>

    @Multipart
    @POST("add-activity.php")
    suspend fun uploadImage(
        @Part fileUploaded: MultipartBody.Part,
        @Part("event_id") eventID: RequestBody

    ): Response<ResponseClass>

    ///////////////////////////////////////////
    //NEW APIs USING PATH AND FROM THE SERVER//
    ///////////////////////////////////////////

    /////////////AUTHENTICATION API////////////////
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

    @FormUrlEncoded
    @PUT("events/0/lists/0/tasks/{taskID}")
    suspend fun moveTask(
        @Header("Authorization") authHeader: String,
        @Path("taskID") taskID: Int,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("members[]") members: List<Int>,
        @Field("list") boardID: Int
    ): Response<ResponseClass>

    //UPDATE, MERON NA PALA ACTIVITIES UNDER TASK JSON SO BAKA OBSOLETE NA TO
//    @GET("activity.php") //Add the uri segment to access the tasks from db
//    suspend fun getActivities(@Query(value = "taskID") taskID: Int): Response<List<ActivityClass>>

    //TODO ASAP: ONLY MEMBERS CAN COMMENT DAPAT
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

}