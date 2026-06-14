package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.api.AlternativeFood
import com.example.data.LoggedMeal
import com.example.ui.theme.*
import com.example.viewmodel.NutriViewModel

@Composable
fun SearchScreen(
    viewModel: NutriViewModel,
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val loggedMeals by viewModel.loggedMeals.collectAsStateWithLifecycle()

    // Base alternatives database to search
    val alternativesDb = listOf(
        AlternativeFood("Palak Dal (Spinach Lentils)", 92, "+14 points", "Slices fat from tempering while adding iron rich spinach fibre.", 14, 26, 9),
        AlternativeFood("Sprouted Moong Salad", 85, "+7 points", "A crisp, fiber powerhouse with 0 gluten carbs.", 12, 18, 7),
        AlternativeFood("True Elements Rolled Oats", 94, "+12 points", "100% natural, whole oat hulls without sodium preservatives.", 14, 62, 11),
        AlternativeFood("Yoga Bar Seed Muesli", 88, "+6 points", "Super seed toppings adding zinc and good polyunsaturated lipids.", 12, 54, 8),
        AlternativeFood("Turmeric Ginger Herbal Infusion", 90, "+25 points", "Sugar-free warming tea that mitigates inflammatory indicators.", 1, 4, 1),
        AlternativeFood("Brown Rice & Tofu Stirfry", 82, "+10 points", "Lean, high protein swap substituting white refined grain starch.", 18, 48, 6),
        AlternativeFood("Ragi Roti with Curd", 87, "+9 points", "Finger millet bread offering slow-burning calcium-rich carbohydrates.", 10, 38, 8)
    )

    // Filtered Logged Meals
    val filteredMeals = remember(loggedMeals, viewModel.searchFilterQuery, viewModel.searchFilterMealType, viewModel.searchFilterMaxCalories, viewModel.searchSortBy) {
        var list = loggedMeals.filter { meal ->
            val matchesQuery = meal.name.contains(viewModel.searchFilterQuery, ignoreCase = true) ||
                    meal.mealType.contains(viewModel.searchFilterQuery, ignoreCase = true)
            val matchesMealType = viewModel.searchFilterMealType == "All" || meal.mealType.equals(viewModel.searchFilterMealType, ignoreCase = true)
            val matchesCalories = meal.calories <= viewModel.searchFilterMaxCalories

            matchesQuery && matchesMealType && matchesCalories
        }

        // Sorting
        list = when (viewModel.searchSortBy) {
            "Calories Low->High" -> list.sortedBy { it.calories }
            "Calories High->Low" -> list.sortedByDescending { it.calories }
            "Protein" -> list.sortedByDescending { it.protein }
            "NutriScore" -> list.sortedByDescending { it.score }
            else -> list.sortedByDescending { it.timestamp } // Newest
        }
        list
    }

    // Filtered Alternatives
    val filteredAlternatives = remember(viewModel.searchFilterQuery, viewModel.searchFilterMaxCalories, viewModel.searchSortBy) {
        var list = alternativesDb.filter { alt ->
            val matchesQuery = alt.name.contains(viewModel.searchFilterQuery, ignoreCase = true) ||
                    alt.description.contains(viewModel.searchFilterQuery, ignoreCase = true)
            // Alternatives don't have explicit meal categories but let's filter by estimated carbs/calories or general query
            val matchesCalories = (alt.carbs * 4 + alt.protein * 4) <= viewModel.searchFilterMaxCalories

            matchesQuery && matchesCalories
        }

        list = when (viewModel.searchSortBy) {
            "Protein" -> list.sortedByDescending { alt -> alt.protein }
            "NutriScore" -> list.sortedByDescending { alt -> alt.score }
            else -> list.sortedBy { alt -> alt.name }
        }
        list
    }

    Scaffold(
        topBar = {
            NutriTopAppBar(
                title = "Advanced Search",
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        containerColor = OrganicBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
                .testTag("search_page_parent")
        ) {
            // Live Search input with cancel toggle
            TextField(
                value = viewModel.searchFilterQuery,
                onValueChange = { viewModel.searchFilterQuery = it },
                label = { Text("Search meals or healthy alternatives") },
                placeholder = { Text("try Dal, Salad, or tea...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search bar") },
                trailingIcon = {
                    if (viewModel.searchFilterQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchFilterQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search query")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_query_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = BentoPurpleMedium,
                    unfocusedIndicatorColor = BentoBorderGrey
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bento Grid filter panels
            Text(
                text = "REFINE BY MEAL CATEGORY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BentoTextGrey)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable Pill filters row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Breakfast", "Lunch", "Dinner", "Snack").forEach { type ->
                    val isSelected = viewModel.searchFilterMealType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.searchFilterMealType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BentoPurpleMedium,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = BentoPurpleMedium
                        ),
                        modifier = Modifier.testTag("pill_filter_$type")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Slider Filter and Sort Column
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BentoBorderGrey)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Maximum Energy Intake: ${viewModel.searchFilterMaxCalories.toInt()} kcal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = BentoPurpleDark)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = viewModel.searchFilterMaxCalories,
                        onValueChange = { viewModel.searchFilterMaxCalories = it },
                        valueRange = 100f..1200f,
                        colors = SliderDefaults.colors(
                            thumbColor = ForestGreen,
                            activeTrackColor = ForestGreen
                        ),
                        modifier = Modifier.testTag("slider_calories_filter")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "SORT RESULTS BY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BentoTextGrey)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sort order pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Newest", "Calories Low->High", "Calories High->Low", "Protein", "NutriScore").forEach { sort ->
                            val isSelected = viewModel.searchSortBy == sort
                            OutlinedButton(
                                onClick = { viewModel.searchSortBy = sort },
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, if (isSelected) ForestGreen else BentoBorderGrey),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) ForestGreen.copy(alpha = 0.1f) else Color.White
                                ),
                                modifier = Modifier.testTag("sort_pill_$sort")
                            ) {
                                Text(
                                    text = sort,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) ForestGreen else Color.DarkGray,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Logged Meals Section ---
            Text(
                text = "LOGGED MEALS (${filteredMeals.size})",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BentoTextGrey)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredMeals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, BentoBorderGrey, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching meal logs found. Clean filters or query text.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredMeals.forEach { meal ->
                        MealRowItem(meal = meal, onDeleteClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteMeal(meal)
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Recommended Swaps Section ---
            Text(
                text = "RECOMMENDED ALTERNATIVES (${filteredAlternatives.size})",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = BentoTextGrey)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredAlternatives.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, BentoBorderGrey, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching recommended alternatives found.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(color = BentoTextGrey)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredAlternatives.forEach { alt ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White)
                                .border(1.dp, BentoBorderGrey, RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = alt.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontSize = 16.sp,
                                                color = BentoPurpleDark,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFEBF9F0))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${alt.score} Score",
                                                style = MaterialTheme.typography.labelSmall.copy(color = ForestGreen, fontSize = 9.sp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = alt.pointsText,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color(0xFFE65100),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = alt.description,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, color = BentoTextGrey)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "P: ${alt.protein}g · C: ${alt.carbs}g · F: ${alt.fiber}g (Fib)",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
