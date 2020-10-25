fun main() {
    val directoryPath = "/home/yury/ist_docs"
    println("Documents directory is: $directoryPath")
    val documentsRepo = DocumentsRepo(directoryPath = directoryPath)
    val searchingSystem = SearchingSystem(documentsRepo.documents)

    while (true) {
        println()
        val response = searchingSystem.searchByQuery(readSearchQuery())
        response.forEach { searchResponse ->
            print(SUMMARY_TEXT_SEPARATOR)
            with(searchResponse) {
                println("Document \"${resultDocument.name}\" results:")
                println("Matches count: $matchesCount")
                queryMatch.forEach { (queryWord, matchesInText) ->
                    println("${matchesInText.size}\t-\t\"$queryWord\"")
                }
                if (matchesCount > 0) {
                    println(
                        "\nUppercased matches in document content:" +
                                "\n" + DOCUMENT_TEXT_SEPARATOR + "\n" + resultDocument.content + DOCUMENT_TEXT_SEPARATOR
                    )
                }
            }
        }
    }
}

private const val DOCUMENT_TEXT_SEPARATOR = "<==============Document Content==============>"
private const val SUMMARY_TEXT_SEPARATOR =
    "\n<-----------Document processing summary--------------------------------------------------------------->\n"

private fun readSearchQuery(): String {
    print("Enter search query: ")
    var searchQuery: String? = null
    while (true) {
        searchQuery = readLine()?.trim()
        if (!searchQuery.isNullOrEmpty()) return searchQuery
        println("Query is empty, please enter query again")
    }
}