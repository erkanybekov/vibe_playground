package com.erkan.infrastructure.repositories

import com.erkan.domain.entities.Photo
import com.erkan.domain.repositories.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class PhotoRepositoryImpl(
    private val uploadDirectory: String = "uploads/photos"
) : PhotoRepository {
    
    private val photos = mutableMapOf<String, Photo>()
    private val taskPhotos = mutableMapOf<String, MutableList<String>>()
    
    init {
        // Create upload directory if it doesn't exist
        File(uploadDirectory).mkdirs()
    }
    
    override suspend fun savePhoto(photo: Photo, fileData: ByteArray): String = withContext(Dispatchers.IO) {
        val filePath = Paths.get(uploadDirectory, photo.filename)
        Files.write(filePath, fileData, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        photos[photo.id] = photo
        photo.id
    }
    
    override suspend fun getPhoto(id: String): Photo? {
        return photos[id]
    }
    
    override suspend fun getPhotoData(id: String): ByteArray? = withContext(Dispatchers.IO) {
        val photo = photos[id] ?: return@withContext null
        val filePath = Paths.get(uploadDirectory, photo.filename)
        if (Files.exists(filePath)) {
            Files.readAllBytes(filePath)
        } else {
            null
        }
    }
    
    override suspend fun deletePhoto(id: String): Boolean = withContext(Dispatchers.IO) {
        val photo = photos[id] ?: return@withContext false
        val filePath = Paths.get(uploadDirectory, photo.filename)
        
        val fileDeleted = if (Files.exists(filePath)) {
            Files.delete(filePath)
            true
        } else {
            true // File doesn't exist, consider it deleted
        }
        
        if (fileDeleted) {
            photos.remove(id)
            // Remove from task associations
            taskPhotos.values.forEach { it.remove(id) }
        }
        
        fileDeleted
    }
    
    override suspend fun getPhotosByTaskId(taskId: String): List<Photo> {
        val photoIds = taskPhotos[taskId] ?: return emptyList()
        return photoIds.mapNotNull { photos[it] }
    }
    
    override suspend fun getAllPhotos(): List<Photo> {
        return photos.values.toList()
    }
    
    override suspend fun associatePhotoWithTask(photoId: String, taskId: String) {
        withContext(Dispatchers.IO) {
            taskPhotos.getOrPut(taskId) { mutableListOf() }.add(photoId)
        }
    }
    
    override suspend fun removePhotoFromTask(photoId: String, taskId: String) {
        withContext(Dispatchers.IO) {
            taskPhotos[taskId]?.remove(photoId)
        }
    }
}
