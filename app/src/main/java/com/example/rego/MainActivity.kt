package com.example.rego

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.rego.data.local.AppDatabase
import com.example.rego.data.local.UserSession
import com.example.rego.data.repository.ProductRepository
import com.example.rego.navigation.NavGraph
import com.example.rego.navigation.Screen
import com.example.rego.navigation.bottomNavItems
import com.example.rego.ui.theme.ReGoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReGoTheme {
                ReGoApp()
            }
        }
    }
}

@Composable
fun ReGoApp() {
    val context = LocalContext.current
    val userSession = remember { UserSession(context) }
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { 
        ProductRepository(
            database.productDao(),
            database.categoryDao(),
            database.favoriteDao()
        )
    }
    
    val userId by userSession.userId.collectAsState(initial = null)
    val isLoggedIn = userId != null

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if we should show bottom bar
    val showBottomBar = when (currentDestination?.route) {
        Screen.Home.route,
        Screen.Products.route,
        Screen.Sell.routePattern,
        Screen.Chat.route,
        Screen.Settings.route,
        Screen.Profile.route -> true
        else -> false
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { 
                            it.route == screen.routePattern || it.route == screen.route 
                        } == true
                        
                        NavigationBarItem(
                            icon = { screen.icon?.let { Icon(it, contentDescription = screen.title) } },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF00695C),
                                selectedTextColor = Color(0xFF00695C),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color(0xFFE0F2F1)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavGraph(
                navController = navController,
                userSession = userSession,
                repository = repository,
                productDao = database.productDao(),
                cartDao = database.cartDao(),
                userDao = database.userDao(),
                orderDao = database.orderDao(),
                addressDao = database.addressDao(),
                isLoggedIn = isLoggedIn,
                userId = userId
            )
        }
    }
}
