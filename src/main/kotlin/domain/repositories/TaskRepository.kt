package com.erkan.domain.repositories

import com.erkan.domain.entities.Task
import com.erkan.domain.entities.Priority
import com.erkan.domain.entities.PageRequest
import com.erkan.domain.entities.PagedResult

interface TaskRepository {
    suspend fun getAllTasks(): List<Task>
    suspend fun getTasksPaged(pageRequest: PageRequest): PagedResult<Task>
    suspend fun getTasksByPriority(priority: Priority): List<Task>
    suspend fun getTasksByPriorityPaged(priority: Priority, pageRequest: PageRequest): PagedResult<Task>
    suspend fun getTaskById(id: String): Task?
    suspend fun getTaskByName(name: String): Task?
    suspend fun addTask(task: Task): Task
    suspend fun updateTask(task: Task): Task?
    suspend fun removeTask(id: String): Boolean
    suspend fun removeTaskByName(name: String): Boolean
}
