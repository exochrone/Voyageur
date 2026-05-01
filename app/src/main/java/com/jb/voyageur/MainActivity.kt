package com.jb.voyageur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jb.voyageur.feature.accueil.AccueilScreen
import com.jb.voyageur.feature.caracteristiques.CaracteristiquesScreen
import com.jb.voyageur.core.ui.theme.VoyageurTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoyageurTheme {
                VoyageurNavHost()
            }
        }
    }
}

@Composable
fun VoyageurNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "accueil") {
        composable("accueil") {
            AccueilScreen(
                onNavigateToCaracteristiques = { id ->
                    navController.navigate("caracteristiques/$id")
                }
            )
        }
        composable(
            route = "caracteristiques/{voyageurId}",
            arguments = listOf(navArgument("voyageurId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("voyageurId") ?: 0L
            CaracteristiquesScreen(voyageurId = id)
        }
    }
}
