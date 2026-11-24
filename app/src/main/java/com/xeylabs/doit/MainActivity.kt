package com.xeylabs.doit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xeylabs.doit.ui.BudgetScreen
import com.xeylabs.doit.ui.HistoryScreen
import com.xeylabs.doit.ui.HomeScreen
import com.xeylabs.doit.ui.OnboardingScreen
import com.xeylabs.doit.ui.QuickAddSheet
import com.xeylabs.doit.ui.SavingsScreen
import com.xeylabs.doit.ui.theme.DoitTheme
import com.xeylabs.doit.viewmodel.MainViewModel
import com.xeylabs.doit.viewmodel.UpdateViewModel
import com.xeylabs.doit.widget.DoitWidgetProvider

private data class Tab(val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("Beranda", Icons.Filled.Home),
    Tab("Riwayat", Icons.Filled.List),
    Tab("Tabungan", Icons.Filled.Savings),
    Tab("Anggaran", Icons.Filled.Wallet)
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DoitWidgetProvider.refresh(this)
        setContent {
            DoitTheme {
                val viewModel: MainViewModel = viewModel()
                val updateViewModel: UpdateViewModel = viewModel()
                val onboardingDone by viewModel.onboardingDone.collectAsState()

                if (!onboardingDone) {
                    OnboardingScreen(viewModel)
                } else {
                    MainScaffold(viewModel, updateViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(viewModel: MainViewModel, updateViewModel: UpdateViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("do.it", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Catat baru")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(viewModel, updateViewModel, onOpenSavings = { selectedTab = 2 })
                1 -> HistoryScreen(viewModel)
                2 -> SavingsScreen(viewModel)
                else -> BudgetScreen(viewModel)
            }
        }
        if (showAdd) {
            QuickAddSheet(
                viewModel = viewModel,
                onDismiss = { showAdd = false }
            )
        }
    }
}
