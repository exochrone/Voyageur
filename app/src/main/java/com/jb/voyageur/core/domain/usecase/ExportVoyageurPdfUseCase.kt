package com.jb.voyageur.core.domain.usecase

import android.content.Context
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.jb.voyageur.core.domain.model.*
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import com.jb.voyageur.core.ui.util.FormatPhysique
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class ExportVoyageurPdfUseCase @Inject constructor(
    private val voyageurRepository: VoyageurRepository,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(voyageurId: Long): ByteArray? {
        val voyageur = voyageurRepository.charger(voyageurId) ?: return null
        val caracs = voyageur.caracteristiques

        return try {
            val inputStream = context.assets.open("fiche.pdf")
            val reader = PdfReader(inputStream)
            val outputStream = ByteArrayOutputStream()
            val stamper = PdfStamper(reader, outputStream)
            val form = stamper.acroFields

            // DEBUG — à supprimer après diagnostic
            form.fields.keys.sorted().forEach { fieldName ->
                android.util.Log.d("PDF_FIELDS", "Champ disponible : '$fieldName'")
            }

            // ── Description ─────────────────────────────────────────
            form.setField("Text16", voyageur.nom)
            form.setField("1", voyageur.age?.toString() ?: "")
            form.setField("2", if (voyageur.sexe == Sexe.HOMME) "Homme" else "Femme")
            form.setField("3", voyageur.tailleCm?.let { FormatPhysique.formatTailleCm(it) } ?: "")
            form.setField("4", voyageur.poidsKg?.let { FormatPhysique.formatPoids(it) } ?: "")
            form.setField("5", voyageur.cheveux)
            form.setField("6", voyageur.yeux)
            form.setField("7", voyageur.beaute.toString())
            form.setField("8", voyageur.heureNaissance.label)
            form.setField("9", voyageur.heureNaissance.symbole.toString())

            val (signes1, signes2) = decouperSignesParticuliers(voyageur.signeParticulier)
            form.setField("10", signes1)
            form.setField("11", signes2)

            // ── Caractéristiques principales ─────────────────────────
            form.setField("12", caracs.taille.toString())
            form.setField("13", caracs.apparence.toString())
            form.setField("15", caracs.constitution.toString())
            form.setField("17", caracs.force.toString())
            form.setField("19", caracs.agilite.toString())
            form.setField("21", caracs.dexterite.toString())
            form.setField("23", caracs.vue.toString())
            form.setField("25", caracs.ouie.toString())
            form.setField("27", caracs.odoGout.toString())
            form.setField("29", caracs.volonte.toString())
            form.setField("31", caracs.intellect.toString())
            form.setField("33", caracs.empathie.toString())
            form.setField("35", caracs.reve.toString())
            form.setField("37", caracs.chance.toString())

            // ── Caractéristiques dérivées ────────────────────────────
            form.setField("39", caracs.melee.toString())
            form.setField("40", caracs.tir.toString())
            form.setField("41", caracs.lancer.toString())
            form.setField("42", caracs.derobee.toString())

            // ── Points et seuils ─────────────────────────────────────
            form.setField("145", caracs.sust.toString())
            form.setField("146", caracs.sc.toString())
            form.setField("147", "%.1f".format(caracs.encombrement))
            val bonusDom = caracs.bonusDom
            form.setField("148", if (bonusDom >= 0) "+$bonusDom" else bonusDom.toString())

            // ── Compétences — helper ─────────────────────────────────
            fun comp(nom: String) = niveauCompetence(nom, voyageur)

            // Générales
            form.setField("45", comp("Bricolage"))
            form.setField("49", comp("Chant"))
            form.setField("53", comp("Course"))
            form.setField("57", comp("Cuisine"))
            form.setField("61", comp("Danse"))
            form.setField("65", comp("Dessin"))
            form.setField("69", comp("Discrétion"))
            form.setField("73", comp("Escalade"))
            form.setField("77", comp("Saut"))
            form.setField("81", comp("Séduction"))
            form.setField("85", comp("Vigilance"))

            // Particulières
            form.setField("47", comp("Charpenterie"))
            form.setField("51", comp("Comédie"))
            form.setField("55", comp("Commerce"))
            form.setField("59", comp("Équitation"))
            form.setField("63", comp("Maçonnerie"))
            form.setField("67", comp("Musique"))
            form.setField("71", comp("Pickpocket"))
            form.setField("75", comp("Survie en cité"))
            form.setField("79", comp("Survie en extérieur"))
            form.setField("83", comp("Survie en désert"))
            form.setField("87", comp("Survie en forêt"))
            form.setField("89", comp("Survie en glaces"))
            form.setField("91", comp("Survie en marais"))
            form.setField("93", comp("Survie en montagne"))
            form.setField("95", comp("Survie en sous-sol"))
            form.setField("97", comp("Travestissement"))

            // Spécialisées
            form.setField("99",  comp("Acrobatie"))
            form.setField("101", comp("Chirurgie"))
            form.setField("103", comp("Jeu"))
            form.setField("105", comp("Jonglerie"))
            form.setField("107", comp("Maroquinerie"))
            form.setField("109", comp("Métallurgie"))
            form.setField("111", comp("Natation"))
            form.setField("113", comp("Navigation"))
            form.setField("115", comp("Orfèvrerie"))
            form.setField("117", comp("Serrurerie"))

            // Connaissances
            form.setField("119", comp("Alchimie"))
            form.setField("121", comp("Astrologie"))
            form.setField("123", comp("Botanique"))
            form.setField("125", comp("Écriture"))
            form.setField("127", comp("Légendes"))
            form.setField("129", comp("Médecine"))
            form.setField("131", comp("Zoologie"))

            // ── Draconic ─────────────────────────────────────────────
            fun draconic(niveau: Int) = if (niveau >= 0) "+$niveau" else niveau.toString()
            form.setField("133", draconic(voyageur.draconic.oniros))
            form.setField("139", draconic(voyageur.draconic.hypnos))
            form.setField("136", draconic(voyageur.draconic.narcos))
            form.setField("142", draconic(voyageur.draconic.thanatos))

            // ── Tronc Corps ──────────────────────────────────────────
            fun troncNiveau(tronc: Tronc, nom: String) =
                tronc.niveauPour(nom).let { if (it >= 0) "+$it" else it.toString() }

            form.setField("178", troncNiveau(voyageur.troncCorps, "Corps à corps"))
            form.setField("182", troncNiveau(voyageur.troncCorps, "Dague de mêlée"))
            form.setField("186", troncNiveau(voyageur.troncCorps, "Esquive"))

            // ── 4 meilleures autres compétences de combat ────────────
            val meilleures = meilleuresCompetencesCombat(voyageur)
            val champNoms   = listOf("174", "175", "176", "177")
            val champNiveaux = listOf("190", "194", "198", "202")
            meilleures.forEachIndexed { index, (nom, niveau) ->
                form.setField(champNoms[index], nom)
                form.setField(champNiveaux[index],
                    if (niveau >= 0) "+$niveau" else niveau.toString())
            }

            // On aplatit le formulaire pour que les champs ne soient plus éditables dans le PDF final
            stamper.setFormFlattening(true)
            stamper.close()
            reader.close()

            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun niveauCompetence(nom: String, voyageur: Voyageur): String {
        val comp   = CatalogueCompetences.toutes.find { it.nom == nom } ?: return ""
        val niveau = voyageur.competences[nom] ?: comp.niveauBase
        return if (niveau >= 0) "+$niveau" else niveau.toString()
    }

    private fun decouperSignesParticuliers(texte: String): Pair<String, String> {
        if (texte.length <= 60) return texte to ""
        val coupure = texte.lastIndexOf(' ', 60).takeIf { it > 0 } ?: 60
        return texte.substring(0, coupure).trim() to texte.substring(coupure).trim()
    }

    private fun meilleuresCompetencesCombat(voyageur: Voyageur): List<Pair<String, Int>> {
        val exclues = setOf("Corps à corps", "Dague de mêlée", "Esquive")

        val troncArmes = voyageur.troncArmes.membres
            .filter { it !in exclues }
            .map { it to voyageur.troncArmes.niveauPour(it) }

        val independantes = CatalogueCompetences.parFamille[FamilleCompetence.COMBAT_MELEE]
            ?.filter { it.appartientAuTronc == null && it.nom !in exclues }
            ?.map { it.nom to (voyageur.competences[it.nom] ?: it.niveauBase) }
            ?: emptyList()

        val tirLancer = CatalogueCompetences.parFamille[FamilleCompetence.TIR_LANCER]
            ?.map { it.nom to (voyageur.competences[it.nom] ?: it.niveauBase) }
            ?: emptyList()

        return (troncArmes + independantes + tirLancer)
            .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
            .take(4)
    }
}
