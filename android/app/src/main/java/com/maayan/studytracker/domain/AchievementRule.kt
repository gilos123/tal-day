package com.maayan.studytracker.domain

import com.maayan.studytracker.data.db.entities.TimerSessionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * A snapshot of the user's lifetime stats at a point in time — passed into each
 * [AchievementRule] for evaluation. Computed once per completed session so we don't
 * run the same aggregation ten times.
 */
data class StatsSnapshot(
    val totalSessions: Int,
    val totalMinutes: Long,
    val streakDays: Int,
    /** Actual durations of every session in seconds; used by "single-session" rules. */
    val sessionDurationsSeconds: List<Long>,
    /** For the "Variety 5" rule: distinct topic names per local date. */
    val topicsStudiedByDate: Map<LocalDate, Set<String>>,
    /** Completion wall-clock hours (0–23) per session, for Night Owl / Early Bird. */
    val sessionHours: List<Int>,
    /** Total minutes studied within each ISO week start date. */
    val minutesByIsoWeekStart: Map<LocalDate, Long>,
    /** The session that just completed (the one being evaluated). */
    val latestSession: TimerSessionEntity?
)

/** Definition of a single achievement. Evaluation is pure — no IO. */
data class AchievementRule(
    val code: String,
    val icon: String,      // emoji
    val title: String,
    val description: String,
    val evaluate: (StatsSnapshot) -> Boolean
)

object AchievementCatalogue {

    val all: List<AchievementRule> = listOf(
        AchievementRule(
            code = "FIRST_TIMER",
            icon = "🎯",
            title = "First session",
            description = "Complete your first timer."
        ) { snap -> snap.totalSessions >= 1 },

        AchievementRule(
            code = "STREAK_7",
            icon = "🔥",
            title = "Week warrior",
            description = "Study 7 days in a row."
        ) { snap -> snap.streakDays >= 7 },

        AchievementRule(
            code = "STREAK_30",
            icon = "🏆",
            title = "Unstoppable",
            description = "Study 30 days in a row."
        ) { snap -> snap.streakDays >= 30 },

        AchievementRule(
            code = "MARATHON_1H",
            icon = "🐎",
            title = "Marathon",
            description = "Complete a single 60-minute session."
        ) { snap -> snap.sessionDurationsSeconds.any { it >= 60L * 60L } },

        AchievementRule(
            code = "TEN_SESSIONS",
            icon = "🎓",
            title = "Ten down",
            description = "Complete 10 timer sessions."
        ) { snap -> snap.totalSessions >= 10 },

        AchievementRule(
            code = "HUNDRED_SESSIONS",
            icon = "💯",
            title = "Centurion",
            description = "Complete 100 timer sessions."
        ) { snap -> snap.totalSessions >= 100 },

        AchievementRule(
            code = "VARIETY_5",
            icon = "🎨",
            title = "Polymath",
            description = "Study 5 different topics in a single day."
        ) { snap -> snap.topicsStudiedByDate.values.any { it.size >= 5 } },

        AchievementRule(
            code = "NIGHT_OWL",
            icon = "🦉",
            title = "Night owl",
            description = "Finish a session after 23:00."
        ) { snap -> snap.sessionHours.any { it >= 23 } },

        AchievementRule(
            code = "EARLY_BIRD",
            icon = "🌅",
            title = "Early bird",
            description = "Finish a session before 07:00."
        ) { snap -> snap.sessionHours.any { it < 7 } },

        AchievementRule(
            code = "WEEK_TEN_HOURS",
            icon = "⚡",
            title = "Full focus",
            description = "Study 10+ hours in a single week."
        ) { snap -> snap.minutesByIsoWeekStart.values.any { it >= 10L * 60L } }
    )
}

/** Compute a [StatsSnapshot] from the raw session list. Pure. */
fun buildSnapshot(
    sessions: List<TimerSessionEntity>,
    streakDays: Int,
    today: LocalDate = LocalDate.now(),
    zone: ZoneId = ZoneId.systemDefault()
): StatsSnapshot {
    val totalMinutes = sessions.sumOf { it.actualDurationSeconds } / 60L

    val topicsByDate: Map<LocalDate, Set<String>> = sessions
        .mapNotNull { sess ->
            val parsed = runCatching { LocalDate.parse(sess.date) }.getOrNull() ?: return@mapNotNull null
            parsed to sess.topicName
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, names) -> names.filter { it.isNotBlank() }.toSet() }

    val hours: List<Int> = sessions.map { sess ->
        Instant.ofEpochMilli(sess.completedAt).atZone(zone).hour
    }

    val weekFields = WeekFields.of(Locale.getDefault())
    val minutesByWeek: Map<LocalDate, Long> = sessions
        .mapNotNull { sess ->
            val parsed = runCatching { LocalDate.parse(sess.date) }.getOrNull() ?: return@mapNotNull null
            val weekStart = parsed.with(weekFields.dayOfWeek(), 1L)
            weekStart to sess.actualDurationSeconds
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, secs) -> secs.sum() / 60L }

    return StatsSnapshot(
        totalSessions = sessions.size,
        totalMinutes = totalMinutes,
        streakDays = streakDays,
        sessionDurationsSeconds = sessions.map { it.actualDurationSeconds },
        topicsStudiedByDate = topicsByDate,
        sessionHours = hours,
        minutesByIsoWeekStart = minutesByWeek,
        latestSession = sessions.maxByOrNull { it.completedAt }
    )
}
