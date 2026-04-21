package com.maayan.studytracker.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maayan.studytracker.ui.schedule.ScheduleScreen
import com.maayan.studytracker.ui.stats.StatsScreen
import com.maayan.studytracker.ui.timer.TimerScreen
import com.maayan.studytracker.ui.topic.TopicFolderScreen

object Routes {
    const val SCHEDULE = "schedule"
    const val TIMER = "timer"
    const val TOPIC = "topic/{projectId}/{topicName}?folderId={folderId}"
    const val STATS = "stats"

    fun topic(projectId: Long, topicName: String, folderId: Long? = null): String {
        val encoded = Uri.encode(topicName)
        return if (folderId == null) "topic/$projectId/$encoded?folderId=-1"
        else "topic/$projectId/$encoded?folderId=$folderId"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.SCHEDULE) {
        composable(Routes.SCHEDULE) {
            ScheduleScreen(
                onOpenTopic = { projectId, topicName ->
                    navController.navigate(Routes.topic(projectId, topicName))
                },
                onOpenStats = { navController.navigate(Routes.STATS) }
            )
        }
        composable(Routes.TIMER) {
            TimerScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.TOPIC,
            arguments = listOf(
                navArgument("projectId") { type = NavType.LongType },
                navArgument("topicName") { type = NavType.StringType },
                navArgument("folderId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entry ->
            val projectId = entry.arguments?.getLong("projectId") ?: return@composable
            val topicName = Uri.decode(entry.arguments?.getString("topicName").orEmpty())
            val folderIdArg = entry.arguments?.getLong("folderId") ?: -1L
            TopicFolderScreen(
                projectId = projectId,
                topicName = topicName,
                initialFolderId = if (folderIdArg < 0) null else folderIdArg,
                onOpenSubfolder = { subId ->
                    navController.navigate(Routes.topic(projectId, topicName, subId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
    }
}
