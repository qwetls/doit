package com.xeylabs.doit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
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
import com.xeylabs.doit.ui.HistoryScreen
import com.xeylabs.doit.ui.HomeScreen
import com.xeylabs.doit.ui.OnboardingScreen
import com.xeylabs.doit.ui.PlansScreen
import com.xeylabs.doit.ui.QuickAddSheet
import com.xeylabs.doit.ui.ReportsScreen
import com.xeylabs.doit.ui.WalletsScreen
import com.xeylabs.doit.ui.theme.DoitTheme
import com.xeylabs.doit.viewmodel.MainViewModel
import com.xeylabs.doit.viewmodel.UpdateViewModel
import com.xeylabs.doit.widget.DoitWidgetProvider

/** Satu tab = satu tugas jelas. Urutan enum = urutan tab di bawah. */
enum class DoitTab(val label: String, val icon: ImageVector) {
    BERANDA("Beranda", Icons.Filled.Home),
    RIWAYAT("Riwayat", Icons.Filled.List),
    DOMPET("Dompet", Icons.Filled.AccountBalanceWallet),
    RENCANA("Rencana", Icons.Filled.EventNote),
    LAPORAN("Laporan", Icons.Filled.BarChart)
}

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
    var selectedTab by remember { mutableStateOf(DoitTab.BERANDA) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("do.it", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            NavigationBar {
                DoitTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
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
                DoitTab.BERANDA -> HomeScreen(
                    viewModel = viewModel,
                    updateViewModel = updateViewModel,
                    onNavigate = { selectedTab = it }
                )
                DoitTab.RIWAYAT -> HistoryScreen(viewModel)
                DoitTab.DOMPET -> WalletsScreen(viewModel)
                DoitTab.RENCANA -> PlansScreen(viewModel)
                DoitTab.LAPORAN -> ReportsScreen(viewModel)
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
