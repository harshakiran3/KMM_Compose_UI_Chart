import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Application
import com.example.kmmcocoapods.StartScreenIOS
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController =
    Application("TravelApp-KMP") {
        Column {
            // To skip upper part of screen.
            Box(
                modifier = Modifier
                    .height(40.dp)
            )
            StartScreenIOS()
        }
    }