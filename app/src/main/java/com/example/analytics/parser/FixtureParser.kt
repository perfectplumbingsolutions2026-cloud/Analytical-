package com.example.analytics.parser

import com.example.data.models.CompetitionType
import com.example.data.models.MatchFixture
import java.util.UUID

object FixtureParser {

    /**
     * Parses raw plain text lines containing fixtures into validated MatchFixture objects.
     * Example lines:
     * 20:00 Team A vs Team B - China FA Cup
     * 18:30 Team C vs Team D - Sweden Allsvenskan
     * 21:15 Team E vs Team F - Argentina Primera Nacional
     */
    fun parseBatch(rawInput: String): List<MatchFixture> {
        if (rawInput.isBlank()) return emptyList()

        val lines = rawInput.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }

        val parsedList = mutableListOf<MatchFixture>()
        val seenKeys = mutableSetOf<String>()

        for (line in lines) {
            val fixture = parseSingleLine(line) ?: continue
            val key = "${fixture.homeTeam.lowercase()}_vs_${fixture.awayTeam.lowercase()}"
            if (!seenKeys.contains(key)) {
                seenKeys.add(key)
                parsedList.add(fixture)
            }
        }

        return parsedList
    }

    private fun parseSingleLine(line: String): MatchFixture? {
        val cleanLine = line
            .replace("\\s+".toRegex(), " ")
            .replace(" v ", " vs ", ignoreCase = true)
            .replace(" - ", " | ")
            .replace(" – ", " | ")

        // Extract time if present (e.g., 20:00 or 8:00pm or 19.30)
        val timeRegex = """^(\d{1,2}[:.]\d{2}\s*(?:am|pm)?|\d{1,2}\s*(?:am|pm))\s+""".toRegex(RegexOption.IGNORE_CASE)
        val timeMatch = timeRegex.find(cleanLine)
        val timeStr = timeMatch?.groupValues?.get(1) ?: "20:00"

        val lineWithoutTime = if (timeMatch != null) {
            cleanLine.substring(timeMatch.range.last + 1).trim()
        } else {
            cleanLine
        }

        // Split by '|' or '-' for competition
        val parts = lineWithoutTime.split("|").map { it.trim() }
        val teamsPart = parts.getOrNull(0) ?: return null
        val compPart = parts.getOrNull(1) ?: detectCompetitionFromTeams(teamsPart)

        // Split Teams part by " vs "
        val teamSplit = teamsPart.split(" vs ", ignoreCase = true)
        if (teamSplit.size < 2) return null

        val rawHome = cleanTeamName(teamSplit[0])
        val rawAway = cleanTeamName(teamSplit[1])

        if (rawHome.isBlank() || rawAway.isBlank()) return null

        val (country, league, compType) = classifyCompetition(compPart)

        return MatchFixture(
            id = UUID.randomUUID().toString(),
            timeStr = timeStr,
            homeTeam = rawHome,
            awayTeam = rawAway,
            competition = compPart.ifBlank { "$country $league" },
            country = country,
            league = league,
            competitionType = compType,
            isRivalry = isDerbyMatch(rawHome, rawAway)
        )
    }

    private fun cleanTeamName(name: String): String {
        return name.trim()
            .replace("^\\d+\\.\\s*".toRegex(), "") // remove leading numbers like "1. Arsenal"
            .replace("\\s*\\(H\\)".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("\\s*\\(A\\)".toRegex(RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun classifyCompetition(compString: String): Triple<String, String, CompetitionType> {
        val lower = compString.lowercase()

        // Detect Competition Type
        val compType = when {
            lower.contains("cup") || lower.contains("trophy") || lower.contains("pokal") || lower.contains("copa") -> CompetitionType.CUP
            lower.contains("champions league") || lower.contains("europa") || lower.contains("libertadores") || lower.contains("caf") -> CompetitionType.CONTINENTAL
            lower.contains("u21") || lower.contains("u19") || lower.contains("youth") || lower.contains("sub-20") -> CompetitionType.YOUTH
            lower.contains("women") || lower.contains("wsl") || lower.contains("damallsvenskan") || lower.contains("fem") -> CompetitionType.WOMEN
            lower.contains("reserve") || lower.contains("ii") || lower.contains("b team") -> CompetitionType.RESERVE
            lower.contains("friendly") || lower.contains("club friendlies") -> CompetitionType.FRIENDLY
            else -> CompetitionType.LEAGUE
        }

        // Detect Country & League
        var country = "Global"
        var league = compString

        when {
            lower.contains("premier league") || lower.contains("england") || lower.contains("championship") -> {
                country = "England"
                league = if (lower.contains("championship")) "EFL Championship" else "Premier League"
            }
            lower.contains("la liga") || lower.contains("spain") || lower.contains("segunda") -> {
                country = "Spain"
                league = if (lower.contains("segunda")) "La Liga 2" else "La Liga"
            }
            lower.contains("serie a") || lower.contains("italy") -> {
                country = "Italy"
                league = "Serie A"
            }
            lower.contains("bundesliga") || lower.contains("germany") -> {
                country = "Germany"
                league = "Bundesliga"
            }
            lower.contains("ligue 1") || lower.contains("france") -> {
                country = "France"
                league = "Ligue 1"
            }
            lower.contains("allsvenskan") || lower.contains("sweden") -> {
                country = "Sweden"
                league = "Allsvenskan"
            }
            lower.contains("china") || lower.contains("fa cup") -> {
                country = "China"
                league = if (lower.contains("fa cup")) "China FA Cup" else "Super League"
            }
            lower.contains("argentina") || lower.contains("primera nacional") -> {
                country = "Argentina"
                league = "Primera Nacional"
            }
            lower.contains("brazil") || lower.contains("brasileirao") || lower.contains("serie b") -> {
                country = "Brazil"
                league = "Brasileirao"
            }
            lower.contains("japan") || lower.contains("j1 league") || lower.contains("j-league") -> {
                country = "Japan"
                league = "J1 League"
            }
            lower.contains("mls") || lower.contains("usa") -> {
                country = "USA"
                league = "Major League Soccer"
            }
        }

        return Triple(country, league, compType)
    }

    private fun detectCompetitionFromTeams(teamsPart: String): String {
        val lower = teamsPart.lowercase()
        return when {
            lower.contains("arsenal") || lower.contains("chelsea") || lower.contains("liverpool") -> "England Premier League"
            lower.contains("real madrid") || lower.contains("barcelona") -> "Spain La Liga"
            lower.contains("bayern") || lower.contains("dortmund") -> "Germany Bundesliga"
            lower.contains("inter") || lower.contains("juventus") || lower.contains("milan") -> "Italy Serie A"
            else -> "Global Football Competition"
        }
    }

    private fun isDerbyMatch(home: String, away: String): Boolean {
        val combined = "$home vs $away".lowercase()
        return combined.contains("real madrid") && combined.contains("barcelona") ||
                combined.contains("arsenal") && combined.contains("tottenham") ||
                combined.contains("inter") && combined.contains("milan") ||
                combined.contains("celtic") && combined.contains("rangers") ||
                combined.contains("boca") && combined.contains("river")
    }

    fun getSampleFixtures(): String {
        return """
            20:00 Arsenal vs Chelsea - England Premier League
            18:30 Malmö FF vs AIK Stockholm - Sweden Allsvenskan
            21:15 Shanghai Shenhua vs Beijing Guoan - China FA Cup
            19:45 River Plate vs Boca Juniors - Argentina Primera Nacional
            20:45 AC Milan vs Inter Milan - Italy Serie A
            17:00 Urawa Red Diamonds vs Yokohama F. Marinos - Japan J1 League
            22:00 LAFC vs LA Galaxy - USA MLS
            19:00 Real Madrid vs Barcelona - Spain La Liga
        """.trimIndent()
    }
}
