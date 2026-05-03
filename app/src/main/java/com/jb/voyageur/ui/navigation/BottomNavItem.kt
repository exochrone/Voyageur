package com.jb.voyageur.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.jb.voyageur.R

sealed class BottomNavItem(val route: String, val icon: ImageVector, val labelRes: Int) {
    object Caracteristiques : BottomNavItem("caracteristiques", Icons.Default.Person, R.string.menu_caracteristiques)
    object Competences : BottomNavItem("competences", Icons.AutoMirrored.Filled.List, R.string.menu_competences)
    object Sorts : BottomNavItem("sorts", Icons.Default.AutoFixHigh, R.string.menu_sorts)
    object Equipement : BottomNavItem("equipement", Icons.Default.Inventory, R.string.menu_equipement)
    object Archetype : BottomNavItem("archetype", Icons.Default.Psychology, R.string.menu_archetype)
    object Options : BottomNavItem("options", Icons.Default.Settings, R.string.menu_options)
}
