package extension

/**
 * Ha lista következő eleme létezik és nem üres vagy whitespace, akkor 'true'-t ad vissza.
 */
fun List<String>.nextIsNotBlank(currentIndex: Int): Boolean {
     return !(this.getOrNull(currentIndex + 1) == null || this[currentIndex + 1].isBlank())
}
