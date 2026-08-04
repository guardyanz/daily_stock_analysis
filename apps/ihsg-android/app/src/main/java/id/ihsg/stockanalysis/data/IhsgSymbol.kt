package id.ihsg.stockanalysis.data

object IhsgSymbol {
    private val symbolPattern = Regex("^[A-Z0-9]{2,8}\\.JK$")

    fun normalize(raw: String): String {
        val compact = raw.trim().uppercase().replace(" ", "")
        return when {
            compact.isBlank() -> ""
            compact.endsWith(".JK") -> compact
            compact.matches(Regex("^[A-Z0-9]{2,8}$")) -> "$compact.JK"
            else -> compact
        }
    }

    fun isValid(symbol: String): Boolean = symbolPattern.matches(symbol)
}
