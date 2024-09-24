import SwiftUI
import ComposeApp
import CoreLocation

@main
struct iOSApp: App {

//     // KMM - Koin Call
     init() {
         HelperKt.doInitKoin()
     }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        
        CLAuthorizationStatus.authorizedAlways
    }
}
