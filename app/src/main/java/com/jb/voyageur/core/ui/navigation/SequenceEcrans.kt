package com.jb.voyageur.core.ui.navigation

enum class EcranCreation {
    CARACTERISTIQUES,
    COMPETENCES,
    SORTS,
    EQUIPEMENT,
    ARCHETYPE
}

object SequenceEcrans {
    fun pour(hautRevant: Boolean): List<EcranCreation> = buildList {
        add(EcranCreation.CARACTERISTIQUES)
        add(EcranCreation.COMPETENCES)
        if (hautRevant) add(EcranCreation.SORTS)
        add(EcranCreation.EQUIPEMENT)
        add(EcranCreation.ARCHETYPE)
    }

    fun precedent(ecran: EcranCreation, hautRevant: Boolean): EcranCreation? {
        val sequence = pour(hautRevant)
        val index    = sequence.indexOf(ecran)
        return if (index > 0) sequence[index - 1] else null
    }

    fun suivant(ecran: EcranCreation, hautRevant: Boolean): EcranCreation? {
        val sequence = pour(hautRevant)
        val index    = sequence.indexOf(ecran)
        return if (index != -1 && index < sequence.size - 1) sequence[index + 1] else null
    }
}
