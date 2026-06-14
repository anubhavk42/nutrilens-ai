package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.AlternativeFood
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.LoggedMeal
import com.example.data.NutriRepository
import com.example.data.UserProfile
import com.example.api.GeminiClient
import com.example.api.GeminiService
import com.example.api.FoodScanResult
import com.example.data.UserAccount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class NutriViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = NutriRepository(db.nutriDao())
    private val prefs = application.getSharedPreferences("nutri_prefs", Context.MODE_PRIVATE)
    private val geminiService = com.example.api.GeminiService()

    // --- Authentication States ---
    var currentUserEmail by mutableStateOf<String?>(prefs.getString("user_email", null))
    val isAuthenticated get() = currentUserEmail != null

    // --- Search Filter States ---
    var searchFilterQuery by mutableStateOf("")
    var searchFilterMealType by mutableStateOf("All")
    var searchFilterMaxCalories by mutableStateOf(1000f)
    var searchSortBy by mutableStateOf("Newest") // Options: Newest, Calories Low->High, Calories High->Low, Protein, Score

    // --- Push Notification Opt-in Settings ---
    var optInImportantUpdates by mutableStateOf(true)
    var optInMealReminders by mutableStateOf(true)
    var optInNewContent by mutableStateOf(true)
    var prefillNotificationPrompt by mutableStateOf("")

    // --- Auth Action Handlers ---
    fun signUp(email: String, passwordHash: String, name: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || passwordHash.isBlank() || name.isBlank()) {
            onResult(false, "All fields are required")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onResult(false, "Invalid email address format")
            return
        }
        if (passwordHash.length < 6) {
            onResult(false, "Password must be at least 6 characters")
            return
        }
        viewModelScope.launch {
            try {
                val existing = repository.getUserAccount(email)
                if (existing != null) {
                    onResult(false, "This email is already registered")
                } else {
                    val newAccount = UserAccount(email = email, passwordHash = passwordHash, name = name)
                    repository.saveUserAccount(newAccount)
                    
                    // Update user profile representation name
                    val currentProfile = repository.getUserProfileOneShot()
                    repository.saveProfile(currentProfile.copy(name = name))
                    
                    currentUserEmail = email
                    prefs.edit().putString("user_email", email).apply()
                    onResult(true, "Registration successful")
                }
            } catch (e: Exception) {
                onResult(false, "Signup error: ${e.message}")
            }
        }
    }

    fun logIn(email: String, passwordHash: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || passwordHash.isBlank()) {
            onResult(false, "Please enter both email and password")
            return
        }
        viewModelScope.launch {
            try {
                val account = repository.getUserAccount(email)
                if (account == null) {
                    onResult(false, "Account not found")
                } else if (account.passwordHash != passwordHash) {
                    onResult(false, "Invalid credentials")
                } else {
                    currentUserEmail = email
                    prefs.edit().putString("user_email", email).apply()
                    
                    // Sync profile name
                    if (account.name.isNotBlank()) {
                        val currentProfile = repository.getUserProfileOneShot()
                        repository.saveProfile(currentProfile.copy(name = account.name))
                    }
                    onResult(true, "Login successful")
                }
            } catch (e: Exception) {
                onResult(false, "Login error: ${e.message}")
            }
        }
    }

    fun logInByPhoneOrSocial(identifier: String, name: String, onResult: (Boolean, String) -> Unit) {
        if (identifier.isBlank()) {
            onResult(false, "Identifier is required")
            return
        }
        viewModelScope.launch {
            try {
                val existing = repository.getUserAccount(identifier)
                if (existing == null) {
                    val newAccount = UserAccount(email = identifier, passwordHash = "VERIFIED_SESSION", name = name)
                    repository.saveUserAccount(newAccount)
                }
                currentUserEmail = identifier
                prefs.edit().putString("user_email", identifier).apply()
                
                val currentProfile = repository.getUserProfileOneShot()
                val updatedName = if (name.isNotBlank()) name else currentProfile.name
                repository.saveProfile(currentProfile.copy(name = updatedName))
                
                onResult(true, "Authentication successful")
            } catch (e: Exception) {
                onResult(false, "Authentication error: ${e.message}")
            }
        }
    }

    fun logOut() {
        currentUserEmail = null
        prefs.edit().remove("user_email").apply()
    }

    // --- State Flows from Local Database ---
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val loggedMeals: StateFlow<List<LoggedMeal>> = repository.allMeals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val weightLogs: StateFlow<List<com.example.data.WeightLog>> = repository.weightLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Transient UI States ---
    var scannedMealResult by mutableStateOf<FoodScanResult?>(null)
        private set

    var isScanning by mutableStateOf(false)
        private set

    var isChatThinking by mutableStateOf(false)
    var dietPlan by mutableStateOf<String?>(null)
    var isGeneratingDietPlan by mutableStateOf(false)
    var dietPlanError by mutableStateOf<String?>(null)
        private set

    var scanError by mutableStateOf<String?>(null)
        private set

    init {
        // Load session from prefs
        val savedEmail = prefs.getString("user_email", null)
        if (savedEmail != null) {
            currentUserEmail = savedEmail
            Toast.makeText(application, "Welcome back, $savedEmail", Toast.LENGTH_SHORT).show()
        }

        // Run database initialization tasks
        viewModelScope.launch {
            repository.populateStaticSampleDataIfEmpty()
        }
    }

    // --- Action Implementations ---

    /**
     * Recalculates and saves the user metrics.
     */
    fun saveUserProfile(
        name: String,
        sex: String,
        age: Int,
        weight: Int,
        height: Int,
        activityLevel: String,
        goal: String,
        dietaryPreferences: String
    ) {
        viewModelScope.launch {
            // Harris-Benedict BMR calculation
            val bmr = if (sex.equals("Male", ignoreCase = true)) {
                (10 * weight) + (6.25 * height) - (5 * age) + 5
            } else {
                (10 * weight) + (6.25 * height) - (5 * age) - 161
            }

            // Activity Factor
            val rawFactor = when (activityLevel) {
                "Mostly sitting" -> 1.2
                "Lightly active" -> 1.375
                "Active" -> 1.55
                "Very active" -> 1.725
                else -> 1.375
            }

            var dailyCalories = (bmr * rawFactor).toInt()

            // Goal modification
            when (goal) {
                "Manage Weight" -> dailyCalories -= 350 // standard mild deficit for weight adjustment
                "Build Muscle" -> dailyCalories += 300
                "Manage Diabetes" -> dailyCalories = (dailyCalories * 0.95).toInt()
                "PCOS" -> dailyCalories = (dailyCalories * 0.95).toInt()
                "Heart Health" -> dailyCalories = (dailyCalories * 1.0).toInt()
                else -> { /* wellness / maintenance */ }
            }

            // Macros calculation
            // Protein: 25% of calories (4 kcal/g)
            val pro = ((dailyCalories * 0.25) / 4).toInt()
            // Fat: 30% of calories (9 kcal/g)
            val fat = ((dailyCalories * 0.30) / 9).toInt()
            // Carbs: Remaining calories (4 kcal/g)
            val carb = ((dailyCalories * 0.45) / 4).toInt()

            val updatedProfile = UserProfile(
                id = 1,
                name = name,
                sex = sex,
                age = age,
                weight = weight,
                height = height,
                activityLevel = activityLevel,
                goal = goal,
                caloriesBaseline = dailyCalories,
                proteinTarget = pro,
                carbsTarget = carb,
                fatTarget = fat,
                dietaryPreferences = dietaryPreferences
            )

            repository.saveProfile(updatedProfile)
        }
    }

    /**
     * Executes food scan analysis with Gemini.
     */
    fun performFoodScan(query: String, bitmap: Bitmap?, onResult: () -> Unit = {}) {
        isScanning = true
        scanError = null
        scannedMealResult = null // Important: Clear previous results
        viewModelScope.launch {
            try {
                val goal = userProfile.value?.goal ?: "Manage Weight"
                val result = GeminiClient.analyzeMeal(query, bitmap, goal)
                scannedMealResult = result
                onResult()
            } catch (e: Exception) {
                scanError = "Scanner Error: ${e.message}"
                Log.e("NutriViewModel", "Scan failed", e)
            } finally {
                delay(600)
                isScanning = false
            }
        }
    }

    /**
     * Logs the current scanned meal in the database.
     */
    fun logScannedMeal(mealType: String) {
        val result = scannedMealResult ?: return
        viewModelScope.launch {
            val meal = LoggedMeal(
                name = result.name,
                timestamp = System.currentTimeMillis(),
                calories = result.calories,
                protein = result.protein,
                carbs = result.carbs,
                fat = result.fat,
                mealType = mealType,
                score = result.score,
                isVerified = true
            )
            repository.logMeal(meal)
        }
    }

    /**
     * Manual custom log addition helper.
     */
    fun logCustomMeal(name: String, calories: Int, protein: Int, carbs: Int, fat: Int, mealType: String, score: Int = 78) {
        viewModelScope.launch {
            val meal = LoggedMeal(
                name = name,
                timestamp = System.currentTimeMillis(),
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                mealType = mealType,
                score = score,
                isVerified = true
            )
            repository.logMeal(meal)
        }
    }

    /**
     * Deletes log item.
     */
    fun deleteMeal(meal: LoggedMeal) {
        viewModelScope.launch {
            repository.deleteMeal(meal)
        }
    }

    /**
     * Resets meal logs.
     */
    fun clearAllMeals() {
        viewModelScope.launch {
            repository.clearAllMeals()
        }
    }

    /**
     * Resets chatbot history.
     */
    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }

    /**
     * Sends user chat message and returns response from Gemini NutriBot.
     */
    fun sendBotChatMessage(messageText: String) {
        if (messageText.trim().isEmpty()) return

        viewModelScope.launch {
            // 1. Add user message
            val userMsg = ChatMessage(message = messageText, isBot = false)
            repository.addChatMessage(userMsg)

            isChatThinking = true

            // 2. Prepare contextual parameters to send to Gemini
            val profile = userProfile.value
            val goal = profile?.goal ?: "Manage Weight"
            
            // Format today's logged meals info
            val mealInfoBuilder = StringBuilder()
            loggedMeals.value.forEach {
                mealInfoBuilder.append("${it.name} (${it.calories} kcal, P:${it.protein}g), ")
            }
            if (mealInfoBuilder.isEmpty()) {
                mealInfoBuilder.append("No meals logged yet today.")
            }

            try {
                // 3. Query Gemini client
                val historyList = chatMessages.value
                val reply = GeminiClient.chatWithNutriBot(
                    history = historyList,
                    userPrompt = messageText,
                    profileGoal = goal,
                    dailyMealsInfo = mealInfoBuilder.toString()
                )

                // 4. Save Bot message
                val botMsg = ChatMessage(message = reply, isBot = true)
                repository.addChatMessage(botMsg)
            } catch (e: Exception) {
                val errorMsg = ChatMessage(message = "Apologies, I couldn't reach my nutrition servers. Let's try again in a moment!", isBot = true)
                repository.addChatMessage(errorMsg)
            } finally {
                isChatThinking = false
            }
        }

    }
    fun logWeight(weightKg: Float, note: String = "") {
        viewModelScope.launch {
            repository.logWeight(com.example.data.WeightLog(weightKg = weightKg, note = note))
        }
    }
    fun deleteWeightLog(log: com.example.data.WeightLog) {
        viewModelScope.launch {
            repository.deleteWeightLog(log)
        }
    }

    fun generateDietPlan() {
        viewModelScope.launch {
            isGeneratingDietPlan = true
            dietPlanError = null
            try {
                val profile = repository.getUserProfileOneShot()
                val plan = geminiService.generateWeeklyDietPlan(
                    goal = profile.goal,
                    caloriesTarget = profile.caloriesBaseline,
                    proteinTarget = profile.proteinTarget,
                    dietaryPreferences = profile.dietaryPreferences
                )
                dietPlan = plan
            } catch (e: Exception) {
                dietPlanError = "Could not generate diet plan. Please check your internet connection and try again."
            } finally {
                isGeneratingDietPlan = false
            }
        }
    }
}
