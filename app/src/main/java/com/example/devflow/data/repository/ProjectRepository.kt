package com.example.devflow.data.repository

import com.example.devflow.data.local.ProjectDao
import com.example.devflow.data.model.Project
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()
    suspend fun addProject(project: Project) = projectDao.insertProject(project)
    suspend fun updateProject(project: Project) = projectDao.updateProject(project)
    suspend fun deleteProject(project: Project) = projectDao.deleteProject(project)
}