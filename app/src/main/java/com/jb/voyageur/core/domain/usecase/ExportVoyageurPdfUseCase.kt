package com.jb.voyageur.core.domain.usecase

import android.content.Context
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
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

            form.setField("TAILLE", caracs.taille.toString())
            form.setField("APPARENCE", caracs.apparence.toString())
            form.setField("CONSTITUTION", caracs.constitution.toString())
            form.setField("FORCE", caracs.force.toString())
            form.setField("AGILITE", caracs.agilite.toString())

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
}
