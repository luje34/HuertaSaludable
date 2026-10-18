package com.example.huertasaludable.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.huertasaludable.data.model.Rol
import com.example.huertasaludable.data.repository.HuertaRepository
import com.example.huertasaludable.session.UserSessionManager
import com.example.huertasaludable.ui.auth.AuthViewModel
import com.example.huertasaludable.ui.auth.LoginScreen
import com.example.huertasaludable.ui.cultivos.CultivoDetailScreen
import com.example.huertasaludable.ui.cultivos.CultivoFormScreen
import com.example.huertasaludable.ui.cultivos.CultivosListScreen
import com.example.huertasaludable.ui.cultivos.CultivosViewModel
import com.example.huertasaludable.ui.dashboard.AdminDashboardScreen
import com.example.huertasaludable.ui.dashboard.StudentDashboardScreen
import com.example.huertasaludable.ui.historial.HistorialScreen
import com.example.huertasaludable.ui.historial.HistorialViewModel
import com.example.huertasaludable.ui.problemas.ProblemasListScreen
import com.example.huertasaludable.ui.problemas.ProblemasViewModel
import com.example.huertasaludable.ui.problemas.ReportarProblemaScreen
import com.example.huertasaludable.ui.sectores.SectorDetailScreen
import com.example.huertasaludable.ui.sectores.SectorFormScreen
import com.example.huertasaludable.ui.sectores.SectoresListScreen
import com.example.huertasaludable.ui.sectores.SectoresViewModel
import com.example.huertasaludable.ui.sensores.SensoresScreen
import com.example.huertasaludable.ui.tareas.StudentTasksScreen
import com.example.huertasaludable.ui.tareas.TareaDeliveryScreen
import com.example.huertasaludable.ui.tareas.TareaFormScreen
import com.example.huertasaludable.ui.tareas.TareaReviewScreen
import com.example.huertasaludable.ui.tareas.TareasListScreen
import com.example.huertasaludable.ui.tareas.TareasViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: HuertaRepository
) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(repository))
    val sectoresViewModel: SectoresViewModel = viewModel(factory = SectoresViewModel.Factory(repository))
    val cultivosViewModel: CultivosViewModel = viewModel(factory = CultivosViewModel.Factory(repository))
    val tareasViewModel: TareasViewModel = viewModel(factory = TareasViewModel.Factory(repository))
    val problemasViewModel: ProblemasViewModel = viewModel(factory = ProblemasViewModel.Factory(repository))
    val historialViewModel: HistorialViewModel = viewModel(factory = HistorialViewModel.Factory(repository))

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { usuario ->
                    if (usuario.rol == Rol.ADMIN) {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.StudentDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateToSectores = { navController.navigate(Screen.Sectores.route) },
                onNavigateToCultivos = { navController.navigate(Screen.Cultivos.route) },
                onNavigateToTareas = { navController.navigate(Screen.Tareas.route) },
                onNavigateToProblemas = { navController.navigate(Screen.Problemas.route) },
                onNavigateToSensores = { navController.navigate(Screen.Sensores.route) },
                onNavigateToHistorial = { navController.navigate(Screen.Historial.route) },
                onLogout = {
                    UserSessionManager.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.StudentDashboard.route) {
            StudentDashboardScreen(
                onNavigateToMisTareas = { navController.navigate(Screen.StudentTasks.route) },
                onNavigateToReportarProblema = { navController.navigate(Screen.ReportarProblema.route) },
                onLogout = {
                    UserSessionManager.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- MÓDULO SECTORES ---
        composable(Screen.Sectores.route) {
            SectoresListScreen(
                viewModel = sectoresViewModel,
                onSectorClick = { sectorId ->
                    navController.navigate(Screen.SectorDetail.createRoute(sectorId))
                },
                onAddSectorClick = {
                    navController.navigate(Screen.SectorForm.createRoute(0L))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SectorDetail.route,
            arguments = listOf(navArgument("sectorId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sectorId = backStackEntry.arguments?.getLong("sectorId") ?: 0L
            SectorDetailScreen(
                sectorId = sectorId,
                viewModel = sectoresViewModel,
                onEditSectorClick = { id ->
                    navController.navigate(Screen.SectorForm.createRoute(id))
                },
                onCultivoClick = { cultivoId ->
                    navController.navigate(Screen.CultivoDetail.createRoute(cultivoId))
                },
                onAddCultivoClick = { secId ->
                    navController.navigate(Screen.CultivoForm.createRoute(0L, secId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SectorForm.route,
            arguments = listOf(navArgument("sectorId") {
                type = NavType.LongType
                defaultValue = 0L
            })
        ) { backStackEntry ->
            val sectorId = backStackEntry.arguments?.getLong("sectorId") ?: 0L
            SectorFormScreen(
                sectorId = sectorId,
                viewModel = sectoresViewModel,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- MÓDULO CULTIVOS ---
        composable(Screen.Cultivos.route) {
            CultivosListScreen(
                viewModel = cultivosViewModel,
                onCultivoClick = { cultivoId ->
                    navController.navigate(Screen.CultivoDetail.createRoute(cultivoId))
                },
                onAddCultivoClick = {
                    navController.navigate(Screen.CultivoForm.createRoute(0L, 0L))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CultivoDetail.route,
            arguments = listOf(navArgument("cultivoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cultivoId = backStackEntry.arguments?.getLong("cultivoId") ?: 0L
            CultivoDetailScreen(
                cultivoId = cultivoId,
                viewModel = cultivosViewModel,
                onEditCultivoClick = { id ->
                    navController.navigate(Screen.CultivoForm.createRoute(id, 0L))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CultivoForm.route,
            arguments = listOf(
                navArgument("cultivoId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("sectorId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val cultivoId = backStackEntry.arguments?.getLong("cultivoId") ?: 0L
            val sectorId = backStackEntry.arguments?.getLong("sectorId") ?: 0L
            CultivoFormScreen(
                cultivoId = cultivoId,
                sectorIdInicial = sectorId,
                viewModel = cultivosViewModel,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- MÓDULO TAREAS ---
        composable(Screen.Tareas.route) {
            TareasListScreen(
                viewModel = tareasViewModel,
                onTareaClick = { tareaId ->
                    navController.navigate(Screen.TareaReview.createRoute(tareaId))
                },
                onAddTareaClick = {
                    navController.navigate(Screen.TareaForm.createRoute(0L))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TareaForm.route,
            arguments = listOf(navArgument("tareaId") {
                type = NavType.LongType
                defaultValue = 0L
            })
        ) { backStackEntry ->
            val tareaId = backStackEntry.arguments?.getLong("tareaId") ?: 0L
            TareaFormScreen(
                tareaId = tareaId,
                viewModel = tareasViewModel,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.StudentTasks.route) {
            StudentTasksScreen(
                viewModel = tareasViewModel,
                onDeliverTaskClick = { tareaId ->
                    navController.navigate(Screen.TareaDelivery.createRoute(tareaId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TareaDelivery.route,
            arguments = listOf(navArgument("tareaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tareaId = backStackEntry.arguments?.getLong("tareaId") ?: 0L
            TareaDeliveryScreen(
                tareaId = tareaId,
                viewModel = tareasViewModel,
                onDeliverySuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TareaReview.route,
            arguments = listOf(navArgument("tareaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tareaId = backStackEntry.arguments?.getLong("tareaId") ?: 0L
            TareaReviewScreen(
                tareaId = tareaId,
                viewModel = tareasViewModel,
                onReviewSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- MÓDULO PROBLEMAS Y SENSORES ---
        composable(Screen.Problemas.route) {
            ProblemasListScreen(
                viewModel = problemasViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.ReportarProblema.route) {
            ReportarProblemaScreen(
                viewModel = problemasViewModel,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Sensores.route) {
            SensoresScreen(
                viewModel = problemasViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // --- MÓDULO HISTORIAL ---
        composable(Screen.Historial.route) {
            HistorialScreen(
                viewModel = historialViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
