/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.compose.rally

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.rally.ui.accounts.AccountsScreen
import com.example.compose.rally.ui.accounts.SingleAccountScreen
import com.example.compose.rally.ui.bills.BillsScreen
import com.example.compose.rally.ui.components.RallyTabRow
import com.example.compose.rally.ui.overview.OverviewScreen
import com.example.compose.rally.ui.theme.RallyTheme

/**
 * This Activity recreates part of the Rally Material Study from
 * https://material.io/design/material-studies/rally.html
 */
class RallyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RallyApp()
        }
    }
}

@Composable
fun RallyApp() {
    RallyTheme {
        val navController = rememberNavController()

        val currentBackStack by navController.currentBackStackEntryAsState()
        // var currentScreen: RallyDestination by remember { mutableStateOf(Overview) }
        val currentDestination = currentBackStack?.destination
        // Change the variable to this and use Overview as a backup screen if this returns null
        val currentScreen =
            rallyTabRowScreens.find { it.route == currentDestination?.route } ?: Overview

        Scaffold(
            topBar = {
                RallyTabRow(
                    allScreens = rallyTabRowScreens,
                    // onTabSelected = { screen -> currentScreen = screen },
                    onTabSelected = { newScreen ->
                        // navController.navigate(newScreen.route) { launchSingleTop = true }
                        // Or rather with the extension helper function:
                        navController.navigateSingleTopTo(newScreen.route)
                    },
                    currentScreen = currentScreen
                )
            }
        ) { innerPadding ->
            // Box(Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Overview.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = Overview.route) {
                    OverviewScreen(
                        onClickSeeAllAccounts = {
                            navController.navigateSingleTopTo(Accounts.route)
                        },
                        onClickSeeAllBills = {
                            navController.navigateSingleTopTo(Bills.route)
                        },
                        onAccountClick = { accountType ->
//                            navController
//                                .navigateSingleTopTo("${SingleAccount.route}/$accountType")
                            navController.navigateToSingleAccount(accountType)
                        }
                    )
                }
                composable(route = Accounts.route) {
                    AccountsScreen(
                        onAccountClick = { accountType ->
//                            navController
//                                .navigateSingleTopTo("${SingleAccount.route}/$accountType")
                            // Or rather with an extension function:
                            navController.navigateToSingleAccount(accountType)
                        }
                    )
                }
                composable(route = Bills.route) {
                    BillsScreen()
                }
                composable(
                    route = SingleAccount.routeWithArgs,
                    arguments = SingleAccount.arguments,
                ) { navBackStackEntry ->
                    // Retrieve the passed argument
                    val accountType =
                        navBackStackEntry.arguments?.getString(SingleAccount.accountTypeArg)

                    // Pass accountType to SingleAccountScreen
                    SingleAccountScreen(accountType)
                }
            }
        }
    }
}

// Another extension helper
private fun NavHostController.navigateToSingleAccount(accountType: String) {
    this.navigateSingleTopTo("${SingleAccount.route}/$accountType")
}

// Extension helper to always pass the "launchSingleTop = true" flag
/*
From the codelab:

- launchSingleTop = true - as mentioned, this makes sure there will be at most one copy
  of a given destination on the top of the back stack
  In Rally app, this would mean that re-tapping the same tab multiple times
  doesn't launch multiple copies of the same destination

- popUpTo(startDestination) { saveState = true } - pop up to the start destination of the graph
  to avoid building up a large stack of destinations on the back stack as you select tabs.
  In Rally, this would mean that pressing the back arrow from any destination
  would pop the entire back stack to Overview.

- restoreState = true - determines whether this navigation action should restore
  any state previously saved by PopUpToBuilder.saveState or the popUpToSaveState attribute.
  Note that, if no state was previously saved with the destination ID being navigated to,
  this has no effect
  In Rally, this would mean that, re-tapping the same tab would keep the previous data
  and user state on the screen without reloading it again
 */
fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
