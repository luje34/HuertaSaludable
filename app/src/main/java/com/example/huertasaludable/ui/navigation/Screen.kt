package com.example.huertasaludable.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object AdminDashboard : Screen("admin_dashboard")
    object StudentDashboard : Screen("student_dashboard")

    object Sectores : Screen("sectores")
    object SectorDetail : Screen("sector_detail/{sectorId}") {
        fun createRoute(sectorId: Long) = "sector_detail/$sectorId"
    }
    object SectorForm : Screen("sector_form?sectorId={sectorId}") {
        fun createRoute(sectorId: Long = 0L) = "sector_form?sectorId=$sectorId"
    }

    object Cultivos : Screen("cultivos")
    object CultivoDetail : Screen("cultivo_detail/{cultivoId}") {
        fun createRoute(cultivoId: Long) = "cultivo_detail/$cultivoId"
    }
    object CultivoForm : Screen("cultivo_form?cultivoId={cultivoId}&sectorId={sectorId}") {
        fun createRoute(cultivoId: Long = 0L, sectorId: Long = 0L) = "cultivo_form?cultivoId=$cultivoId&sectorId=$sectorId"
    }

    object Tareas : Screen("tareas")
    object TareaForm : Screen("tarea_form?tareaId={tareaId}") {
        fun createRoute(tareaId: Long = 0L) = "tarea_form?tareaId=$tareaId"
    }
    object StudentTasks : Screen("student_tasks")
    object TareaDelivery : Screen("tarea_delivery/{tareaId}") {
        fun createRoute(tareaId: Long) = "tarea_delivery/$tareaId"
    }
    object TareaReview : Screen("tarea_review/{tareaId}") {
        fun createRoute(tareaId: Long) = "tarea_review/$tareaId"
    }

    object Problemas : Screen("problemas")
    object ReportarProblema : Screen("reportar_problema")
    object Historial : Screen("historial")
    object Sensores : Screen("sensores")
}
