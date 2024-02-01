import UIKit
import SwiftUI
import SolivagantSampleAppShared

struct ComposeView: UIViewControllerRepresentable {
  let savedStateSupport: NavigationSavedStateSupport
  
  func makeUIViewController(context: Context) -> UIViewController {
    MainViewControllerKt.MainViewController(savedStateSupport: savedStateSupport)
  }

  func updateUIViewController(_ uiViewController: UIViewController, context: Context) { }
}

let savedStateSupport: NavigationSavedStateSupport = NavigationSavedStateSupport()

struct ContentView: View {
  
  var body: some View {
    ZStack {
      ComposeView(savedStateSupport: savedStateSupport)
        .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
      
      VStack {
        NavigationLink("Click me", destination: SecondView())
      }
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
  }
}


struct SecondView: View {
  var body: some View {
    Text("OK")
      .navigationTitle("Second")
  }
}

struct SecondView_Previews: PreviewProvider {
  static var previews: some View {
    SecondView()
  }
}
