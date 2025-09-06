import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.libf.transportrng.App
import de.libf.transportrng.DatabaseModule
import de.libf.transportrng.PlatformModule
import de.libf.transportrng.TransportrModule
import de.libf.transportrng.ViewModelModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(
            TransportrModule,
            DatabaseModule,
            ViewModelModule,
            PlatformModule
        )
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "TransportrNG",
    ) {
        App()
    }
}