package com.lmen918.pda.ui.navigation

sealed class Screen(val route: String) {
    object Timeline : Screen("timeline")
    object AddEditEvent : Screen("add_edit_event?eventId={eventId}") {
        fun createRoute(eventId: Long? = null) =
            if (eventId != null) "add_edit_event?eventId=$eventId" else "add_edit_event"
    }
    object Tags : Screen("tags")
    object Retrospective : Screen("retrospective")
}
