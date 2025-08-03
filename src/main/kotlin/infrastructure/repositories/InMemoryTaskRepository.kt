package com.erkan.infrastructure.repositories

import com.erkan.domain.entities.Task
import com.erkan.domain.entities.Priority
import com.erkan.domain.entities.PageRequest
import com.erkan.domain.entities.PagedResult
import com.erkan.domain.repositories.TaskRepository
import java.util.UUID
import kotlin.math.ceil

class InMemoryTaskRepository : TaskRepository {
    private val tasks = mutableListOf(
        Task(
            id = UUID.randomUUID().toString(),
            name = "cleaning", 
            description = "Clean the house", 
            priority = Priority.Low,
            createdAt = System.currentTimeMillis() - 86400000 // 1 day ago
        ),
        Task(
            id = UUID.randomUUID().toString(),
            name = "gardening", 
            description = "Mow the lawn", 
            priority = Priority.Medium,
            createdAt = System.currentTimeMillis() - 43200000 // 12 hours ago
        ),
        Task(
            id = UUID.randomUUID().toString(),
            name = "shopping", 
            description = "Buy the groceries", 
            priority = Priority.High,
            createdAt = System.currentTimeMillis() - 21600000 // 6 hours ago
        ),
        Task(
            id = UUID.randomUUID().toString(),
            name = "painting", 
            description = "Paint the fence", 
            priority = Priority.Medium,
            createdAt = System.currentTimeMillis() - 10800000 // 3 hours ago
        )
    )

    override suspend fun getAllTasks(): List<Task> = tasks.toList()

    override suspend fun getTasksPaged(pageRequest: PageRequest): PagedResult<Task> {
        val sortedTasks = tasks.sortedByDescending { it.createdAt }
        val totalElements = sortedTasks.size.toLong()
        val totalPages = ceil(totalElements.toDouble() / pageRequest.size).toInt()
        val startIndex = pageRequest.page * pageRequest.size
        val endIndex = minOf(startIndex + pageRequest.size, sortedTasks.size)
        
        val content = if (startIndex < sortedTasks.size) {
            sortedTasks.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return PagedResult(
            content = content,
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = pageRequest.page < totalPages - 1,
            hasPrevious = pageRequest.page > 0
        )
    }

    override suspend fun getTasksByPriority(priority: Priority): List<Task> = 
        tasks.filter { it.priority == priority }

    override suspend fun getTasksByPriorityPaged(priority: Priority, pageRequest: PageRequest): PagedResult<Task> {
        val filteredTasks = tasks.filter { it.priority == priority }.sortedByDescending { it.createdAt }
        val totalElements = filteredTasks.size.toLong()
        val totalPages = ceil(totalElements.toDouble() / pageRequest.size).toInt()
        val startIndex = pageRequest.page * pageRequest.size
        val endIndex = minOf(startIndex + pageRequest.size, filteredTasks.size)
        
        val content = if (startIndex < filteredTasks.size) {
            filteredTasks.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        return PagedResult(
            content = content,
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = pageRequest.page < totalPages - 1,
            hasPrevious = pageRequest.page > 0
        )
    }

    override suspend fun getTaskById(id: String): Task? = 
        tasks.find { it.id == id }

    override suspend fun getTaskByName(name: String): Task? = 
        tasks.find { it.name.equals(name, ignoreCase = true) }

    override suspend fun addTask(task: Task): Task {
        val taskWithId = task.copy(id = task.id ?: UUID.randomUUID().toString())
        tasks.add(taskWithId)
        return taskWithId
    }

    override suspend fun updateTask(task: Task): Task? {
        val index = tasks.indexOfFirst { it.id == task.id }
        return if (index != -1) {
            tasks[index] = task
            task
        } else {
            null
        }
    }

    override suspend fun removeTask(id: String): Boolean {
        return tasks.removeIf { it.id == id }
    }

    override suspend fun removeTaskByName(name: String): Boolean {
        return tasks.removeIf { it.name == name }
    }
}
