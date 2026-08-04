package id.ihsg.stockanalysis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.ihsg.stockanalysis.theme.IhsgTheme
import id.ihsg.stockanalysis.ui.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IhsgTheme {
                DashboardScreen()
            }
        }
    }
}
