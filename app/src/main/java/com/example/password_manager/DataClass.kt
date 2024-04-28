package com.example.password_manager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_table")
data class DataClass(
    @PrimaryKey(autoGenerate = true) val id: Int ,
    val account_name :String,
    val username :String,
    val password :String,
)