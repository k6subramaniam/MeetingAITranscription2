package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object LiveRecord : Screen("live_record/{title}/{category}") {
        fun createRoute(title: String, category: String): String {
            val encTitle = java.net.URLEncoder.encode(title, "UTF-8")
            val encCat = java.net.URLEncoder.encode(category, "UTF-8")
            return "live_record/$encTitle/$encCat"
        }
    }
    object Search : Screen("search?query={query}") {
        fun createRoute(query: String = ""): String {
            val enc = try { java.net.URLEncoder.encode(query, "UTF-8") } catch (e: Exception) { query }
            return "search?query=$enc"
        }
    }
    object MeetingDetail : Screen("meeting_detail/{meetingId}?initialSeekTimestamp={initialSeekTimestamp}") {
        fun createRoute(meetingId: Long, initialSeekTimestamp: Int = -1): String =
            "meeting_detail/$meetingId?initialSeekTimestamp=$initialSeekTimestamp"
    }
}
