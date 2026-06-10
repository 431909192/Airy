package com.mazhuo.airy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mazhuo.airy.ui.screens.AddServerScreen
import com.mazhuo.airy.ui.screens.FileBrowserScreen
import com.mazhuo.airy.ui.screens.HomeScreen
import com.mazhuo.airy.ui.theme.AiryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AiryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                onAddConnectionClick = {
                                    navController.navigate("add_server")
                                },
                                onServerClick = { config ->
                                    navController.navigate("file_explorer/remote/${config.id}?name=${config.name}")
                                },
                                onLocalClick = {
                                    navController.navigate("file_explorer/local/-1?name=本地存储")
                                }
                            )
                        }
                        composable("add_server") {
                            AddServerScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "file_explorer/{type}/{serverId}?name={name}",
                            arguments = listOf(
                                navArgument("type") { type = NavType.StringType },
                                navArgument("serverId") { type = NavType.StringType },
                                navArgument("name") {
                                    type = NavType.StringType
                                    defaultValue = "文件浏览器"
                                }
                            )
                        ) { backStackEntry ->
                            val titleName = backStackEntry.arguments?.getString("name") ?: "文件浏览器"
                            FileBrowserScreen(
                                titleName = titleName,
                                onExitScreen = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}