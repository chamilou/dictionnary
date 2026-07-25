package com.avardiction.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey
    val id: Long,
    val category: String? = null,
    val type: String? = null,
    val notes: String? = null,
    val sourceFile: String? = null,
    val sourcePage: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

@Entity(
    tableName = "translations",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("entryId"),
        Index("languageCode"),
        Index("normalizedText"),
        Index(value = ["languageCode", "normalizedText"]),
        Index(value = ["languageCode", "browseKey"])
    ]
)
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryId: Long,
    val languageCode: String,
    val text: String,
    val normalizedText: String,
    val browseKey: String? = null,
    val isPrimary: Boolean,
    val sourceLanguageCode: String? = null,
    val translationSource: String? = null,
    val checkedStatus: String
)

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FavoriteEntity(
    @PrimaryKey
    val entryId: Long,
    val createdAt: Long
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val normalizedQuery: String,
    val sourceLanguageCode: String,
    val targetLanguageCode: String,
    val createdAt: Long
)

@Entity(
    tableName = "corrections",
    foreignKeys = [
        ForeignKey(
            entity = EntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("entryId"), Index("languageCode")]
)
data class CorrectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryId: Long,
    val languageCode: String,
    val oldText: String,
    val suggestedText: String,
    val comment: String,
    val createdAt: Long,
    val exported: Boolean = false
)
