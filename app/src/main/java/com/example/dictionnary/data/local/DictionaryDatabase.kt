package com.example.dictionnary.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        EntryEntity::class,
        TranslationEntity::class,
        FavoriteEntity::class,
        RecentSearchEntity::class,
        CorrectionEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class DictionaryDatabase : RoomDatabase() {
    abstract fun dictionaryDao(): DictionaryDao

    companion object {
        @Volatile
        private var instance: DictionaryDatabase? = null

        fun getInstance(context: Context): DictionaryDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DictionaryDatabase::class.java,
                    "dictionary.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { database ->
                    instance = database
                }
            }
        }
    }
}
