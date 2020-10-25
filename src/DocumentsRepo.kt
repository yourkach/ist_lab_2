import java.io.File
import java.io.IOException

class DocumentsRepo(
    directoryPath: String,
    private val filesExtension: String = "txt"
) {

    private val mDocuments = mutableListOf<Document>()

    val documents: List<Document>
        get() = mDocuments

    init {
        val filesDirectory = File(directoryPath)
        if (filesDirectory.isDirectory) {
            filesDirectory.listFiles()
                ?.filter { file -> file != null && file.extension == filesExtension }
                ?.map { file: File -> Document(file.name, file.readText()) }
                ?.let(mDocuments::addAll)
        } else throw IOException("Incorrect documents directory path")
    }

}

data class Document(val name: String, val content: String)