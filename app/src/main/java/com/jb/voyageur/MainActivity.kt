package com.jb.voyageur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jb.voyageur.core.data.DebugSettingsManager
import com.jb.voyageur.feature.accueil.AccueilScreen
import com.jb.voyageur.feature.main.MainScreen
import com.jb.voyageur.core.ui.theme.VoyageurTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var debugSettingsManager: DebugSettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val backgroundImageUri by debugSettingsManager.backgroundImageUri.collectAsStateWithLifecycle()
            
            VoyageurTheme(backgroundImageUri = backgroundImageUri) {
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
            MainScreen(voyageurId = id)
        }
    }
}
