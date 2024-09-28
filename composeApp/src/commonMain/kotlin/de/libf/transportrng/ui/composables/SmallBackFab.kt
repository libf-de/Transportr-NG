package de.libf.transportrng.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun SmallBackFab(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    if(navController.previousBackStackEntry != null) {
        SmallFloatingActionButton(
            onClick = { navController.popBackStack() },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = modifier
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                null
            )
        }
    }
}