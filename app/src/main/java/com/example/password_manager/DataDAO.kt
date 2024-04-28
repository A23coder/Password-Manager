package com.example.password_manager

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface DataDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertData(data: DataClass)


    @Query("select * from password_table")
    fun getAllData(): List<DataClass>

    @Query("SELECT * FROM password_table WHERE id = :id")
    suspend fun getDataById(id: Int): DataClass

    @Update
    suspend fun updateData(data: DataClass)


    @Query("DELETE FROM password_table WHERE id = :id")
    suspend fun deleteDataById(id: Int)
}