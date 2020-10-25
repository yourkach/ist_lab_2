fun main() {
    val directoryPath = "/home/yury/ist_docs"
    println("Documents directory is: $directoryPath")
    val documentsRepo = DocumentsRepo(directoryPath = directoryPath)
    val searchingSystem = SearchingSystem(documentsRepo.documents)

    while (true) {
        val response = searchingSystem.searchByQuery(readSearchQuery())
        response.forEach { searchResponse ->
            val matchedWordsString =
                searchResponse.queryMatch.joinToString(transform = { "${it.first} - ${it.second}" }, separator = "\n")
            println(
                "${searchResponse.documentName}: \n$matchedWordsString"
            )
        }
    }
}

private fun readSearchQuery(): String {
    print("Enter search query: ")
    var searchQuery: String? = null
    while (true) {
        searchQuery = readLine()?.trim()
        if (!searchQuery.isNullOrEmpty()) return searchQuery
        println("Query is empty, please enter query again")
    }
}