package com.chronoplan.domain.usecase

import com.chronoplan.R
import com.chronoplan.data.model.AchievementDto
import com.chronoplan.data.model.UserStatsDto
import java.text.SimpleDateFormat
import java.util.*

class AchievementUseCase {

    // Daftar semua achievement
    fun getAllAchievements(): List<AchievementDto> = listOf(
        AchievementDto(
            id = "first_agenda",
            name = "Langkah Pertama",
            description = "Buat agenda pertama kamu",
            iconRes = R.drawable.ic_star,
            requiredCount = 1,
            category = "agenda"
        ),
        AchievementDto(
            id = "agenda_master",
            name = "Master Agenda",
            description = "Selesaikan 10 agenda",
            iconRes = R.drawable.ic_work,
            requiredCount = 10,
            category = "agenda"
        ),
        AchievementDto(
            id = "note_taker",
            name = "Pencatat Ulung",
            description = "Buat 5 catatan",
            iconRes = R.drawable.ic_note,
            requiredCount = 5,
            category = "note"
        ),
        AchievementDto(
            id = "week_streak",
            name = "Konsisten!",
            description = "Login 7 hari berturut-turut",
            iconRes = R.drawable.ic_calendar,
            requiredCount = 7,
            category = "streak"
        ),
        AchievementDto(
            id = "productive",
            name = "Sangat Produktif",
            description = "Selesaikan 50 agenda",
            iconRes = R.drawable.ic_star,
            requiredCount = 50,
            category = "agenda"
        ),
        AchievementDto(
            id = "organized",
            name = "Terorganisir",
            description = "Buat 20 catatan",
            iconRes = R.drawable.ic_note,
            requiredCount = 20,
            category = "note"
        )
    )

    // Check achievement berdasarkan stats
    fun checkAchievements(stats: UserStatsDto): List<AchievementDto> {
        val achievements = getAllAchievements()

        return achievements.map { achievement ->
            val currentCount = when (achievement.category) {
                "agenda" -> stats.completedAgendas
                "note" -> stats.totalNotes
                "streak" -> stats.currentStreak
                else -> 0
            }

            achievement.copy(
                currentCount = currentCount,
                isUnlocked = currentCount >= achievement.requiredCount
            )
        }
    }

    // Calculate level dari experience
    fun calculateLevel(experience: Int): Int {
        return (experience / 100) + 1
    }

    // Update streak
    fun updateStreak(lastActiveDate: String, currentStreak: Int): Pair<Int, Int> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        if (lastActiveDate.isEmpty()) {
            return Pair(1, 1) // new streak, longest = 1
        }

        val lastDate = dateFormat.parse(lastActiveDate)
        val todayDate = dateFormat.parse(today)

        val diffInMillis = todayDate!!.time - lastDate!!.time
        val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

        return when (diffInDays) {
            0 -> Pair(currentStreak, currentStreak) // Same day
            1 -> Pair(currentStreak + 1, maxOf(currentStreak + 1, currentStreak)) // Next day
            else -> Pair(1, currentStreak) // Streak broken
        }
    }
}