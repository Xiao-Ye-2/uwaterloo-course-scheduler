package logic.schedulealgo

import logic.Section
import logic.preference.Preference
import java.util.*

class OptimizedScheduleAlgorithm : ScheduleAlgorithm() {
    override fun generateSchedules(
        sectionLists: List<List<Section>>,
        preferences: List<Preference>,
        totalSchedules: Int
    ): List<List<Section>> {

        val hardPreferences = preferences.filter { it.isHard() }
        val softPreferences = preferences.filter { !it.isHard() }

        fun violatesHardPreference(sections: List<Section>): Boolean {
            return hardPreferences.any { it.eval(sections) == 0 }
        }
        fun evalSoftPreference(sections: List<Section>): Int {
            return softPreferences.sumOf { it.eval(sections) }
        }

        val heuristicOrderedSections = sectionLists.sortedBy { it.size }.map { it.toList() }
        val topSchedules = PriorityQueue<Pair<List<Section>, Int>>(compareBy { it.second })

        fun search(currentSections: List<Section>, index: Int = 0) {
            if (index >= heuristicOrderedSections.size) {
                val totalRating = softPreferences.sumOf { it.eval(currentSections) }
                topSchedules.add(Pair(currentSections, totalRating))
                if (topSchedules.size > totalSchedules) {
                    topSchedules.poll()
                }
                return
            }

            val nextSections = heuristicOrderedSections[index]
            for (section in nextSections) {
                val newSections = currentSections + section
                if (!violatesHardPreference(newSections)) {
                    if (topSchedules.size < totalSchedules ||
                        evalSoftPreference(newSections) > topSchedules.peek().second) {
                        search(newSections, index + 1)
                    }
                }
            }
        }

        search(emptyList())

        return topSchedules.toList().sortedByDescending { it.second }.map { it.first }
    }
}
