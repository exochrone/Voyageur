package com.jb.voyageur.core.ui.util

import com.jb.voyageur.core.domain.model.Sexe
import com.jb.voyageur.core.domain.model.TableFourchettePhysique

object FormatPhysique {

    /**
     * 175 → "1,75 m"
     * 162 → "1,62 m"
     */
    fun formatTailleCm(cm: Int): String {
        val metres  = cm / 100
        val centimes = cm % 100
        return "$metres,${centimes.toString().padStart(2, '0')} m"
    }

    /**
     * 75 → "75 kg"
     */
    fun formatPoids(kg: Int): String = "$kg kg"

    /**
     * Fourchette pour affichage indicatif dans le champ
     * Exemple : "(140–155 cm)"
     */
    fun formatFourchetteTaille(taille: Int, sexe: Sexe): String {
        val f = TableFourchettePhysique.pour(taille).tailleCm(sexe)
        return "(${f.min}–${f.max} cm)"
    }

    fun formatFourchettePoids(taille: Int): String {
        val f = TableFourchettePhysique.pour(taille).poids
        return "(${f.min}–${f.max} kg)"
    }
}
