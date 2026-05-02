package com.example.persistenttimerapp.data

import androidx.room.*
import com.example.persistenttimerapp.data.entities.Category
import com.example.persistenttimerapp.data.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT * FROM tasks WHERE categoryId = :catId")
    fun getTasksByCategory(catId: Int): Flow<List<Task>>

    @Insert
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}
