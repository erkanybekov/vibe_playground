package com.erkan.domain.usecases

import com.erkan.domain.entities.Photo
import com.erkan.domain.repositories.PhotoRepository
import java.util.UUID

class UploadPhotoUseCase(private val photoRepository: PhotoRepository) {
    suspend fun execute(
        originalName: String,
        contentType: String,
        fileData: ByteArray
    ): Photo {
        val photo = Photo(
            id = UUID.randomUUID().toString(),
            filename = "${UUID.randomUUID()}_$originalName",
            originalName = originalName,
            contentType = contentType,
            size = fileData.size.toLong(),
            uploadedAt = System.currentTimeMillis()
        )
        
        photoRepository.savePhoto(photo, fileData)
        return photo
    }
}

class GetPhotoUseCase(private val photoRepository: PhotoRepository) {
    suspend fun execute(id: String): Photo? {
        return photoRepository.getPhoto(id)
    }
}

class GetPhotoDataUseCase(private val photoRepository: PhotoRepository) {
    suspend fun execute(id: String): ByteArray? {
        return photoRepository.getPhotoData(id)
    }
}

class DeletePhotoUseCase(private val photoRepository: PhotoRepository) {
    suspend fun execute(id: String): Boolean {
        return photoRepository.deletePhoto(id)
    }
}

class GetPhotosByTaskIdUseCase(private val photoRepository: PhotoRepository) {
    suspend fun execute(taskId: String): List<Photo> {
        return photoRepository.getPhotosByTaskId(taskId)
    }
}

class GetAllPhotosUseCase(private val photoRepository: PhotoRepository) {
    suspend fun execute(): List<Photo> {
        return photoRepository.getAllPhotos()
    }
}
