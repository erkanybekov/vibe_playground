package com.erkan.presentation.controllers

import com.erkan.domain.usecases.*
import com.erkan.domain.entities.Task
import com.erkan.domain.entities.Priority
import com.erkan.domain.entities.PageRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException

class TaskController(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val getTasksPagedUseCase: GetTasksPagedUseCase,
    private val getTasksByPriorityUseCase: GetTasksByPriorityUseCase,
    private val getTasksByPriorityPagedUseCase: GetTasksByPriorityPagedUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val getTaskByNameUseCase: GetTaskByNameUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val removeTaskUseCase: RemoveTaskUseCase,
    private val removeTaskByNameUseCase: RemoveTaskByNameUseCase
) {
    suspend fun getAllTasks(call: ApplicationCall) {
        val tasks = getAllTasksUseCase.execute()
        call.respond(tasks)
    }

    suspend fun getTasksPaged(call: ApplicationCall) {
        try {
            val page = call.parameters["page"]?.toIntOrNull() ?: 0
            val size = call.parameters["size"]?.toIntOrNull() ?: 10
            
            val pageRequest = PageRequest(page = page, size = size)
            val pagedResult = getTasksPagedUseCase.execute(pageRequest)
            
            call.respond(pagedResult)
        } catch (ex: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to ex.message))
        }
    }

    suspend fun addTask(call: ApplicationCall) {
        try {
            val task = call.receive<Task>()
            val createdTask = addTaskUseCase.execute(task)
            call.respond(HttpStatusCode.Created, createdTask)
        } catch (ex: IllegalStateException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to ex.message))
        } catch (ex: SerializationException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid task format"))
        }
    }

    suspend fun updateTask(call: ApplicationCall) {
        try {
            val taskId = call.parameters["id"]
            if (taskId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Task ID is required"))
                return
            }

            val task = call.receive<Task>()
            val updatedTask = updateTaskUseCase.execute(task.copy(id = taskId))
            
            if (updatedTask == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Task not found"))
                return
            }
            
            call.respond(updatedTask)
        } catch (ex: SerializationException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid task format"))
        }
    }

    suspend fun getTaskById(call: ApplicationCall) {
        val id = call.parameters["id"]
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Task ID is required"))
            return
        }

        val task = getTaskByIdUseCase.execute(id)
        if (task == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Task not found"))
            return
        }
        call.respond(task)
    }

    suspend fun getTaskByName(call: ApplicationCall) {
        val name = call.parameters["taskName"]
        if (name == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Task name is required"))
            return
        }

        val task = getTaskByNameUseCase.execute(name)
        if (task == null) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Task not found"))
            return
        }
        call.respond(task)
    }

    suspend fun removeTask(call: ApplicationCall) {
        val id = call.parameters["id"]
        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Task ID is required"))
            return
        }

        if (removeTaskUseCase.execute(id)) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Task not found"))
        }
    }

    suspend fun removeTaskByName(call: ApplicationCall) {
        val name = call.parameters["taskName"]
        if (name == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Task name is required"))
            return
        }

        if (removeTaskByNameUseCase.execute(name)) {
            call.respond(HttpStatusCode.NoContent)
        } else {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Task not found"))
        }
    }

    suspend fun getTasksByPriority(call: ApplicationCall) {
        val priorityAsText = call.parameters["priority"]
        if (priorityAsText == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Priority is required"))
            return
        }
        
        try {
            val priority = Priority.valueOf(priorityAsText)
            val tasks = getTasksByPriorityUseCase.execute(priority)

            if (tasks.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "No tasks found for priority $priorityAsText"))
                return
            }
            call.respond(tasks)
        } catch (ex: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid priority value"))
        }
    }

    suspend fun getTasksByPriorityPaged(call: ApplicationCall) {
        val priorityAsText = call.parameters["priority"]
        if (priorityAsText == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Priority is required"))
            return
        }
        
        try {
            val priority = Priority.valueOf(priorityAsText)
            val page = call.parameters["page"]?.toIntOrNull() ?: 0
            val size = call.parameters["size"]?.toIntOrNull() ?: 10
            
            val pageRequest = PageRequest(page = page, size = size)
            val pagedResult = getTasksByPriorityPagedUseCase.execute(priority, pageRequest)
            
            call.respond(pagedResult)
        } catch (ex: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid priority value"))
        } catch (ex: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to ex.message))
        }
    }
}
