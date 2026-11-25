package com.xeylabs.doit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xeylabs.doit.data.TxnType
import com.xeylabs.doit.ui.HistoryScreen
import com.xeylabs.doit.ui.HomeScreen
import com.xeylabs.doit.ui.OnboardingScreen
import com.xeylabs.doit.ui.PlansScreen
import com.xeylabs.doit.ui.ProfileScreen
import com.xeylabs.doit.ui.QuickAddSheet
import com.xeylabs.doit.ui.ReportsScreen
import com.xeylabs.doit.ui.theme.DoitTheme
import com.xeylabs.doit.ui.theme.Emerald
import com.xeylabs.doit.ui.theme.EmeraldStrong
import com.xeylabs.doit.ui.theme.SurfaceLowest
import com.xeylabs.doit.viewmodel.MainViewModel
import com.xeylabs.doit.viewmodel.UpdateViewModel
import com.xeylabs.doit.widget.DoitWidgetProvider

enum class DoitTab(val label: String, val icon: ImageVector) {
    BERANDA("Beranda", Icons.Filled.GridView),
    ANALISIS("Analisis", Icons.Filled.Insights),
    ANGGARAN("Anggaran", Icons.Filled.AccountBalanceWallet),
    AKUN("Akun", Icons.Filled.ManageAccounts)
}

class MainActivity : ComponentActivity() {
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

@Composable
private fun MainScaffold(viewModel: MainViewModel, updateViewModel: UpdateViewModel) {
    var selectedTab by remember { mutableStateOf(DoitTab.BERANDA) }
    var addSheetType by remember { mutableStateOf<TxnType?>(null) }
    var showHistory by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            FluxBottomBar(
                selectedTab = selectedTab,
                onSelect = { selectedTab = it; showHistory = false },
                onAdd = { addSheetType = TxnType.EXPENSE }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (showHistory) {
                HistoryScreen(viewModel) { showHistory = false }
            } else {
                when (selectedTab) {
                    DoitTab.BERANDA -> HomeScreen(
                        viewModel = viewModel,
                        updateViewModel = updateViewModel,
                        onNavigate = { selectedTab = it },
                        onQuickAdd = { type -> addSheetType = type },
                        onShowHistory = { showHistory = true }
                    )
                    DoitTab.ANALISIS -> ReportsScreen(
                        viewModel = viewModel,
                        onNavigate = { selectedTab = it }
                    )
                    DoitTab.ANGGARAN -> PlansScreen(
                        viewModel = viewModel,
                        onNavigate = { selectedTab = it }
                    )
                    DoitTab.AKUN -> ProfileScreen(
                        viewModel = viewModel,
                        updateViewModel = updateViewModel,
                        onNavigate = { selectedTab = it }
                    )
                }
            }
        }
        val sheetType = addSheetType
        if (sheetType != null) {
            QuickAddSheet(
                viewModel = viewModel,
                onDismiss = { addSheetType = null },
                initialType = sheetType
            )
        }
    }
}

@Composable
private fun FluxBottomBar(
    selectedTab: DoitTab,
    onSelect: (DoitTab) -> Unit,
    onAdd: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(SurfaceLowest.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp).padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FluxNavItem(
                tab = DoitTab.BERANDA,
                selected = selectedTab == DoitTab.BERANDA,
                onClick = { onSelect(DoitTab.BERANDA) },
                modifier = Modifier.weight(1f)
            )
            FluxNavItem(
                tab = DoitTab.ANALISIS,
                selected = selectedTab == DoitTab.ANALISIS,
                onClick = { onSelect(DoitTab.ANALISIS) },
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(56.dp).offset(y = (-12).dp)
                        .clip(CircleShape).background(EmeraldStrong)
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Catat transaksi",
                        tint = Color(0xFF003824),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            FluxNavItem(
                tab = DoitTab.ANGGARAN,
                selected = selectedTab == DoitTab.ANGGARAN,
                onClick = { onSelect(DoitTab.ANGGARAN) },
                modifier = Modifier.weight(1f)
            )
            FluxNavItem(
                tab = DoitTab.AKUN,
                selected = selectedTab == DoitTab.AKUN,
                onClick = { onSelect(DoitTab.AKUN) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FluxNavItem(
    tab: DoitTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = if (selected) Emerald else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Text(
            tab.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Emerald else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier.size(4.dp).clip(CircleShape)
                .background(if (selected) Emerald else Color.Transparent)
        )
    }
}
