package com.jb.voyageur.feature.competences

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jb.voyageur.core.domain.model.*
import com.jb.voyageur.core.domain.repository.SortRepository
import com.jb.voyageur.core.domain.repository.VoyageurRepository
import com.jb.voyageur.core.domain.usecase.*
import com.jb.voyageur.core.ui.helper.AideCompetenceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompetencesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val voyageurRepository: VoyageurRepository,
    private val sortRepository: SortRepository,
    private val modifierCompetenceUseCase: ModifierCompetenceUseCase,
    private val modifierNiveauTroncUseCase: ModifierNiveauTroncUseCase,
    private val modifierDraconicUseCase: ModifierDraconicUseCase,
    val aideCompetenceProvider: AideCompetenceProvider
) : ViewModel() {

    private val voyageurId: Long = savedStateHandle["voyageurId"] ?: 0L

    val uiState: StateFlow<CompetencesUiState> = voyageurRepository
        .observerVoyageur(voyageurId)
        .filterNotNull()
        .map { voyageur -> voyageur.toCompetencesUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CompetencesUiState.Loading
        )

    private val _aideActive = MutableStateFlow<String?>(null)
    val aideActive: StateFlow<String?> = _aideActive.asStateFlow()

    private val _isXPBlocked = MutableStateFlow(false)
    val isXPBlocked: StateFlow<Boolean> = _isXPBlocked.asStateFlow()

    private val _archetypeCompletAlerte = MutableStateFlow(false)
    val archetypeCompletAlerte: StateFlow<Boolean> = _archetypeCompletAlerte.asStateFlow()

    private val _messageDraconicBloque = MutableStateFlow<String?>(null)
    val messageDraconicBloque: StateFlow<String?> = _messageDraconicBloque.asStateFlow()

    fun onCompetenceChange(nom: String, absoluteBase: Int, niveauCible: Int) {
        viewModelScope.launch {
            val voyageur = voyageurRepository.charger(voyageurId) ?: return@launch
            val currentNiveau = voyageur.competences[nom] ?: absoluteBase
            
            // Nouveau contrôle : ne peut pas dépasser le niveau d'archétype
            val niveauArchetype = voyageur.archetype.niveaux[nom]
            if (niveauArchetype != null && niveauCible > niveauArchetype) {
                // Bloqué par l'archétype
                return@launch
            }

            if (niveauCible > currentNiveau) {
                val coutNiveauSuivant = CoutCompetence.coutUnNiveau(niveauCible)
                if (coutNiveauSuivant > calculPointsRestants(voyageur)) {
                    _isXPBlocked.value = true
                    return@launch
                }
            }
            
            // Pour les compétences custom, on ne veut pas qu'elles disparaissent si elles sont à la base
            // car elles portent le nom de la compétence.
            val isCustom = nom.startsWith("CUSTOM:")
            val finalNiveau = niveauCible.coerceIn(absoluteBase, 3)
            val nouvCompetences = voyageur.competences.toMutableMap()
            
            if (!isCustom && finalNiveau == absoluteBase) {
                nouvCompetences.remove(nom)
            } else {
                nouvCompetences[nom] = finalNiveau
            }
            voyageurRepository.sauvegarder(voyageur.copy(competences = nouvCompetences))
        }
    }

    fun ajouterCompetenceCustom(famille: FamilleCompetence, index: Int, nom: String) {
        viewModelScope.launch {
            val voyageur = voyageurRepository.charger(voyageurId) ?: return@launch
            val key = "CUSTOM:${famille.name}:$index:$nom"
            val nouvCompetences = voyageur.competences.toMutableMap()
            nouvCompetences[key] = famille.base
            voyageurRepository.sauvegarder(voyageur.copy(competences = nouvCompetences))
        }
    }

    fun supprimerCompetenceCustom(key: String) {
        viewModelScope.launch {
            val voyageur = voyageurRepository.charger(voyageurId) ?: return@launch
            val nouvCompetences = voyageur.competences.toMutableMap()
            nouvCompetences.remove(key)
            voyageurRepository.sauvegarder(voyageur.copy(competences = nouvCompetences))
        }
    }

    fun renommerCompetenceCustom(oldKey: String, nouveauNom: String) {
        viewModelScope.launch {
            val voyageur = voyageurRepository.charger(voyageurId) ?: return@launch
            val parts = oldKey.split(":")
            if (parts.size < 4) return@launch
            val prefix = "CUSTOM:${parts[1]}:${parts[2]}:"
            val newKey = "${prefix}$nouveauNom"
            
            val niveau = voyageur.competences[oldKey] ?: -4
            val nouvCompetences = voyageur.competences.toMutableMap()
            nouvCompetences.remove(oldKey)
            nouvCompetences[newKey] = niveau
            voyageurRepository.sauvegarder(voyageur.copy(competences = nouvCompetences))
        }
    }

    fun onTroncChange(nomTronc: String, membre: String, niveauCible: Int) {
        viewModelScope.launch {
            val voyageur = voyageurRepository.charger(voyageurId) ?: return@launch
            val tronc = if (nomTronc == "TroncCorps") voyageur.troncCorps else voyageur.troncArmes
            val currentNiveau = tronc.niveauPour(membre)

            if (niveauCible > currentNiveau) {
                val coutNiveauSuivant = CoutCompetence.coutUnNiveau(niveauCible)
                if (coutNiveauSuivant > calculPointsRestants(voyageur)) {
                    _isXPBlocked.value = true
                    return@launch
                }
            }
            modifierNiveauTroncUseCase(voyageurId, nomTronc, membre, niveauCible)
        }
    }

    fun onDraconicChange(voie: VoieDraconic, niveauCible: Int) {
        viewModelScope.launch {
            val voyageur = voyageurRepository.charger(voyageurId) ?: return@launch
            
            // Vérification si des sorts sont possédés dans cette voie
            val aDesSorts = voyageur.sorts.any { s ->
                val fileName = voie.name.lowercase() + ".txt"
                // On simplifie la vérification en cherchant si le sort appartient à la voie via le repository
                sortRepository.chargerSorts(voie).any { it.nom == s.nom }
            }
            
            if (aDesSorts) {
                _messageDraconicBloque.value = "Vous possédez des sorts dans cette voie : elle ne peut plus être modifiée.\nRenoncez à vos sorts pour ajuster son niveau."
                return@launch
            }

            val currentNiveau = voyageur.draconic.niveau(voie)

            if (niveauCible > currentNiveau) {
                val multi = voyageur.draconic.multiplicateurPour(voie)
                val coutNiveauSuivant = CoutCompetence.coutUnNiveau(niveauCible) * multi
                if (coutNiveauSuivant > calculPointsRestants(voyageur)) {
                    _isXPBlocked.value = true
                    return@launch
                }
            }
            modifierDraconicUseCase(voyageurId, voie, niveauCible)
        }
    }

    fun resetXPBlocked() {
        _isXPBlocked.value = false
    }

    fun effacerAlerteArchetype() {
        _archetypeCompletAlerte.value = false
    }

    fun onTaperPlaceholder() {
        viewModelScope.launch {
            val voyageur = voyageurRepository.charger(voyageurId) ?: return@launch
            if (voyageur.archetype.estComplet()) {
                _archetypeCompletAlerte.value = true
            }
        }
    }

    fun effacerMessageDraconic() {
        _messageDraconicBloque.value = null
    }

    fun onDemanderAide(nom: String) {
        _aideActive.value = nom
    }

    fun onFermerAide() {
        _aideActive.value = null
    }

    private fun Voyageur.toCompetencesUiState(): CompetencesUiState.Success {
        val pointsRestants = calculPointsRestants(this)
        val pointsSorts = sorts.sumOf { it.coutPaye }
        val colonnes = buildColonnes(this)
        val aDesSorts = hautRevant && (
            draconic.oniros > -11 || 
            draconic.hypnos > -11 || 
            draconic.narcos > -11 || 
            draconic.thanatos > -11
        )
        return CompetencesUiState.Success(
            colonnes = colonnes,
            pointsRestants = pointsRestants,
            pointsSortsUtilises = pointsSorts,
            hautRevant = hautRevant,
            aDesSortsAccessibles = aDesSorts,
            archetypeEstComplet = archetype.estComplet(),
            niveauxArchetype = archetype.niveaux
        )
    }

    private fun calculPointsRestants(voyageur: Voyageur): Int {
        val pointsDraconic = voyageur.draconic.pointsTotal()
        
        val pointsCompetences = voyageur.competences.entries.sumOf { (key, niveau) ->
            val famille = if (key.startsWith("CUSTOM:")) {
                val familleName = key.split(":")[1]
                FamilleCompetence.valueOf(familleName)
            } else {
                CatalogueCompetences.toutes.find { it.nom == key }?.famille ?: return@sumOf 0
            }
            CoutCompetence.coutCumule(famille.base, niveau)
        }

        val pointsTroncs = voyageur.troncCorps.coutTotal() + voyageur.troncArmes.coutTotal()
        
        // Calcul des points de sorts
        val pointsSorts = voyageur.sorts.sumOf { it.coutPaye }

        return 3000 - pointsDraconic - pointsCompetences - pointsTroncs - pointsSorts
    }

    private fun buildColonnes(voyageur: Voyageur): List<ColonneCompetences> {
        val result = mutableListOf<ColonneCompetences>()

        // 1. Générales
        result.add(ColonneCompetences(
            famille = FamilleCompetence.GENERALE,
            items = buildItems(FamilleCompetence.GENERALE, voyageur)
        ))

        // 2. Particulières
        result.add(ColonneCompetences(
            famille = FamilleCompetence.PARTICULIERE,
            items = buildItems(FamilleCompetence.PARTICULIERE, voyageur)
        ))

        // 3. Combat mêlée
        result.add(ColonneCompetences(
            famille = FamilleCompetence.COMBAT_MELEE,
            items = buildItemsCombat(voyageur)
        ))

        // 4. Tir et Lancer
        result.add(ColonneCompetences(
            famille = FamilleCompetence.TIR_LANCER,
            items = buildItems(FamilleCompetence.TIR_LANCER, voyageur)
        ))

        // 5. Spécialisées
        result.add(ColonneCompetences(
            famille = FamilleCompetence.SPECIALISEE,
            items = buildItems(FamilleCompetence.SPECIALISEE, voyageur)
        ))

        // 6. Connaissances
        result.add(ColonneCompetences(
            famille = FamilleCompetence.CONNAISSANCE,
            items = buildItems(FamilleCompetence.CONNAISSANCE, voyageur)
        ))

        if (voyageur.hautRevant) {
            result.add(ColonneCompetences(
                famille = FamilleCompetence.DRACONIC,
                items = buildItemsDraconic(voyageur)
            ))
        }

        return result
    }

    private fun buildItems(famille: FamilleCompetence, voyageur: Voyageur): List<CompetenceUiItem> {
        val items = mutableListOf<CompetenceUiItem>()
        
        // 1. Compétences du catalogue
        CatalogueCompetences.parFamille[famille]?.forEach { comp ->
            items.add(if (comp.nom in CatalogueCompetences.SURVIES_SPECIFIQUES) {
                construireItemSurvieRestreinte(comp, voyageur)
            } else if (comp.nom == "Survie en extérieur") {
                construireItemSurvieExterieur(comp, voyageur)
            } else {
                construireItemIndividuel(comp, voyageur)
            })
        }
        
        // 2. Slots custom (3 par famille)
        for (i in 0 until 3) {
            val customKeyPrefix = "CUSTOM:${famille.name}:$i:"
            val existingKey = voyageur.competences.keys.find { it.startsWith(customKeyPrefix) }
            
            if (existingKey != null) {
                val nom = existingKey.substringAfter(customKeyPrefix)
                val niveau = voyageur.competences[existingKey] ?: famille.base
                val niveauArchetype = voyageur.archetype.niveaux[existingKey]
                items.add(CompetenceUiItem.Individuelle(
                    competence = Competence(nom, famille),
                    niveauActuel = niveau,
                    coutCumule = CoutCompetence.coutCumule(famille.base, niveau),
                    isCustom = true,
                    customKey = existingKey,
                    niveauArchetype = niveauArchetype,
                    depasseArchetype = niveauArchetype != null && niveau > niveauArchetype
                ))
            } else {
                items.add(CompetenceUiItem.Placeholder(famille, i))
            }
        }
        
        return items
    }

    private fun buildItemsCombat(voyageur: Voyageur): List<CompetenceUiItem> {
        val items = mutableListOf<CompetenceUiItem>()

        // TroncCorps
        val corps = voyageur.troncCorps
        corps.membres.forEachIndexed { index, nom ->
            val niveau = corps.niveauPour(nom)
            val comp = CatalogueCompetences.toutes.find { it.nom == nom }
                ?: Competence(nom, FamilleCompetence.COMBAT_MELEE, "TroncCorps")

            val coutBaseTronc = CoutCompetence.coutCumule(corps.niveauBase, minOf(0, corps.niveauCommun))
            val coutIndividuel = if (niveau > 0) CoutCompetence.coutCumule(0, niveau) else 0

            val isAncre = if (corps.membreAncreCommun != null) {
                nom == corps.membreAncreCommun
            } else {
                index == 0
            }

            val autreMembresSupZero = troncMembresSupZero(corps, nom)
            val borneInf = if (autreMembresSupZero.isNotEmpty()) 0 else corps.niveauBase
            
            val niveauArchetype = voyageur.archetype.niveaux[nom]
            val borneSup = if (niveauArchetype != null) minOf(3, niveauArchetype) else 3

            items.add(CompetenceUiItem.Individuelle(
                competence = comp,
                niveauActuel = niveau,
                coutCumule = (if (isAncre) coutBaseTronc else 0) + coutIndividuel,
                borneInf = borneInf,
                borneSup = borneSup,
                appartientAuTronc = "TroncCorps",
                estPremierDuTronc = index == 0,
                niveauArchetype = niveauArchetype,
                depasseArchetype = niveauArchetype != null && niveau > niveauArchetype
            ))
        }

        // TroncArmes
        val armes = voyageur.troncArmes
        armes.membres.forEachIndexed { index, nom ->
            val niveau = armes.niveauPour(nom)
            val comp = CatalogueCompetences.toutes.find { it.nom == nom }
                ?: Competence(nom, FamilleCompetence.COMBAT_MELEE, "TroncArmes")

            val coutBaseTronc = CoutCompetence.coutCumule(armes.niveauBase, minOf(0, armes.niveauCommun))
            val coutIndividuel = if (niveau > 0) CoutCompetence.coutCumule(0, niveau) else 0

            val isAncre = if (armes.membreAncreCommun != null) {
                nom == armes.membreAncreCommun
            } else {
                index == 0
            }

            val autreMembresSupZero = troncMembresSupZero(armes, nom)
            val borneInf = if (autreMembresSupZero.isNotEmpty()) 0 else armes.niveauBase

            val niveauArchetype = voyageur.archetype.niveaux[nom]
            val borneSup = if (niveauArchetype != null) minOf(3, niveauArchetype) else 3

            items.add(CompetenceUiItem.Individuelle(
                competence = comp,
                niveauActuel = niveau,
                coutCumule = (if (isAncre) coutBaseTronc else 0) + coutIndividuel,
                borneInf = borneInf,
                borneSup = borneSup,
                appartientAuTronc = "TroncArmes",
                estPremierDuTronc = index == 0,
                niveauArchetype = niveauArchetype,
                depasseArchetype = niveauArchetype != null && niveau > niveauArchetype
            ))
        }

        CatalogueCompetences.parFamille[FamilleCompetence.COMBAT_MELEE]
            ?.filter { it.appartientAuTronc == null }
            ?.forEach { comp ->
                items.add(construireItemIndividuel(comp, voyageur))
            }
            
        // 3 Slots custom pour Combat Mêlée
        val combatFamille = FamilleCompetence.COMBAT_MELEE
        for (i in 0 until 3) {
            val customKeyPrefix = "CUSTOM:${combatFamille.name}:$i:"
            val existingKey = voyageur.competences.keys.find { it.startsWith(customKeyPrefix) }
            
            if (existingKey != null) {
                val nom = existingKey.substringAfter(customKeyPrefix)
                val niveau = voyageur.competences[existingKey] ?: combatFamille.base
                val niveauArchetype = voyageur.archetype.niveaux[existingKey]
                items.add(CompetenceUiItem.Individuelle(
                    competence = Competence(nom, combatFamille),
                    niveauActuel = niveau,
                    coutCumule = CoutCompetence.coutCumule(combatFamille.base, niveau),
                    isCustom = true,
                    customKey = existingKey,
                    niveauArchetype = niveauArchetype,
                    depasseArchetype = niveauArchetype != null && niveau > niveauArchetype
                ))
            } else {
                items.add(CompetenceUiItem.Placeholder(combatFamille, i))
            }
        }

        return items
    }

    private fun troncMembresSupZero(tronc: Tronc, membreExclu: String): List<String> {
        return tronc.niveauxIndividuels
            .filter { it.key != membreExclu && it.value > 0 }
            .map { it.key }
    }

    private fun buildItemsDraconic(voyageur: Voyageur): List<CompetenceUiItem.Individuelle> =
        VoieDraconic.entries.map { voie ->
            val niveau = voyageur.draconic.niveau(voie)
            val multiplicateur = voyageur.draconic.multiplicateurPour(voie)
            
            // Vérifier si la voie contient des sorts achetés
            val sortsDeLaVoie = sortRepository.chargerSorts(voie).map { it.nom }.toSet()
            val aDesSorts = voyageur.sorts.any { it.nom in sortsDeLaVoie }

            val comp = Competence(
                nom = voie.name.lowercase().replaceFirstChar { it.uppercase() },
                famille = FamilleCompetence.DRACONIC
            )
            CompetenceUiItem.Individuelle(
                competence = comp,
                niveauActuel = niveau,
                coutCumule = CoutCompetence.coutCumuleAvecMultiplicateur(-11, niveau, multiplicateur),
                borneInf = -11,
                borneSup = 3,
                estVerrouilleParSorts = aDesSorts
            )
        }

    private fun construireItemIndividuel(competence: Competence, voyageur: Voyageur): CompetenceUiItem.Individuelle {
        val niveauActuel = voyageur.competences[competence.nom] ?: competence.niveauBase
        val niveauArchetype = voyageur.archetype.niveaux[competence.nom]
        val borneSup = if (niveauArchetype != null) minOf(3, niveauArchetype) else 3
        return CompetenceUiItem.Individuelle(
            competence = competence,
            niveauActuel = niveauActuel,
            coutCumule = CoutCompetence.coutCumule(competence.famille.base, niveauActuel),
            borneInf = competence.niveauBase,
            borneSup = borneSup,
            niveauArchetype = niveauArchetype,
            depasseArchetype = niveauArchetype != null && niveauActuel > niveauArchetype
        )
    }

    private fun construireItemSurvieRestreinte(competence: Competence, voyageur: Voyageur): CompetenceUiItem.Individuelle {
        val niveauActuel = voyageur.competences[competence.nom] ?: competence.niveauBase
        val survieExterieur = voyageur.competences["Survie en extérieur"] ?: -8
        val niveauArchetype = voyageur.archetype.niveaux[competence.nom]
        
        var borneSup = if (survieExterieur >= 0) 3 else survieExterieur
        if (niveauArchetype != null) borneSup = minOf(borneSup, niveauArchetype)

        return CompetenceUiItem.Individuelle(
            competence = competence,
            niveauActuel = niveauActuel,
            coutCumule = CoutCompetence.coutCumule(competence.famille.base, niveauActuel),
            borneInf = competence.niveauBase,
            borneSup = borneSup,
            niveauArchetype = niveauArchetype,
            depasseArchetype = niveauArchetype != null && niveauActuel > niveauArchetype
        )
    }

    private fun construireItemSurvieExterieur(competence: Competence, voyageur: Voyageur): CompetenceUiItem.Individuelle {
        val niveauActuel = voyageur.competences[competence.nom] ?: competence.niveauBase
        val maxSpecific = CatalogueCompetences.SURVIES_SPECIFIQUES
            .map { voyageur.competences[it] ?: -8 }
            .maxOrNull() ?: -8
        
        // Si une survie spécifique est >= 0, l'extérieur est bloqué à 0 minimum.
        // Sinon (toutes < 0), l'extérieur est bloqué au max des survies spécifiques.
        val borneInf = if (maxSpecific >= 0) 0 else maxSpecific
        val niveauArchetype = voyageur.archetype.niveaux[competence.nom]
        
        val borneSup = if (niveauArchetype != null) minOf(3, niveauArchetype) else 3

        return CompetenceUiItem.Individuelle(
            competence = competence,
            niveauActuel = niveauActuel,
            coutCumule = CoutCompetence.coutCumule(competence.famille.base, niveauActuel),
            borneInf = borneInf,
            borneSup = borneSup,
            niveauArchetype = niveauArchetype,
            depasseArchetype = niveauArchetype != null && niveauActuel > niveauArchetype
        )
    }
}

sealed interface CompetencesUiState {
    data object Loading : CompetencesUiState
    data class Success(
        val colonnes: List<ColonneCompetences>,
        val pointsRestants: Int,
        val pointsSortsUtilises: Int,
        val hautRevant: Boolean,
        val aDesSortsAccessibles: Boolean,
        val archetypeEstComplet: Boolean,
        val niveauxArchetype: Map<String, Int>
    ) : CompetencesUiState
}

data class ColonneCompetences(
    val famille: FamilleCompetence,
    val items: List<CompetenceUiItem>
)

sealed interface CompetenceUiItem {
    data class Individuelle(
        val competence: Competence,
        val niveauActuel: Int,
        val coutCumule: Int,
        val borneInf: Int = competence.niveauBase,
        val borneSup: Int = 3,
        val appartientAuTronc: String? = null,
        val estPremierDuTronc: Boolean = false,
        val estVerrouilleParSorts: Boolean = false,
        val isCustom: Boolean = false,
        val customKey: String? = null,
        val niveauArchetype: Int? = null,
        val depasseArchetype: Boolean = false
    ) : CompetenceUiItem

    data class Placeholder(
        val famille: FamilleCompetence,
        val index: Int
    ) : CompetenceUiItem
}
