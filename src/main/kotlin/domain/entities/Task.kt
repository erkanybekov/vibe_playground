package com.erkan.domain.entities

import kotlinx.serialization.Serializable

enum class Priority {
    Low, Medium, High, Vital
}

@Serializable
data class Photo(
    val id: String,
    val filename: String,
    val originalName: String,
    val contentType: String,
    val size: Long,
    val uploadedAt: Long
)

@Serializable
data class Task(
    val id: String? = null,
    val name: String,
    val description: String,
    val priority: Priority,
    val photos: List<Photo> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class PageRequest(
    val page: Int = 0,
    val size: Int = 10
)

@Serializable
data class PagedResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
