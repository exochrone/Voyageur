package com.jb.voyageur.feature.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jb.voyageur.R
import com.jb.voyageur.core.ui.composable.BarreNavigationEcran
import com.jb.voyageur.core.ui.navigation.EcranCreation
import com.jb.voyageur.core.ui.theme.Luminari
import com.jb.voyageur.feature.caracteristiques.CaracteristiquesScreen
import com.jb.voyageur.feature.competences.CompetencesScreen
import com.jb.voyageur.feature.equipement.EquipementScreen
import com.jb.voyageur.feature.options.OptionsScreen
import com.jb.voyageur.feature.sorts.SortsScreen
import com.jb.voyageur.ui.navigation.NavItem
import kotlinx.coroutines.launch

fun EcranCreation.toRoute(): String = when (this) {
    EcranCreation.CARACTERISTIQUES -> NavItem.Caracteristiques.route
    EcranCreation.COMPETENCES      -> NavItem.Competences.route
    EcranCreation.SORTS            -> NavItem.Sorts.route
    EcranCreation.EQUIPEMENT       -> NavItem.Equipement.route
    EcranCreation.ARCHETYPE        -> NavItem.Archetype.route
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    voyageurId: Long,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val voyageurNom by viewModel.voyageurNom.collectAsStateWithLifecycle()
    val hautRevant by viewModel.hautRevant.collectAsStateWithLifecycle()
    val pdfExportState by viewModel.pdfExportState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null && pdfExportState is PdfExportState.Success) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write((pdfExportState as PdfExportState.Success).data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            viewModel.onPdfExportConsumed()
        }
    }

    LaunchedEffect(pdfExportState) {
        if (pdfExportState is PdfExportState.Success) {
            createDocumentLauncher.launch((pdfExportState as PdfExportState.Success).fileName)
        }
    }

    if (pdfExportState is PdfExportState.Error) {
        AlertDialog(
            onDismissRequest = viewModel::onPdfExportConsumed,
            title = { Text(stringResource(R.string.pdf_export_erreur_titre)) },
            text = { Text((pdfExportState as PdfExportState.Error).message) },
            confirmButton = {
                TextButton(onClick = viewModel::onPdfExportConsumed) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val onNaviguerVers: (EcranCreation) -> Unit = { ecran ->
        navController.navigate(ecran.toRoute()) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    var showRenameDialog by remember { mutableStateOf(false) }

    val menuItems = listOf(
        NavItem.Caracteristiques,
        NavItem.Competences,
        NavItem.Sorts,
        NavItem.Equipement,
        NavItem.Archetype
    )

    val drawerColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = Color(0xFF99958E),
        unselectedContainerColor = Color.Transparent,
        selectedTextColor = Color.Black,
        unselectedTextColor = Color.Black,
        selectedIconColor = Color.Black,
        unselectedIconColor = Color.Black
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(240.dp),
                drawerContainerColor = Color(0xFFE6E0D6)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = Luminari,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
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
                            colors = drawerColors,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Sauvegarde PDF
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_sauvegarde_pdf), fontFamily = FontFamily.Serif, fontSize = 18.sp) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.onExportPdf()
                        },
                        colors = drawerColors,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Aide
                    NavigationDrawerItem(
                        label = { Text(stringResource(NavItem.Aide.labelRes), fontFamily = FontFamily.Serif, fontSize = 18.sp) },
                        selected = currentRoute == NavItem.Aide.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(NavItem.Aide.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = drawerColors,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Options
                    NavigationDrawerItem(
                        label = { Text(stringResource(NavItem.Options.labelRes), fontFamily = FontFamily.Serif, fontSize = 18.sp) },
                        selected = currentRoute == NavItem.Options.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(NavItem.Options.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = drawerColors,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                val isSpecialScreen = currentRoute == NavItem.Aide.route || currentRoute == NavItem.Options.route
                val titleText = if (isSpecialScreen) {
                    stringResource(if (currentRoute == NavItem.Aide.route) R.string.menu_aide else R.string.menu_options)
                } else {
                    voyageurNom.ifBlank { stringResource(R.string.app_name) }
                }

                CenterAlignedTopAppBar(
                    modifier = Modifier.statusBarsPadding().height(48.dp),
                    windowInsets = WindowInsets(0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = null)
                        }
                    },
                    title = {
                        Text(
                            text = titleText,
                            fontFamily = FontFamily.Serif,
                            fontSize = 26.sp,
                            modifier = Modifier.clickable(enabled = !isSpecialScreen) { showRenameDialog = true }
                        )
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavItem.Caracteristiques.route,
                modifier = Modifier.padding(innerPadding)
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
                        onNaviguerVers = onNaviguerVers
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
                        onNaviguerVers = onNaviguerVers
                    )
                }
                composable(
                    route = NavItem.Sorts.route,
                    arguments = listOf(navArgument("voyageurId") {
                        type = NavType.LongType
                        defaultValue = voyageurId
                    })
                ) {
                    SortsScreen(
                        voyageurId = voyageurId,
                        onNaviguerVers = onNaviguerVers
                    )
                }
                composable(
                    route = NavItem.Equipement.route,
                    arguments = listOf(navArgument("voyageurId") {
                        type = NavType.LongType
                        defaultValue = voyageurId
                    })
                ) {
                    EquipementScreen(
                        voyageurId = voyageurId,
                        onNaviguerVers = onNaviguerVers
                    )
                }
                composable(NavItem.Archetype.route) {
                    PlaceholderScreen(
                        name = "Archétype",
                        ecranCourant = EcranCreation.ARCHETYPE,
                        hautRevant = hautRevant,
                        onNaviguerVers = onNaviguerVers
                    )
                }
                composable(NavItem.Aide.route) {
                    PlaceholderScreen("Aide")
                }
                composable(NavItem.Options.route) {
                    OptionsScreen()
                }
            }
        }

        if (showRenameDialog) {
            var tempName by remember {
                mutableStateOf(TextFieldValue(text = voyageurNom, selection = TextRange(0, voyageurNom.length)))
            }
            val focusRequester = remember { FocusRequester() }
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text(stringResource(R.string.description_nom_voyageur)) },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onRename(tempName.text)
                        showRenameDialog = false
                    }) { Text(stringResource(R.string.valider)) }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) { Text(stringResource(R.string.annuler)) }
                }
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    name: String,
    ecranCourant: EcranCreation? = null,
    hautRevant: Boolean = false,
    onNaviguerVers: ((EcranCreation) -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (ecranCourant != null && onNaviguerVers != null) {
            BarreNavigationEcran(
                titre = name,
                ecranCourant = ecranCourant,
                hautRevant = hautRevant,
                onNaviguerVers = onNaviguerVers
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(text = name, fontSize = 24.sp)
        }
    }
}
