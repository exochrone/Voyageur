package com.jb.voyageur.core.domain.model

data class Sort(
    val nom: String,
    val difficulte: Int?, // Le X dans R-X, null pour "variable"
    val voie: VoieDraconic,
    val description: String // Le libellé complet tel qu'affiché
) {
    val estAnnulationDeMagie: Boolean get() = nom.uppercase().contains("ANNULATION DE MAGIE")

    val nomPur: String get() {
        val firstParen = description.indexOf('(')
        val firstR = description.indexOf(" R-")
        val firstVar = description.indexOf(" variable")
        
        var end = description.length
        if (firstParen != -1) end = minOf(end, firstParen)
        if (firstR != -1) end = minOf(end, firstR)
        if (firstVar != -1) end = minOf(end, firstVar)
        
        return description.substring(0, end).trim()
    }

    val tmr: String? get() {
        val start = description.indexOf('(')
        val end = description.indexOf(')')
        if (start != -1 && end != -1 && end > start) {
            val content = description.substring(start + 1, end)
            if (content.startsWith("r") && content.length > 1 && content[1].isDigit()) return null
            return content.trim()
        }
        return null
    }

    val diffFull: String get() {
        val firstR = description.indexOf(" R-")
        val firstVar = description.indexOf(" variable")
        val start = if (firstR != -1) firstR else if (firstVar != -1) firstVar else -1
        
        if (start != -1) {
            return description.substring(start).trim()
        }
        return if (difficulte != null) "R-$difficulte" else "variable"
    }

    fun calculerCoutDeBase(): Int {
        if (estAnnulationDeMagie) return 70
        return (difficulte ?: 0) * 10
    }

    fun calculerSupplement(niveauDraconic: Int): Int {
        val diff = difficulte ?: 0
        val seuilNormal = niveauDraconic + 3
        if (diff <= seuilNormal) return 0
        val niveauxEnTrop = diff - seuilNormal
        return niveauxEnTrop * 20
    }

    fun estAccessible(niveauDraconic: Int): Boolean {
        val diff = difficulte ?: 0
        return diff <= niveauDraconic + 5
    }
}
