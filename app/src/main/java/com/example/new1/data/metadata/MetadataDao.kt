package com.example.new1.data.metadata

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MetadataDao {
    @Query("SELECT * FROM category_options ORDER BY position ASC, id ASC")
    fun listCategoryOptions(): List<CategoryOptionEntity>

    @Query("DELETE FROM category_options")
    fun clearCategoryOptions()

    @Insert
    fun insertCategoryOptions(options: List<CategoryOptionEntity>)

    @Query("SELECT COUNT(*) FROM category_options")
    fun countCategoryOptions(): Int

    @Query("SELECT * FROM type_field_configs ORDER BY type_normalized_label ASC, position ASC")
    fun listTypeFieldConfigs(): List<TypeFieldConfigEntity>

    @Query("DELETE FROM type_field_configs")
    fun clearTypeFieldConfigs()

    @Insert
    fun insertTypeFieldConfigs(configs: List<TypeFieldConfigEntity>)

    @Query("SELECT COUNT(*) FROM type_field_configs")
    fun countTypeFieldConfigs(): Int

    @Query("SELECT * FROM type_date_formats")
    fun listTypeDateFormats(): List<TypeDateFormatEntity>

    @Query("DELETE FROM type_date_formats")
    fun clearTypeDateFormats()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTypeDateFormats(formats: List<TypeDateFormatEntity>)

    @Query("SELECT COUNT(*) FROM type_date_formats")
    fun countTypeDateFormats(): Int
}
