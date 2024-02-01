import UIKit
import SwiftUI
import Combine
import SolivagantSampleAppShared

struct ContentView: View {

  var body: some View {
    ZStack(alignment: .center) {
      NavigationLink("To Compose View") { SecondView() }
        .buttonStyle(.plain)
        .padding()
    }
  }
}

// MARK: - ComposeView

struct ComposeView: UIViewControllerRepresentable {
  let savedStateSupport: NavigationSavedStateSupport

  func makeUIViewController(context: Context) -> UIViewController {
    MainViewControllerKt.MainViewController(savedStateSupport: savedStateSupport)
  }

  func updateUIViewController(_ uiViewController: UIViewController, context: Context) { }
}


@MainActor
class SecondViewModel: ObservableObject {
  let savedStateSupport = NavigationSavedStateSupport()

  init() {
    print("Init \(savedStateSupport)")
  }

  deinit {
    self.savedStateSupport.clear()
    print("Cleared \(savedStateSupport)")
  }
}

struct SecondView: View {
  @StateObject private var viewModel = SecondViewModel()

  var body: some View {
    ComposeView(savedStateSupport: viewModel.savedStateSupport)
      .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
      .overlay(
        NavigationLink("To Third View") { ThirdView() }
          .buttonStyle(.plain)
          .padding(),
        alignment: .bottomTrailing
      )
      .onAppear { print("onAppear") }
      .onDisappear { print("onDisappear") }
      .navigationBarTitle("", displayMode: .inline)
  }
}

// MARK: - ThirdView

struct ThirdView: View {
  var body: some View {
    Text("It is good!")
      .navigationTitle("Third view")
  }
}
