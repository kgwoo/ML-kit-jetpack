package com.example.ml_kit_jetpack.`gloabl-navigation-bar`

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomNavItem(
    var title: String,
    var icon: ImageVector
) {
    object Home :
        BottomNavItem(
            "pose-detection",
            Icons.Filled.Person
        )

    object List :
        BottomNavItem(
            "List",
            Icons.Filled.Home
        )

    object Analytics :
        BottomNavItem(
            "Analytics",
            Icons.Filled.Home
        )

    object Profile :
        BottomNavItem(
            "Profile",
            Icons.Filled.Home
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GlobalNavigationBar(poseDetectionCamera: @Composable () -> Unit) {
    val items = listOf(
        BottomNavItem.Home,
//        BottomNavItem.List,
//        BottomNavItem.Analytics,
//        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    AddItem(
                        screen = item
                    )
                }
            }
        },
    ) {
        Box {
            poseDetectionCamera()
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomNavItem
) {
    NavigationBarItem(
        // Text that shows bellow the icon
        label = {
            Text(text = screen.title)
        },

        // The icon resource
        icon = {
            Icon(
                screen.icon, contentDescription = screen.title
            )
        },

        // Display if the icon it is select or not
        selected = true,

        // Always show the label bellow the icon or not
        alwaysShowLabel = true,

        // Click listener for the icon
        onClick = { /*TODO*/ },

        // Control all the colors of the icon
        colors = NavigationBarItemDefaults.colors()
    )
}