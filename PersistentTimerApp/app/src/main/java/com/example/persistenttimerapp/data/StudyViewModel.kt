package com.example.persistenttimerapp.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.persistenttimerapp.data.entities.Category
import com.example.persistenttimerapp.data.entities.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class StudyViewModel(private val dao: AppDao) : ViewModel() {

    val allCategories: Flow<List<Category>> = dao.getAllCategories()
    val allTasks: Flow<List<Task>> = dao.getAllTasks()

    fun getTasksByCategoryAndDate(catId: Int, date: Long): Flow<List<Task>> {
        return dao.getTasksByCategoryAndDate(catId, date)
    }

    fun insertCategory(name: String, color: Int) {
        viewModelScope.launch {
            dao.insertCategory(Category(name = name, color = color))
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            dao.deleteCategory(category)
        }
    }

    fun insertTask(categoryId: Int, taskName: String, dateLong: Long) {
        viewModelScope.launch {
            dao.insertTask(Task(categoryId = categoryId, taskName = taskName, durationMinutes = 0, dateCompleted = dateLong))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            dao.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }
}

class StudyViewModelFactory(private val dao: AppDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
