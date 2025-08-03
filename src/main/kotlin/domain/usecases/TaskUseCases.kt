package com.erkan.domain.usecases

import com.erkan.domain.entities.Task
import com.erkan.domain.entities.Priority
import com.erkan.domain.entities.PageRequest
import com.erkan.domain.entities.PagedResult
import com.erkan.domain.entities.Photo
import com.erkan.domain.repositories.TaskRepository
import java.util.UUID

class GetAllTasksUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(): List<Task> = taskRepository.getAllTasks()
}

class GetTasksPagedUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(pageRequest: PageRequest): PagedResult<Task> = 
        taskRepository.getTasksPaged(pageRequest)
}

class GetTasksByPriorityUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(priority: Priority): List<Task> = 
        taskRepository.getTasksByPriority(priority)
}

class GetTasksByPriorityPagedUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(priority: Priority, pageRequest: PageRequest): PagedResult<Task> = 
        taskRepository.getTasksByPriorityPaged(priority, pageRequest)
}

class GetTaskByIdUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(id: String): Task? = taskRepository.getTaskById(id)
}

class GetTaskByNameUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(name: String): Task? = taskRepository.getTaskByName(name)
}

class AddTaskUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(task: Task): Task {
        if (taskRepository.getTaskByName(task.name) != null) {
            throw IllegalStateException("Cannot duplicate task names!")
        }
        val taskWithId = task.copy(id = task.id ?: UUID.randomUUID().toString())
        return taskRepository.addTask(taskWithId)
    }
}

class UpdateTaskUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(task: Task): Task? {
        return taskRepository.updateTask(task)
    }
}

class AddPhotoToTaskUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(taskId: String, photo: Photo): Task? {
        val task = taskRepository.getTaskById(taskId) ?: return null
        val updatedTask = task.copy(photos = task.photos + photo)
        return taskRepository.updateTask(updatedTask)
    }
}

class RemovePhotoFromTaskUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(taskId: String, photoId: String): Task? {
        val task = taskRepository.getTaskById(taskId) ?: return null
        val updatedTask = task.copy(photos = task.photos.filter { it.id != photoId })
        return taskRepository.updateTask(updatedTask)
    }
}

class RemoveTaskUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(id: String): Boolean = taskRepository.removeTask(id)
}

class RemoveTaskByNameUseCase(private val taskRepository: TaskRepository) {
    suspend fun execute(name: String): Boolean = taskRepository.removeTaskByName(name)
}
