package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveRecordingScreen
import com.example.ui.screens.MeetingDetailScreen
import com.example.ui.screens.SearchMeetingsScreen
import com.example.ui.viewmodel.MeetingViewModel
import java.net.URLDecoder

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    meetingViewModel: MeetingViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = meetingViewModel,
                onNavigateToRecord = { title, category ->
                    navController.navigate(Screen.LiveRecord.createRoute(title, category))
                },
                onNavigateToDetail = { meetingId ->
                    navController.navigate(Screen.MeetingDetail.createRoute(meetingId))
                },
                onNavigateToSearch = { query ->
                    navController.navigate(Screen.Search.createRoute(query))
                }
            )
        }

        composable(
            route = Screen.Search.route,
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val rawQuery = backStackEntry.arguments?.getString("query") ?: ""
            val initialQuery = try { URLDecoder.decode(rawQuery, "UTF-8") } catch (e: Exception) { rawQuery }

            SearchMeetingsScreen(
                viewModel = meetingViewModel,
                initialQuery = initialQuery,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMeeting = { meetingId, timestampSeconds ->
                    navController.navigate(
                        Screen.MeetingDetail.createRoute(meetingId, timestampSeconds)
                    )
                }
            )
        }

        composable(
            route = Screen.LiveRecord.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rawTitle = backStackEntry.arguments?.getString("title") ?: "Live Recorded Meeting"
            val rawCategory = backStackEntry.arguments?.getString("category") ?: "Strategy & Planning"
            val title = try { URLDecoder.decode(rawTitle, "UTF-8") } catch (e: Exception) { rawTitle }
            val category = try { URLDecoder.decode(rawCategory, "UTF-8") } catch (e: Exception) { rawCategory }

            LiveRecordingScreen(
                title = title,
                category = category,
                viewModel = meetingViewModel,
                onNavigateBack = { navController.popBackStack() },
                onTranscribeComplete = { newMeetingId ->
                    navController.navigate(Screen.MeetingDetail.createRoute(newMeetingId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.MeetingDetail.route,
            arguments = listOf(
                navArgument("meetingId") { type = NavType.LongType },
                navArgument("initialSeekTimestamp") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val meetingId = backStackEntry.arguments?.getLong("meetingId") ?: 1L
            val initialSeekTimestamp: Int = backStackEntry.arguments?.getInt("initialSeekTimestamp") ?: -1

            MeetingDetailScreen(
                meetingId = meetingId,
                initialSeekTimestamp = initialSeekTimestamp,
                viewModel = meetingViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
