package com.erkan.di

import com.erkan.domain.usecases.*
import com.erkan.domain.repositories.TaskRepository
import com.erkan.domain.repositories.PhotoRepository
import com.erkan.infrastructure.repositories.InMemoryTaskRepository
import com.erkan.infrastructure.repositories.PhotoRepositoryImpl
import com.erkan.presentation.controllers.TaskController
import com.erkan.presentation.controllers.PhotoController

object TaskModule {
    // Repositories
    private val taskRepository: TaskRepository = InMemoryTaskRepository()
    private val photoRepository: PhotoRepository = PhotoRepositoryImpl()
    
    // Task Use Cases
    val getAllTasksUseCase = GetAllTasksUseCase(taskRepository)
    val getTasksPagedUseCase = GetTasksPagedUseCase(taskRepository)
    val getTasksByPriorityUseCase = GetTasksByPriorityUseCase(taskRepository)
    val getTasksByPriorityPagedUseCase = GetTasksByPriorityPagedUseCase(taskRepository)
    val getTaskByIdUseCase = GetTaskByIdUseCase(taskRepository)
    val getTaskByNameUseCase = GetTaskByNameUseCase(taskRepository)
    val addTaskUseCase = AddTaskUseCase(taskRepository)
    val updateTaskUseCase = UpdateTaskUseCase(taskRepository)
    val addPhotoToTaskUseCase = AddPhotoToTaskUseCase(taskRepository)
    val removePhotoFromTaskUseCase = RemovePhotoFromTaskUseCase(taskRepository)
    val removeTaskUseCase = RemoveTaskUseCase(taskRepository)
    val removeTaskByNameUseCase = RemoveTaskByNameUseCase(taskRepository)
    
    // Photo Use Cases
    val uploadPhotoUseCase = UploadPhotoUseCase(photoRepository)
    val getPhotoUseCase = GetPhotoUseCase(photoRepository)
    val getPhotoDataUseCase = GetPhotoDataUseCase(photoRepository)
    val deletePhotoUseCase = DeletePhotoUseCase(photoRepository)
    val getAllPhotosUseCase = GetAllPhotosUseCase(photoRepository)
    val getPhotosByTaskIdUseCase = GetPhotosByTaskIdUseCase(photoRepository)
    
    // Controllers
    val taskController = TaskController(
        getAllTasksUseCase = getAllTasksUseCase,
        getTasksPagedUseCase = getTasksPagedUseCase,
        getTasksByPriorityUseCase = getTasksByPriorityUseCase,
        getTasksByPriorityPagedUseCase = getTasksByPriorityPagedUseCase,
        getTaskByIdUseCase = getTaskByIdUseCase,
        getTaskByNameUseCase = getTaskByNameUseCase,
        addTaskUseCase = addTaskUseCase,
        updateTaskUseCase = updateTaskUseCase,
        removeTaskUseCase = removeTaskUseCase,
        removeTaskByNameUseCase = removeTaskByNameUseCase
    )
    
    val photoController = PhotoController(
        uploadPhotoUseCase = uploadPhotoUseCase,
        getPhotoUseCase = getPhotoUseCase,
        getPhotoDataUseCase = getPhotoDataUseCase,
        deletePhotoUseCase = deletePhotoUseCase,
        getAllPhotosUseCase = getAllPhotosUseCase,
        getPhotosByTaskIdUseCase = getPhotosByTaskIdUseCase,
        addPhotoToTaskUseCase = addPhotoToTaskUseCase,
        removePhotoFromTaskUseCase = removePhotoFromTaskUseCase
    )
}
