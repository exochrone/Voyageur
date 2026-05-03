package com.jb.voyageur.core.domain.model

data class Fourchette(val min: Int, val max: Int) {
    fun aleatoire(): Int = (min..max).random()
    fun clamper(valeur: Int): Int = valeur.coerceIn(min, max)
}

data class FourchettePhysique(
    val poids: Fourchette,
    val tailleCmHomme: Fourchette,
    val tailleCmFemme: Fourchette
) {
    fun tailleCm(sexe: Sexe): Fourchette = when (sexe) {
        Sexe.HOMME -> tailleCmHomme
        Sexe.FEMME -> tailleCmFemme
    }
}

object TableFourchettePhysique {

    private val TABLE = mapOf(
        6  to FourchettePhysique(Fourchette(31,  40),  Fourchette(140, 155), Fourchette(135, 150)),
        7  to FourchettePhysique(Fourchette(41,  50),  Fourchette(150, 165), Fourchette(145, 160)),
        8  to FourchettePhysique(Fourchette(51,  60),  Fourchette(160, 175), Fourchette(150, 165)),
        9  to FourchettePhysique(Fourchette(61,  65),  Fourchette(165, 180), Fourchette(155, 170)),
        10 to FourchettePhysique(Fourchette(66,  70),  Fourchette(170, 182), Fourchette(158, 172)),
        11 to FourchettePhysique(Fourchette(71,  75),  Fourchette(172, 185), Fourchette(160, 175)),
        12 to FourchettePhysique(Fourchette(76,  80),  Fourchette(175, 188), Fourchette(162, 178)),
        13 to FourchettePhysique(Fourchette(81,  90),  Fourchette(178, 192), Fourchette(165, 180)),
        14 to FourchettePhysique(Fourchette(91,  100), Fourchette(180, 195), Fourchette(168, 182)),
        15 to FourchettePhysique(Fourchette(101, 110), Fourchette(182, 200), Fourchette(170, 185))
    )

    fun pour(taille: Int): FourchettePhysique =
        TABLE[taille.coerceIn(6, 15)]!!

    fun poidsAleatoire(taille: Int): Int =
        pour(taille).poids.aleatoire()

    fun tailleCmAleatoire(taille: Int, sexe: Sexe): Int =
        pour(taille).tailleCm(sexe).aleatoire()

    fun clamperPoids(taille: Int, valeur: Int): Int =
        pour(taille).poids.clamper(valeur)

    fun clamperTailleCm(taille: Int, sexe: Sexe, valeur: Int): Int =
        pour(taille).tailleCm(sexe).clamper(valeur)
}
