package com.erkan.presentation.routes

import com.erkan.*
import com.erkan.di.TaskModule
import com.erkan.presentation.controllers.TaskController
import com.erkan.presentation.controllers.PhotoController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection

fun Application.configureRouting() {
    val dbConnection: Connection = connectToPostgres(embedded = true)
    val cityService = CityService(dbConnection)
    val taskController = TaskModule.taskController
    val photoController = TaskModule.photoController
    
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        // Task routes using Clean Architecture
        route("/tasks") {
            // Get all tasks (non-paged)
            get {
                taskController.getAllTasks(call)
            }
            
            // Get tasks with paging
            get("/paged") {
                taskController.getTasksPaged(call)
            }
            
            // Create new task
            post {
                taskController.addTask(call)
            }
            
            // Get task by ID
            get("/{id}") {
                taskController.getTaskById(call)
            }
            
            // Update task by ID
            put("/{id}") {
                taskController.updateTask(call)
            }
            
            // Delete task by ID
            delete("/{id}") {
                taskController.removeTask(call)
            }
            
            // Get task by name
            get("/byName/{taskName}") {
                taskController.getTaskByName(call)
            }
            
            // Delete task by name
            delete("/byName/{taskName}") {
                taskController.removeTaskByName(call)
            }
            
            // Get tasks by priority (non-paged)
            get("/byPriority/{priority}") {
                taskController.getTasksByPriority(call)
            }
            
            // Get tasks by priority with paging
            get("/byPriority/{priority}/paged") {
                taskController.getTasksByPriorityPaged(call)
            }
        }

        // Photo routes
        route("/photos") {
            // Get all photos
            get {
                photoController.getAllPhotos(call)
            }
            
            // Upload photo (optionally associate with task)
            post {
                photoController.uploadPhoto(call)
            }
            
            // Get photo metadata
            get("/{id}") {
                photoController.getPhoto(call)
            }
            
            // Download photo file
            get("/{id}/download") {
                photoController.downloadPhoto(call)
            }
            
            // Delete photo
            delete("/{id}") {
                photoController.deletePhoto(call)
            }
            
            // Delete photo from specific task
            delete("/{id}/task/{taskId}") {
                photoController.deletePhoto(call)
            }
            
            // Associate existing photo with task
            post("/{photoId}/associate/{taskId}") {
                photoController.associatePhotoWithTask(call)
            }
        }
        
        // Task-specific photo routes
        route("/tasks/{taskId}/photos") {
            // Get all photos for a specific task
            get {
                photoController.getPhotosByTask(call)
            }
        }

        // Use nested routing to avoid conflicts
        route("/cities") {
            cities(cityService)
        }
    }
}

private fun Route.cities(cityService: CityService) {
    // Get all cities
    get {
        try {
            val cities = cityService.getAll()
            call.respond(HttpStatusCode.OK, cities)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
        }
    }

    // Create city
    post {
        val city = call.receive<City>()
        val id = cityService.create(city)
        call.respond(HttpStatusCode.Created, id)
    }

    // Get specific city by ID
    get("/{id}") {
        val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
        try {
            val city = cityService.read(id)
            call.respond(HttpStatusCode.OK, city)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    // Update city
    put("/{id}") {
        val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
        val city = call.receive<City>()
        cityService.update(id, city)
        call.respond(HttpStatusCode.OK)
    }

    // Delete city
    delete("/{id}") {
        val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
        cityService.delete(id)
        call.respond(HttpStatusCode.OK)
    }
}