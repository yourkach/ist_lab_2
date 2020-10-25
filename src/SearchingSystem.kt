import java.io.File

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
                            documentStartIndex = matchResult.range.first
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
            .mapNotNull { matchResult ->
                matchResult.groups[0]?.run {
                    value.toLowerCase()
                }
            }
            .filter { it.isNotEmpty() }
            .toList()

        // TODO: 25.10.2020 запихать в один лист с парами <word, stemmed>
        val cleanQueryWords = queryWords.filter { !englishStopWords.contains(it) }

        val stemmedQueryWords = cleanQueryWords.map { stemmer.stemWord(it) }

        return processedDocuments.map { document ->

            val queryWordMatches = mutableListOf<Pair<String, DocumentWord>>()

            stemmedQueryWords.forEach { stemmedQueryWord ->
                document.stemmedWords[stemmedQueryWord]?.forEach { documentMatchedWord ->
                    queryWordMatches.add(stemmedQueryWord to documentMatchedWord)
                }
            }

            return@map DocumentSearchResponse(
                documentName = document.name,
                queryMatch = queryWordMatches
            )
        }
    }

    private fun readStopWords(): Set<String> {
        return File("english_stopwords")
            .readLines()
            .filter { it.isNotEmpty() }
            .toSet()
    }


}

class DocumentSearchResponse(val documentName: String, val queryMatch: List<Pair<String, DocumentWord>>)

data class DocumentWord(val word: String, val documentStartIndex: Int) {

    override fun hashCode(): Int = word.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentWord

        if (word != other.word) return false
        if (documentStartIndex != other.documentStartIndex) return false

        return true
    }
}

data class StemmedWord(val value: String, val original: DocumentWord) {

    override fun hashCode(): Int = value.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StemmedWord

        if (value != other.value) return false
        if (original != other.original) return false

        return true
    }
}

data class ProcessedDocument(
    val name: String,
    val content: String,
    val cleanWordsList: List<DocumentWord>,
    val stemmedWords: Map<String, List<DocumentWord>>
)