package com.erkan.domain.repositories

import com.erkan.domain.entities.Photo

interface PhotoRepository {
    suspend fun savePhoto(photo: Photo, fileData: ByteArray): String
    suspend fun getPhoto(id: String): Photo?
    suspend fun getPhotoData(id: String): ByteArray?
    suspend fun deletePhoto(id: String): Boolean
    suspend fun getPhotosByTaskId(taskId: String): List<Photo>
    suspend fun getAllPhotos(): List<Photo>
}
