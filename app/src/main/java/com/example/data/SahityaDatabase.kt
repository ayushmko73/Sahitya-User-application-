package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CharacterStoryEntity::class,
        ActiveChatEntity::class,
        ChatMessageEntity::class,
        ChatSummaryEntity::class,
        UserWalletEntity::class,
        TransactionEntity::class,
        UserChallengeEntity::class,
        UserPreferencesEntity::class,
        AgentLearningStoryEntity::class,
        AgentLearningM1BlockEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class SahityaDatabase : RoomDatabase() {
    abstract fun sahityaDao(): SahityaDao

    companion object {
        @Volatile
        private var INSTANCE: SahityaDatabase? = null

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user_wallet ADD COLUMN lastClaimDate INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE active_chats ADD COLUMN apiKeyIndex INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): SahityaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SahityaDatabase::class.java,
                    "sahitya_database"
                )
                    .addMigrations(MIGRATION_8_9)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
