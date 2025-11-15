package com.example.outsy.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.outlined.Place
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.outsy.ui.user.ranking.map.UserMapComponent

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        UserMainScreen(navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = @androidx.compose.runtime.Composable {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Search Button
                        IconButton(
                            onClick = { /* TODO: Open search */ },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }

                        // Title
                        Text("Outsy", style = MaterialTheme.typography.titleLarge)

                        // Profile section at TB
                        IconButton(
                            onClick = { /* TODO: Open profile */ },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Menu, contentDescription = "Map") },
                    label = { Text("Rankings") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Place, contentDescription = "Ranking") },
                    label = { Text("Map") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Face, contentDescription = "Friends") },
                    label = { Text("Friends") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                // 0 -> RankingScreen()
                1 -> UserMapComponent(navController)
                // 2 -> FriendsScreen()
            }
        }
    }
}

