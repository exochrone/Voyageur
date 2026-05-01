package com.jb.voyageur.core.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CaracteristiquesTest {

    @Test
    fun `pointsDeVie calculation is correct (rounded up)`() {
        val caracs = Caracteristiques(taille = 10, constitution = 11)
        // (10 + 11) / 2 = 10.5 -> 11
        assertThat(caracs.pointsDeVie).isEqualTo(11)
    }

    @Test
    fun `endurance calculation is correct`() {
        val caracs = Caracteristiques(taille = 10, constitution = 11, volonte = 13)
        // pointsDeVie = 11
        // max(10+11, 11+13) = max(21, 24) = 24
        assertThat(caracs.endurance).isEqualTo(24)
    }

    @Test
    fun `seuilConstitution follows the table`() {
        assertThat(Caracteristiques(constitution = 7).seuilConstitution).isEqualTo(2)
        assertThat(Caracteristiques(constitution = 10).seuilConstitution).isEqualTo(3)
        assertThat(Caracteristiques(constitution = 13).seuilConstitution).isEqualTo(4)
        assertThat(Caracteristiques(constitution = 15).seuilConstitution).isEqualTo(5)
    }

    @Test
    fun `bonusDommages follows the table`() {
        // (Taille + Force) / 2
        assertThat(Caracteristiques(taille = 6, force = 7).bonusDommages).isEqualTo(-1) // moy 6
        assertThat(Caracteristiques(taille = 10, force = 10).bonusDommages).isEqualTo(0) // moy 10
        assertThat(Caracteristiques(taille = 12, force = 13).bonusDommages).isEqualTo(1) // moy 12
        assertThat(Caracteristiques(taille = 15, force = 15).bonusDommages).isEqualTo(2) // moy 15
    }

    @Test
    fun `derived combat stats are correct`() {
        val caracs = Caracteristiques(force = 10, agilite = 12, vue = 8, dexterite = 14, taille = 10)
        assertThat(caracs.melee).isEqualTo(11) // (10+12)/2
        assertThat(caracs.tir).isEqualTo(11) // (8+14)/2
        assertThat(caracs.lancer).isEqualTo(10) // (11+10)/2 = 10.5 -> 10
        assertThat(caracs.derobee).isEqualTo(11) // (12 + (21-10)) / 2 = (12+11)/2 = 11.5 -> 11
    }
}
