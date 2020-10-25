fun Stemmer.stemWord(word: String): String {
    add(word.toLowerCase().toCharArray(), word.length)
    stem()
    return toString()
}