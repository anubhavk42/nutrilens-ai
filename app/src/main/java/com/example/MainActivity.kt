package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.NutriViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Make status bar icons dark so they are visible on light background
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true
        setContent {
            MyApplicationTheme {
                NutriLensApp()
            }
        }
    }
}

@Composable
fun NutriLensApp() {
    val navController = rememberNavController()
    val viewModel: NutriViewModel = viewModel()

    val currentUserEmail = viewModel.currentUserEmail

    // Monitor authentication session status reactively
    androidx.compose.runtime.LaunchedEffect(currentUserEmail) {
        if (currentUserEmail == null) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != null && currentRoute != "splash" && currentRoute != "auth") {
                navController.navigate("auth") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Smooth navigation transitions specifications
    val prefs = androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("nutrilens_prefs", android.content.Context.MODE_PRIVATE)
    val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)

    NavHost(
        navController = navController,
        startDestination = if (hasSeenOnboarding) "splash" else "onboarding",
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 300 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -300 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -300 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 300 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable("onboarding") {
            com.example.ui.OnboardingScreen(
                onFinished = {
                    prefs.edit().putBoolean("has_seen_onboarding", true).apply()
                    navController.navigate("app_tour") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("app_tour") {
            com.example.ui.AppTourScreen(
                onFinished = {
                    navController.navigate("splash") {
                        popUpTo("app_tour") { inclusive = true }
                    }
                }
            )
        }

        composable("splash") {
            SplashScreen(onContinue = {
                if (viewModel.isAuthenticated) {
                    navController.navigate("dashboard") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    navController.navigate("auth") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            })
        }

        composable("auth") {
            AuthScreen(
                viewModel = viewModel,
                onAuthSuccess = {
                    navController.navigate("goal_setup") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("goal_setup") {
            GoalSetupScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onContinue = { navController.navigate("profile_numbers") }
            )
        }

        composable("profile_numbers") {
            ProfileNumbersScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSetMyGoals = {
                    navController.navigate("dashboard") {
                        popUpTo("goal_setup") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            // MainScreenContainer hosts home, journal, insights, profile, chatbot tabs inside Scaffold
            MainScreenContainer(
                viewModel = viewModel,
                initialRoute = "dashboard",
                onNavigateToSetup = { navController.navigate("goal_setup") },
                onNavigateToScanResult = { navController.navigate("camera_scan") },
                onNavigateToBarcodeScan = { navController.navigate("barcode_scan") },
                onNavigateToAlternatives = { navController.navigate("alternatives_browser") },
                onNavigateToNudges = { navController.navigate("notification_hub") },
                onNavigateToSearch = { navController.navigate("search_screen") },
                onNavigateToWeightTracker = { navController.navigate("weight_tracker") },
                onNavigateToDietPlan = { navController.navigate("diet_plan") }
            )
        }

        composable("weight_tracker") {
            com.example.ui.WeightTrackerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("diet_plan") {
            com.example.ui.DietPlanScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("search_screen") {
            SearchScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("notification_hub") {
            NotificationCenterScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("camera_scan") {
            CameraScanScreen(
                viewModel = viewModel,
                onNavigateToResult = { navController.navigate("meal_analysis") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("meal_analysis") {
            MealAnalysisScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogMealSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("barcode_scan") {
            BarcodeScanScreen(
                viewModel = viewModel,
                onNavigateToResult = { navController.navigate("meal_analysis") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("alternatives_browser") {
            ExtraAlternativesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("perm_modal") {
            ExtraPermissionScreen(
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
