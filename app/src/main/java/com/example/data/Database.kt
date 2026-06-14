package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Anubhav",
    val sex: String = "Male",
    val age: Int = 29,
    val weight: Int = 75,
    val height: Int = 175,
    val activityLevel: String = "Lightly active",
    val goal: String = "Manage Weight",
    val caloriesBaseline: Int = 1840,
    val proteinTarget: Int = 92,
    val carbsTarget: Int = 230,
    val fatTarget: Int = 61,
    val dietaryPreferences: String = "Vegetarian,Gluten-Free,Low Sodium"
)

@Entity(tableName = "logged_meals")
data class LoggedMeal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val timestamp: Long,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val score: Int = 78,
    val isVerified: Boolean = true,
    val imageUrl: String? = null,
    val portionQuantity: Double = 1.0,
    val portionUnit: String = "katori"
)


@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weightKg: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val isBot: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val name: String = ""
)

// --- DAO Interface ---

@Dao
interface NutriDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOneShot(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserAccount(email: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(account: UserAccount)

    @Query("SELECT * FROM logged_meals ORDER BY timestamp DESC")
    fun getAllLoggedMeals(): Flow<List<LoggedMeal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: LoggedMeal)

    @Delete
    suspend fun deleteMeal(meal: LoggedMeal)

    @Query("DELETE FROM logged_meals")
    suspend fun clearAllMeals()

    @Query("SELECT * FROM weight_logs ORDER BY timestamp ASC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(log: WeightLog)

    @Delete
    suspend fun deleteWeightLog(log: WeightLog)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()
}

// --- App Database Class ---

@Database(entities = [UserProfile::class, LoggedMeal::class, ChatMessage::class, UserAccount::class, WeightLog::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nutriDao(): NutriDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutrilens_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Repository Implementation ---

class NutriRepository(private val dao: NutriDao) {
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allMeals: Flow<List<LoggedMeal>> = dao.getAllLoggedMeals()
    val chatMessages: Flow<List<ChatMessage>> = dao.getAllChatMessages()

    suspend fun getUserProfileOneShot(): UserProfile {
        return dao.getUserProfileOneShot() ?: UserProfile()
    }

    suspend fun saveProfile(profile: UserProfile) {
        dao.saveUserProfile(profile)
    }

    suspend fun logMeal(meal: LoggedMeal) {
        dao.insertMeal(meal)
    }

    suspend fun deleteMeal(meal: LoggedMeal) {
        dao.deleteMeal(meal)
    }

    suspend fun clearAllMeals() {
        dao.clearAllMeals()
    }

    suspend fun addChatMessage(message: ChatMessage) {
        dao.insertChatMessage(message)
    }

    val weightLogs: Flow<List<WeightLog>> = dao.getAllWeightLogs()

    suspend fun logWeight(log: WeightLog) {
        dao.insertWeightLog(log)
    }

    suspend fun deleteWeightLog(log: WeightLog) {
        dao.deleteWeightLog(log)
    }

    suspend fun clearChatHistory() {
        dao.clearChatHistory()
    }

    suspend fun getUserAccount(email: String): UserAccount? {
        return dao.getUserAccount(email)
    }

    suspend fun saveUserAccount(account: UserAccount) {
        dao.insertUserAccount(account)
    }

    // Populate helper to pre-fill database with sample data matching reference screens
    suspend fun populateStaticSampleDataIfEmpty() {
        val currentProfile = dao.getUserProfileOneShot()
        if (currentProfile == null) {
            dao.saveUserProfile(UserProfile()) // Default profile Meera/Anubhav
        }

        // Just check if logged meals is empty, then populate
        // To query flow immediately we can check a static query or do fallback check.
    }
}
