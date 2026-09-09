package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.ConfigType
import com.example.ui.screens.*
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.ConfiguratorViewModel
import com.example.viewmodel.PackagingViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Dashboard : Screen("dashboard")
    object JigCatalog : Screen("jig_catalog")
    object JigConfig : Screen("jig_config")
    object JigEngineering : Screen("jig_engineering")
    object RodCategory : Screen("rod_category")
    object RodConfig : Screen("rod_config")
    object RodEngineering : Screen("rod_engineering")
    object LureCatalog : Screen("lure_catalog")
    object LureConfig : Screen("lure_config")
    object LureEngineering : Screen("lure_engineering")
    object Packaging : Screen("packaging")
    object VisualQa : Screen("visual_qa")
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    configViewModel: ConfiguratorViewModel = viewModel(),
    packagingViewModel: PackagingViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val startDestination = if (currentUser != null) Screen.Dashboard.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                },
                onNavigateToVisualQa = {
                    navController.navigate(Screen.VisualQa.route)
                }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                authViewModel = authViewModel,
                onSignupSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                currentUser = currentUser,
                configViewModel = configViewModel,
                onNavigateToJigs = {
                    navController.navigate(Screen.JigCatalog.route)
                },
                onNavigateToRods = {
                    navController.navigate(Screen.RodCategory.route)
                },
                onNavigateToLures = {
                    navController.navigate(Screen.LureCatalog.route)
                },
                onNavigateToPackaging = {
                    navController.navigate(Screen.Packaging.route)
                },
                onNavigateToEngineering = { entity ->
                    configViewModel.loadSavedConfig(entity)
                    when (entity.configType) {
                        ConfigType.JIG.name -> navController.navigate(Screen.JigEngineering.route)
                        ConfigType.ROD.name -> navController.navigate(Screen.RodEngineering.route)
                        ConfigType.LURE.name -> navController.navigate(Screen.LureEngineering.route)
                        else -> navController.navigate(Screen.JigEngineering.route)
                    }
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToVisualQa = {
                    navController.navigate(Screen.VisualQa.route)
                }
            )
        }

        // JIG FLOW (Shape-First Workflow)
        composable(Screen.JigCatalog.route) {
            JigCatalogScreen(
                onSelectShape = { selectedShape ->
                    configViewModel.selectShape(selectedShape)
                    navController.navigate(Screen.JigConfig.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.JigConfig.route) {
            JigConfigScreen(
                configViewModel = configViewModel,
                onNavigateToEngineering = {
                    navController.navigate(Screen.JigEngineering.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.JigEngineering.route) {
            JigEngineeringScreen(
                configViewModel = configViewModel,
                onNavigateToEdit = {
                    navController.navigate(Screen.JigConfig.route) {
                        popUpTo(Screen.JigEngineering.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                }
            )
        }

        // ROD FLOW
        composable(Screen.RodCategory.route) {
            RodCategoryScreen(
                onSelectCategory = { category, lengthGroup ->
                    configViewModel.selectRodCategory(category, lengthGroup)
                    navController.navigate(Screen.RodConfig.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.RodConfig.route) {
            RodConfigScreen(
                configViewModel = configViewModel,
                onNavigateToEngineering = {
                    navController.navigate(Screen.RodEngineering.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.RodEngineering.route) {
            RodEngineeringScreen(
                configViewModel = configViewModel,
                onNavigateToEdit = {
                    navController.navigate(Screen.RodConfig.route) {
                        popUpTo(Screen.RodEngineering.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                }
            )
        }

        // LURE FLOW
        composable(Screen.LureCatalog.route) {
            LureCatalogScreen(
                onSelectLure = { selectedLure ->
                    configViewModel.selectLure(selectedLure)
                    navController.navigate(Screen.LureConfig.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.LureConfig.route) {
            LureConfigScreen(
                configViewModel = configViewModel,
                onNavigateToEngineering = {
                    navController.navigate(Screen.LureEngineering.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.LureEngineering.route) {
            LureEngineeringScreen(
                configViewModel = configViewModel,
                onNavigateToEdit = {
                    navController.navigate(Screen.LureConfig.route) {
                        popUpTo(Screen.LureEngineering.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                }
            )
        }

        // PACKAGING FLOW
        composable(Screen.Packaging.route) {
            PackagingScreen(
                packagingViewModel = packagingViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // VISUAL QA STUDIO
        composable(Screen.VisualQa.route) {
            VisualQaScreen(
                authViewModel = authViewModel,
                configViewModel = configViewModel,
                packagingViewModel = packagingViewModel,
                onExitQa = {
                    navController.popBackStack()
                }
            )
        }
    }
}
