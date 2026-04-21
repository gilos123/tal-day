package com.maayan.studytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maayan.studytracker.ui.navigation.AppNavGraph
import com.maayan.studytracker.ui.navigation.Routes
import com.maayan.studytracker.ui.settings.SettingsViewModel
import com.maayan.studytracker.ui.theme.MaayanTheme
import com.maayan.studytracker.ui.timer.TimerMiniBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Collect the user's Material You preference at the composition root so
            // flipping the toggle re-themes the whole app without a restart.
            val settingsVm: SettingsViewModel = hiltViewModel()
            val useDynamicColor by settingsVm.useDynamicColor.collectAsStateWithLifecycle()

            MaayanTheme(useDynamicColor = useDynamicColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val onTimerScreen = backStackEntry?.destination?.route == Routes.TIMER

                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            AppNavGraph(navController = navController)
                        }
                        if (!onTimerScreen) {
                            TimerMiniBar(
                                onExpand = {
                                    if (backStackEntry?.destination?.route != Routes.TIMER) {
                                        navController.navigate(Routes.TIMER)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
