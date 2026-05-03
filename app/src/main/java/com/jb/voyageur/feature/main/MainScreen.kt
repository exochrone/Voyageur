package com.jb.voyageur.feature.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jb.voyageur.R
import com.jb.voyageur.core.ui.theme.GoudyAcc
import com.jb.voyageur.feature.caracteristiques.CaracteristiquesScreen
import com.jb.voyageur.feature.competences.CompetencesScreen
import com.jb.voyageur.ui.navigation.NavItem
import kotlinx.coroutines.launch

@Composable
fun MainScreen(voyageurId: Long) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val menuItems = listOf(
        NavItem.Caracteristiques,
        NavItem.Competences,
        NavItem.Sorts,
        NavItem.Equipement,
        NavItem.Archetype
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(240.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = GoudyAcc,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    menuItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(stringResource(item.labelRes), fontFamily = FontFamily.Serif, fontSize = 18.sp) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    NavigationDrawerItem(
                        label = { Text(stringResource(NavItem.Options.labelRes), fontFamily = FontFamily.Serif, fontSize = 18.sp) },
                        selected = currentDestination?.hierarchy?.any { it.route == NavItem.Options.route } == true,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(NavItem.Options.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = NavItem.Caracteristiques.route
        ) {
            composable(
                route = NavItem.Caracteristiques.route,
                arguments = listOf(navArgument("voyageurId") {
                    type = NavType.LongType
                    defaultValue = voyageurId
                })
            ) {
                CaracteristiquesScreen(
                    voyageurId = voyageurId,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            composable(
                route = NavItem.Competences.route,
                arguments = listOf(navArgument("voyageurId") {
                    type = NavType.LongType
                    defaultValue = voyageurId
                })
            ) {
                CompetencesScreen(
                    voyageurId = voyageurId,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            composable(NavItem.Sorts.route) {
                PlaceholderScreen("Sorts")
            }
            composable(NavItem.Equipement.route) {
                PlaceholderScreen("Équipement")
            }
            composable(NavItem.Archetype.route) {
                PlaceholderScreen("Archétype")
            }
            composable(NavItem.Options.route) {
                PlaceholderScreen("Options")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name, fontSize = 24.sp)
    }
}
