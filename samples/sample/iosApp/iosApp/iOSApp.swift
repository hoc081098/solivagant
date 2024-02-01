import SwiftUI
import SolivagantSampleAppShared

@main
struct iOSApp: App {
  init() {
    DIContainer.shared.doInit(appDeclaration: { _ in })
  }

  var body: some Scene {
    WindowGroup {
      NavigationView {
        ContentView()
      }.navigationViewStyle(.stack)
    }
  }
}
