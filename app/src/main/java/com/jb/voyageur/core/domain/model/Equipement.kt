package com.jb.voyageur.core.domain.model

data class ObjetEquipement(
    val nom: String,
    val encombrement: Float,  // 0.0 à N
    val prix: Int             // en deniers
)
