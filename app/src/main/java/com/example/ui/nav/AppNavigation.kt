package com.example.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AITattooGalleryScreen
import com.example.ui.screens.BuilderScreen
import com.example.ui.screens.DivinationScreen
import com.example.ui.screens.EncyclopediaScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NameStaveScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SketchScreen
import com.example.ui.screens.TryOnScreen
import com.example.ui.viewmodel.RuneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppHost(
    viewModel: RuneViewModel,
    navController: NavHostController = rememberNavController()
) {
    val runes by viewModel.runes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem.NameStave,
        BottomNavItem.Builder,
        BottomNavItem.Library,
        BottomNavItem.Encyclopedia,
        BottomNavItem.Divination
    )

    val isRootTab = bottomNavItems.any { it.route == currentRoute }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Scaffold(
        topBar = {
            if (isRootTab) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Рунический Став",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(AppDestinations.AI_TATTOO_GALLERY) }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "ИИ Тату-Концепты", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { navController.navigate(AppDestinations.FAVORITES) }) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Сохранённые ставы", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { navController.navigate(AppDestinations.SETTINGS) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Настройки", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (isRootTab) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.titleRu) },
                            label = { Text(item.titleRu, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val startDestination = if (userSettings.hasCompletedOnboarding) {
            BottomNavItem.NameStave.route
        } else {
            AppDestinations.ONBOARDING
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestinations.ONBOARDING) {
                OnboardingScreen(
                    viewModel = viewModel,
                    onComplete = {
                        navController.navigate(BottomNavItem.NameStave.route) {
                            popUpTo(AppDestinations.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(BottomNavItem.NameStave.route) {
                NameStaveScreen(
                    viewModel = viewModel,
                    allRunes = runes,
                    onNavigateToSketch = { ids, layout ->
                        navController.navigate(AppDestinations.buildSketchRoute(ids, layout))
                    },
                    onNavigateToTryOn = { ids, layout, seed, style ->
                        navController.navigate(AppDestinations.buildTryOnRoute(ids, layout, seed, style))
                    }
                )
            }

            composable(BottomNavItem.Builder.route) {
                BuilderScreen(
                    viewModel = viewModel,
                    allRunes = runes,
                    onNavigateToSketch = { ids, layout ->
                        navController.navigate(AppDestinations.buildSketchRoute(ids, layout))
                    },
                    onNavigateToTryOn = { ids, layout, seed, style ->
                        navController.navigate(AppDestinations.buildTryOnRoute(ids, layout, seed, style))
                    }
                )
            }

            composable(BottomNavItem.Library.route) {
                LibraryScreen(
                    allRunes = runes,
                    onNavigateToSketch = { ids, layout ->
                        navController.navigate(AppDestinations.buildSketchRoute(ids, layout))
                    },
                    onNavigateToTryOn = { ids, layout, seed, style ->
                        navController.navigate(AppDestinations.buildTryOnRoute(ids, layout, seed, style))
                    },
                    onNavigateToAITattoo = {
                        navController.navigate(AppDestinations.AI_TATTOO_GALLERY)
                    }
                )
            }

            composable(BottomNavItem.Encyclopedia.route) {
                EncyclopediaScreen(
                    allRunes = runes,
                    onSelectRuneForBuilder = { runeId ->
                        navController.navigate(BottomNavItem.Builder.route)
                    }
                )
            }

            composable(BottomNavItem.Divination.route) {
                DivinationScreen(
                    allRunes = runes,
                    onNavigateToSketch = { ids, layout ->
                        navController.navigate(AppDestinations.buildSketchRoute(ids, layout))
                    }
                )
            }

            composable(
                route = AppDestinations.SKETCH,
                arguments = listOf(
                    navArgument("runeIds") { type = NavType.StringType },
                    navArgument("layoutType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val runeIdsParam = backStackEntry.arguments?.getString("runeIds") ?: ""
                val layoutTypeParam = backStackEntry.arguments?.getString("layoutType") ?: "BINDRUNE"
                val runeIds = runeIdsParam.split(",").filter { it.isNotEmpty() }

                SketchScreen(
                    runeIds = runeIds,
                    layoutTypeName = layoutTypeParam,
                    allRunes = runes,
                    onBack = { navController.popBackStack() },
                    onNavigateToTryOn = { ids, layout, seed, style ->
                        navController.navigate(AppDestinations.buildTryOnRoute(ids, layout, seed, style))
                    },
                    onNavigateToAITattoo = {
                        navController.navigate(AppDestinations.AI_TATTOO_GALLERY)
                    }
                )
            }

            composable(
                route = AppDestinations.TRY_ON,
                arguments = listOf(
                    navArgument("runeIds") { type = NavType.StringType },
                    navArgument("layoutType") { type = NavType.StringType },
                    navArgument("seed") { type = NavType.LongType },
                    navArgument("style") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val runeIdsParam = backStackEntry.arguments?.getString("runeIds") ?: ""
                val layoutTypeParam = backStackEntry.arguments?.getString("layoutType") ?: "BINDRUNE"
                val seedParam = backStackEntry.arguments?.getLong("seed") ?: 1337L
                val styleParam = backStackEntry.arguments?.getString("style") ?: "ORNAMENTAL"
                val runeIds = runeIdsParam.split(",").filter { it.isNotEmpty() }

                TryOnScreen(
                    runeIds = runeIds,
                    layoutTypeName = layoutTypeParam,
                    seed = seedParam,
                    styleName = styleParam,
                    allRunes = runes,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(AppDestinations.FAVORITES) {
                FavoritesScreen(
                    viewModel = viewModel,
                    allRunes = runes,
                    onBack = { navController.popBackStack() },
                    onNavigateToSketch = { ids, layout ->
                        navController.navigate(AppDestinations.buildSketchRoute(ids, layout))
                    },
                    onNavigateToTryOn = { ids, layout, seed, style ->
                        navController.navigate(AppDestinations.buildTryOnRoute(ids, layout, seed, style))
                    }
                )
            }

            composable(AppDestinations.AI_TATTOO_GALLERY) {
                AITattooGalleryScreen(
                    viewModel = viewModel,
                    allRunes = runes,
                    onBack = { navController.popBackStack() },
                    onNavigateToSketch = { ids, layout ->
                        navController.navigate(AppDestinations.buildSketchRoute(ids, layout))
                    },
                    onNavigateToTryOn = { ids, layout, seed, style ->
                        navController.navigate(AppDestinations.buildTryOnRoute(ids, layout, seed, style))
                    }
                )
            }

            composable(AppDestinations.SETTINGS) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onResetOnboarding = {
                        navController.navigate(AppDestinations.ONBOARDING) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
