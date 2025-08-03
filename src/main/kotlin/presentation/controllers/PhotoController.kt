package com.erkan.presentation.controllers

import com.erkan.domain.usecases.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.ktor.utils.io.*
import java.io.File

class PhotoController(
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val getPhotoUseCase: GetPhotoUseCase,
    private val getPhotoDataUseCase: GetPhotoDataUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
    private val getAllPhotosUseCase: GetAllPhotosUseCase,
    private val getPhotosByTaskIdUseCase: GetPhotosByTaskIdUseCase,
    private val addPhotoToTaskUseCase: AddPhotoToTaskUseCase,
    private val removePhotoFromTaskUseCase: RemovePhotoFromTaskUseCase
) {
    
    suspend fun uploadPhoto(call: ApplicationCall) {
        try {
            val multipartData = call.receiveMultipart()
            var taskId: String? = null
            var uploadedPhoto: com.erkan.domain.entities.Photo? = null
            
            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "taskId") {
                            taskId = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        val originalFileName = part.originalFileName ?: "unknown"
                        val contentType = part.contentType?.toString() ?: "application/octet-stream"
                        
                        val channel = part.provider()
                        val fileBytes = ByteArray(channel.availableForRead)
                        channel.readFully(fileBytes)
                        
                        uploadedPhoto = uploadPhotoUseCase.execute(
                            originalName = originalFileName,
                            contentType = contentType,
                            fileData = fileBytes
                        )
                    }
                    else -> {}
                }
                part.dispose()
            }
            
            if (uploadedPhoto == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No file uploaded"))
                return
            }
            
            // If taskId is provided, associate photo with task
            taskId?.let { id ->
                val updatedTask = addPhotoToTaskUseCase.execute(id, uploadedPhoto!!)
                if (updatedTask == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Task not found"))
                    return
                }
            }
            
            call.respond(HttpStatusCode.Created, uploadedPhoto!!)
            
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
    
    suspend fun getPhoto(call: ApplicationCall) {
        try {
            val photoId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Photo ID is required"))
                return
            }
            
            val photo = getPhotoUseCase.execute(photoId)
            if (photo == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Photo not found"))
                return
            }
            
            call.respond(HttpStatusCode.OK, photo)
            
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
    
    suspend fun downloadPhoto(call: ApplicationCall) {
        try {
            val photoId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Photo ID is required"))
                return
            }
            
            val photo = getPhotoUseCase.execute(photoId)
            if (photo == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Photo not found"))
                return
            }
            
            val photoData = getPhotoDataUseCase.execute(photoId)
            if (photoData == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Photo data not found"))
                return
            }
            
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName,
                    photo.originalName
                ).toString()
            )
            call.respondBytes(photoData, ContentType.parse(photo.contentType))
            
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
    
    suspend fun deletePhoto(call: ApplicationCall) {
        try {
            val photoId = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Photo ID is required"))
                return
            }
            
            val taskId = call.parameters["taskId"]
            
            // Remove photo from task if taskId is provided
            taskId?.let { id ->
                removePhotoFromTaskUseCase.execute(id, photoId)
            }
            
            val deleted = deletePhotoUseCase.execute(photoId)
            if (deleted) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Photo deleted successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Photo not found"))
            }
            
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
    
    suspend fun getAllPhotos(call: ApplicationCall) {
        try {
            val photos = getAllPhotosUseCase.execute()
            call.respond(HttpStatusCode.OK, photos)
            
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
    
    suspend fun getPhotosByTask(call: ApplicationCall) {
        try {
            val taskId = call.parameters["taskId"] ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Task ID is required"))
                return
            }
            
            val photos = getPhotosByTaskIdUseCase.execute(taskId)
            call.respond(HttpStatusCode.OK, photos)
            
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
    
    suspend fun associatePhotoWithTask(call: ApplicationCall) {
        try {
            val photoId = call.parameters["photoId"] ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Photo ID is required"))
                return
            }
            
            val taskId = call.parameters["taskId"] ?: run {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Task ID is required"))
                return
            }
            
            // First, check if the photo exists
            val photo = getPhotoUseCase.execute(photoId)
            if (photo == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Photo not found"))
                return
            }
            
            // Associate the photo with the task
            val updatedTask = addPhotoToTaskUseCase.execute(taskId, photo)
            if (updatedTask == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Task not found"))
                return
            }
            
            call.respond(HttpStatusCode.OK, mapOf(
                "message" to "Photo associated with task successfully",
                "task" to updatedTask
            ))
            
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }
}
