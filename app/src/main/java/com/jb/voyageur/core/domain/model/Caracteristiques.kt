package com.jb.voyageur.core.domain.model

import kotlin.math.ceil

data class Caracteristiques(
    val taille: Int = 10,
    val apparence: Int = 10,
    val constitution: Int = 10,
    val force: Int = 10,
    val agilite: Int = 10,
    val dexterite: Int = 10,
    val vue: Int = 10,
    val ouie: Int = 10,
    val odoGout: Int = 10,
    val volonte: Int = 10,
    val intellect: Int = 10,
    val empathie: Int = 10,
    val reve: Int = 10,
    val chance: Int = 10
)

val Caracteristiques.melee: Int get() = (force + agilite) / 2
val Caracteristiques.tir: Int get() = (vue + dexterite) / 2
val Caracteristiques.lancer: Int get() = (tir + force) / 2
val Caracteristiques.derobee: Int get() = (agilite + (21 - taille)) / 2
val Caracteristiques.pointsDeVie: Int get() = ceil((taille + constitution) / 2.0).toInt()
val Caracteristiques.endurance: Int get() = maxOf(taille + constitution, pointsDeVie + volonte)
val Caracteristiques.seuilConstitution: Int get() = when (constitution) {
    in 6..8 -> 2; in 9..11 -> 3; in 12..14 -> 4; else -> 5
}
val Caracteristiques.sustentation: Int get() = when (taille) {
    in 6..9 -> 2; in 10..13 -> 3; else -> 4
}
val Caracteristiques.bonusDommages: Int get() = when ((taille + force) / 2) {
    in 6..7 -> -1; in 8..11 -> 0; in 12..13 -> 1; else -> 2
}
val Caracteristiques.encombrement: Float get() = (taille + force) / 2f
val Caracteristiques.sust: Int get() = sustentation
val Caracteristiques.sc: Int get() = seuilConstitution
val Caracteristiques.bonusDom: Int get() = bonusDommages

val Caracteristiques.pointsTotal: Int get() =
    taille + apparence + constitution + force + agilite + dexterite +
    vue + ouie + odoGout + volonte + intellect + empathie + reve + chance
