package com.chronoplan.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.chronoplan.R
import com.chronoplan.data.model.NoteDto
import com.chronoplan.ui.agenda.AgendaScreen
import com.chronoplan.ui.akun.AkunScreen
import com.chronoplan.ui.home.HomeScreen
import com.chronoplan.ui.note.NoteScreen
import com.chronoplan.ui.note.NoteEditorScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chronoplan.di.AppViewModelFactory
import com.chronoplan.ui.note.NoteViewModel

sealed class Screen(val route: String, val label: String, val iconRes: Int) {
    object Home : Screen("home", "Home", R.drawable.ic_home)
    object Agenda : Screen("agenda", "Agenda", R.drawable.ic_calendar)
    object Note : Screen("note", "Note", R.drawable.ic_note)
    object Akun : Screen("akun", "Akun", R.drawable.ic_person)
    object NoteEditor : Screen("note_editor", "Editor", R.drawable.ic_note)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Agenda,
    Screen.Note,
    Screen.Akun
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onLogoutSuccess: () -> Unit = {}
) {
    val mainNavController = rememberNavController()
    val noteViewModel: NoteViewModel = viewModel(factory = AppViewModelFactory())

    var showBottomBar by remember { mutableStateOf(true) }

    // Hide bottom bar on note editor
    mainNavController.addOnDestinationChangedListener { _, destination, _ ->
        showBottomBar = destination.route != Screen.NoteEditor.route
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF303F9F),
                    contentColor = Color.White.copy(alpha = 0.6f),
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(75.dp)
                ) {
                    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(painterResource(id = screen.iconRes), contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = selected,
                            onClick = {
                                mainNavController.navigate(screen.route) {
                                    popUpTo(mainNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = Color.White.copy(alpha = 0.15f),
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = mainNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(Modifier.fillMaxSize()) }
            composable(Screen.Agenda.route) { AgendaScreen(Modifier.fillMaxSize()) }
            composable(Screen.Note.route) {
                NoteScreen(
                    modifier = Modifier.fillMaxSize(),
                    onNavigateToEditor = { note ->
                        mainNavController.navigate(Screen.NoteEditor.route)
                    },
                    viewModel = noteViewModel
                )
            }
            composable(Screen.NoteEditor.route) {
                val noteToEdit = noteViewModel.state.collectAsState().value.noteToEdit
                NoteEditorScreen(
                    existingNote = noteToEdit,
                    onBack = { mainNavController.popBackStack() },
                    onSave = { note ->
                        if (note.id.isEmpty()) {
                            noteViewModel.addNote(note)
                        } else {
                            noteViewModel.updateNote(note)
                        }
                    },
                    onUploadImage = { uri ->
                        noteViewModel.uploadAttachment(uri)
                    }
                )
            }
            composable(Screen.Akun.route) {
                AkunScreen(
                    modifier = Modifier.fillMaxSize(),
                    onLogoutSuccess = onLogoutSuccess
                )
            }
        }
    }
}