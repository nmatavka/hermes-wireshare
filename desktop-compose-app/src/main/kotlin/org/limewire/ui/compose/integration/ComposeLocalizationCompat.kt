package org.limewire.ui.compose.integration

import org.limewire.collection.AutoCompleteDictionary
import org.limewire.core.api.FilePropertyKey
import org.limewire.core.api.library.FriendAutoCompleterFactory
import org.limewire.core.api.search.SearchCategory
import org.limewire.ui.compose.AdvancedSearchSuggestionEntry
import org.limewire.ui.compose.SearchSuggestionEntry
import org.limewire.ui.compose.SearchSuggestionSource
import org.limewire.ui.compose.tr
import java.util.LinkedHashMap
import java.util.Locale

class CoreComposeSearchSuggestionsService(
    private val searchHistory: AutoCompleteDictionary? = null
) : ComposeSearchSuggestionsService {
    override fun suggestions(
        input: String,
        category: SearchCategory,
        includeHistory: Boolean,
        includeSmartSuggestions: Boolean
    ): List<SearchSuggestionEntry> {
        val normalizedInput = input.trim()
        val suggestions = mutableListOf<SearchSuggestionEntry>()
        val seen = linkedSetOf<String>()

        if (includeHistory) {
            val historyEntries = searchHistory?.getPrefixedBy(normalizedInput).orEmpty()
            historyEntries
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .forEach { entry ->
                    val key = entry.lowercase(Locale.US)
                    if (seen.add(key)) {
                        suggestions += SearchSuggestionEntry(
                            source = SearchSuggestionSource.HISTORY,
                            title = entry,
                            queryText = entry,
                            category = category
                        )
                    }
                }
        }

        if (includeSmartSuggestions) {
            smartSuggestions(normalizedInput, category).forEach { entry ->
                val key = entry.queryText.lowercase(Locale.US)
                if (seen.add(key)) {
                    suggestions += entry
                }
            }
        }

        return suggestions.take(10)
    }

    private fun smartSuggestions(input: String, category: SearchCategory): List<SearchSuggestionEntry> {
        if (input.isBlank() || input.contains(':')) {
            return emptyList()
        }

        return when (category) {
            SearchCategory.AUDIO -> createAudioSmartSuggestions(input)
            SearchCategory.VIDEO -> createVideoSmartSuggestions(input)
            else -> emptyList()
        }
    }

    private fun createAudioSmartSuggestions(input: String): List<SearchSuggestionEntry> {
        val fields = input.split('-')
        return when {
            fields.size == 1 || (fields.size > 1 && fields[1].trim().isEmpty()) -> listOfNotNull(
                smartSuggestion(SearchCategory.AUDIO, FilePropertyKey.AUTHOR to fields[0].trim()),
                smartSuggestion(SearchCategory.AUDIO, FilePropertyKey.TITLE to fields[0].trim()),
                smartSuggestion(SearchCategory.AUDIO, FilePropertyKey.ALBUM to fields[0].trim())
            )

            fields.size == 2 || (fields.size > 2 && fields[2].trim().isEmpty()) -> listOfNotNull(
                smartSuggestion(
                    SearchCategory.AUDIO,
                    FilePropertyKey.AUTHOR to fields[0].trim(),
                    FilePropertyKey.TITLE to fields[1].trim()
                )
            )

            else -> listOfNotNull(
                smartSuggestion(
                    SearchCategory.AUDIO,
                    FilePropertyKey.AUTHOR to fields[0].trim(),
                    FilePropertyKey.TITLE to fields[1].trim(),
                    FilePropertyKey.ALBUM to fields[2].trim()
                ),
                smartSuggestion(
                    SearchCategory.AUDIO,
                    FilePropertyKey.AUTHOR to fields[0].trim(),
                    FilePropertyKey.ALBUM to fields[1].trim(),
                    FilePropertyKey.TITLE to fields[2].trim()
                )
            )
        }
    }

    private fun createVideoSmartSuggestions(input: String): List<SearchSuggestionEntry> {
        val fields = input.split('-')
        return when {
            fields.size == 1 -> {
                val field = fields[0].trim()
                if (field.matches(Regex("\\d+"))) {
                    listOfNotNull(
                        smartSuggestion(SearchCategory.VIDEO, FilePropertyKey.YEAR to field),
                        smartSuggestion(SearchCategory.VIDEO, FilePropertyKey.TITLE to field)
                    )
                } else {
                    emptyList()
                }
            }

            fields.size > 1 -> {
                val first = fields[0].trim()
                val second = fields[1].trim()
                when {
                    second.matches(Regex("\\d+")) -> listOfNotNull(
                        smartSuggestion(
                            SearchCategory.VIDEO,
                            FilePropertyKey.TITLE to first,
                            FilePropertyKey.YEAR to second
                        )
                    )

                    first.matches(Regex("\\d+")) -> listOfNotNull(
                        smartSuggestion(
                            SearchCategory.VIDEO,
                            FilePropertyKey.YEAR to first,
                            FilePropertyKey.TITLE to second
                        )
                    )

                    else -> emptyList()
                }
            }

            else -> emptyList()
        }
    }

    private fun smartSuggestion(
        category: SearchCategory,
        vararg parts: Pair<FilePropertyKey, String>
    ): SearchSuggestionEntry? {
        val filteredParts = parts
            .mapNotNull { (key, value) ->
                value.takeIf(String::isNotBlank)?.let { key to it.trim() }
            }
        if (filteredParts.isEmpty()) {
            return null
        }

        val advancedDetails = LinkedHashMap<FilePropertyKey, String>()
        filteredParts.forEach { (key, value) -> advancedDetails[key] = value }
        val labels = filteredParts.map { (key, value) -> propertyLabel(key, category) to value }
        val title = when (labels.size) {
            1 -> tr("{0} is \"{1}\"", labels[0].first, labels[0].second)
            2 -> tr(
                "{0} \"{1}\" - {2} \"{3}\"",
                labels[0].first,
                labels[0].second,
                labels[1].first,
                labels[1].second
            )

            else -> tr(
                "{0} \"{1}\" - {2} \"{3}\" - {4} \"{5}\"",
                labels[0].first,
                labels[0].second,
                labels[1].first,
                labels[1].second,
                labels[2].first,
                labels[2].second
            )
        }
        val queryText = filteredParts.joinToString(" ") { (key, value) ->
            "${propertyLabel(key, category).lowercase(Locale.getDefault())}:$value"
        }

        return SearchSuggestionEntry(
            source = SearchSuggestionSource.SMART,
            title = title,
            queryText = queryText,
            subtitle = queryText,
            category = category,
            advancedDetails = advancedDetails
        )
    }

    private fun propertyLabel(key: FilePropertyKey, category: SearchCategory): String {
        return tr(
            when (key) {
                FilePropertyKey.TITLE -> "Title"
                FilePropertyKey.AUTHOR -> if (category == SearchCategory.AUDIO) "Artist" else "Author"
                FilePropertyKey.BITRATE -> "Bitrate"
                FilePropertyKey.DESCRIPTION -> "Description"
                FilePropertyKey.COMPANY -> "Company"
                FilePropertyKey.DATE_CREATED -> "Date"
                FilePropertyKey.FILE_SIZE -> "Size"
                FilePropertyKey.GENRE -> "Genre"
                FilePropertyKey.HEIGHT -> "Height"
                FilePropertyKey.LENGTH -> "Length"
                FilePropertyKey.NAME -> "Name"
                FilePropertyKey.PLATFORM -> "Platform"
                FilePropertyKey.QUALITY -> "Quality"
                FilePropertyKey.RATING -> "Rating"
                FilePropertyKey.TRACK_NUMBER -> "Track"
                FilePropertyKey.ALBUM -> "Album"
                FilePropertyKey.WIDTH -> "Width"
                FilePropertyKey.LOCATION -> "Location"
                FilePropertyKey.YEAR -> "Year"
                FilePropertyKey.TORRENT -> "Torrent"
                FilePropertyKey.USERAGENT -> "User Agent"
                FilePropertyKey.REFERRER -> "Referrer"
                else -> throw IllegalArgumentException("Unknown file property key: $key")
            }
        )
    }
}

class CoreComposeAdvancedSearchSuggestionsService(
    private val friendAutoCompleterFactory: FriendAutoCompleterFactory
) : ComposeAdvancedSearchSuggestionsService {
    override fun suggestions(
        category: SearchCategory,
        key: FilePropertyKey,
        input: String
    ): List<AdvancedSearchSuggestionEntry> {
        val normalizedInput = input.trim()
        if (normalizedInput.isBlank()) {
            return emptyList()
        }

        val dictionary = friendAutoCompleterFactory.getDictionary(category, key)
        return try {
            dictionary.getPrefixedBy(normalizedInput)
                .orEmpty()
                .asSequence()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .distinctBy { it.lowercase(Locale.US) }
                .take(8)
                .map(::AdvancedSearchSuggestionEntry)
                .toList()
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            emptyList()
        }
    }
}
