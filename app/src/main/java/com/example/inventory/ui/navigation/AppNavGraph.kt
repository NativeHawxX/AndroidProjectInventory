package com.example.inventory.ui.navigation

import androidx.compose.runtime.Composable
import android.net.Uri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.inventory.ui.screens.InventoryScreen
import com.example.inventory.ui.screens.container.ContainerDetailScreen
import com.example.inventory.ui.screens.scanner.InvalidQrScreen
import com.example.inventory.ui.screens.scanner.QrScannerScreen

object Routes {
    const val LIST = "list"
    const val CONTAINER = "container/{containerId}"
    const val SCANNER = "scanner"
    const val INVALID_QR = "invalid_qr/{payload}"

    fun container(id: Long) = "container/$id"
    fun invalidQr(payload: String) = "invalid_qr/${Uri.encode(payload)}"
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            InventoryScreen(
                onContainerClick = { id -> navController.navigate(Routes.container(id)) },
                onScanClick = { navController.navigate(Routes.SCANNER) }
            )
        }

        composable(
            route = Routes.CONTAINER,
            arguments = listOf(navArgument("containerId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("containerId") ?: 0L
            ContainerDetailScreen(
                containerId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SCANNER) {
            QrScannerScreen(
                onBack = { navController.popBackStack() },
                onScanned = { payload ->
                    val id = payload.substringAfterLast('-').toLongOrNull()
                    if (id != null && payload.startsWith("INV-CONTAINER-")) {
                        navController.popBackStack()
                        navController.navigate(Routes.container(id))
                    } else {
                        navController.popBackStack()
                        navController.navigate(Routes.invalidQr(payload))
                    }
                }
            )
        }

        composable(
            route = Routes.INVALID_QR,
            arguments = listOf(navArgument("payload") { type = NavType.StringType })
        ) { entry ->
            val payload = entry.arguments?.getString("payload").orEmpty()
            InvalidQrScreen(
                payload = payload,
                onBack = {
                    navController.popBackStack(Routes.LIST, inclusive = false)
                },
                onScanAgain = {
                    navController.popBackStack()
                    navController.navigate(Routes.SCANNER)
                }
            )
        }
    }
}
