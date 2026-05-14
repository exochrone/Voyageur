package com.jb.voyageur.core.ui.navigation

enum class EcranCreation {
    CARACTERISTIQUES,
    COMPETENCES,
    SORTS,
    EQUIPEMENT,
    ARCHETYPE
}

object SequenceEcrans {
    fun pour(afficherSorts: Boolean): List<EcranCreation> = buildList {
        add(EcranCreation.CARACTERISTIQUES)
        add(EcranCreation.COMPETENCES)
        if (afficherSorts) add(EcranCreation.SORTS)
        add(EcranCreation.EQUIPEMENT)
        add(EcranCreation.ARCHETYPE)
    }

    fun precedent(ecran: EcranCreation, afficherSorts: Boolean): EcranCreation? {
        val sequence = pour(afficherSorts)
        val index    = sequence.indexOf(ecran)
        if (index == -1) return null
        return if (index > 0) sequence[index - 1] else sequence.last()
    }

    fun suivant(ecran: EcranCreation, afficherSorts: Boolean): EcranCreation? {
        val sequence = pour(afficherSorts)
        val index    = sequence.indexOf(ecran)
        if (index == -1) return null
        return if (index < sequence.size - 1) sequence[index + 1] else sequence.first()
    }
}
