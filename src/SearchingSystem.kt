import java.io.File
import java.lang.StringBuilder

class SearchingSystem(
    rawDocuments: List<Document>
) {

    private val stemmer = Stemmer()

    private val processedDocuments: List<ProcessedDocument>

    private val englishStopWords: Set<String>

    private val wordRegex = Regex("(?<=\\W|^)\\w*(?=\\W|$)")

    init {
        englishStopWords = readStopWords()
        processedDocuments = rawDocuments.map(::mapToProcessed)
    }

    private fun mapToProcessed(document: Document): ProcessedDocument {
        with(document) {
            val cleanWordsList = wordRegex.findAll(content)
                .filter { it.value.isNotEmpty() }
                .mapNotNull { matchResult ->
                    matchResult.groups[0]?.run {
                        DocumentWord(
                            word = value.toLowerCase(),
                            documentStartIndex = matchResult.range.first,
                            documentEndIndex = matchResult.range.last
                        )
                    }
                }
                .filter { !englishStopWords.contains(it.word) }
                .toList()

            val stemmedWordsMap = mutableMapOf<String, MutableList<DocumentWord>>()
            cleanWordsList.forEach {
                val stemmedWord = stemmer.stemWord(it.word)
                if (stemmedWordsMap.containsKey(stemmedWord)) {
                    stemmedWordsMap[stemmedWord]!!.add(it)
                } else stemmedWordsMap[stemmedWord] = mutableListOf(it)
            }

            return ProcessedDocument(
                name = document.name,
                content = document.content,
                cleanWordsList = cleanWordsList,
                stemmedWords = stemmedWordsMap
            )
        }

    }

    fun searchByQuery(query: String): List<DocumentSearchResponse> {
        val queryWords = wordRegex.findAll(query)
            .mapNotNull { it.groups[0]?.value }
            .filter { it.isNotEmpty() && !englishStopWords.contains(it) }
            .map { QueryWord(it, stemmer.stemWord(it.toLowerCase())) }
            .toList()

        return processedDocuments.map { document ->
            val queryWordMatches = mutableListOf<Pair<String, List<DocumentWord>>>()
            queryWords.forEach { (originalWord, stemmedWord) ->
                val matchedWords = document.stemmedWords[stemmedWord] ?: listOf()
                queryWordMatches.add(originalWord to matchedWords)
            }

            val highlightedContentBuilder = StringBuilder(document.content)

            queryWordMatches.forEach { (_, matchedWords) ->
                matchedWords.forEach { word ->
                    highlightedContentBuilder.replace(
                        word.documentStartIndex,
                        word.documentEndIndex + 1,
                        highlightedContentBuilder.substring(word.documentStartIndex, word.documentEndIndex + 1)
                            .toUpperCase()
                    )
                }
            }

            return@map DocumentSearchResponse(
                resultDocument = Document(name = document.name, content = highlightedContentBuilder.toString()),
                queryMatch = queryWordMatches.sortedByDescending { it.second.size },
                matchesCount = queryWordMatches.sumBy { it.second.size }
            )
        }
            .sortedByDescending { it.matchesCount }
    }

    private fun readStopWords(): Set<String> {
        return File("english_stopwords")
            .readLines()
            .filter { it.isNotEmpty() }
            .toSet()
    }
}

data class QueryWord(val word: String, val stemmedWord: String)

class DocumentSearchResponse(
    val resultDocument: Document,
    val queryMatch: List<Pair<String, List<DocumentWord>>>,
    val matchesCount: Int
)

data class DocumentWord(val word: String, val documentStartIndex: Int, val documentEndIndex: Int) {
    override fun hashCode(): Int = word.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentWord

        if (word != other.word) return false
        if (documentStartIndex != other.documentStartIndex) return false
        if (documentEndIndex != other.documentEndIndex) return false

        return true
    }
}

data class ProcessedDocument(
    val name: String,
    val content: String,
    val cleanWordsList: List<DocumentWord>,
    val stemmedWords: Map<String, List<DocumentWord>>
)