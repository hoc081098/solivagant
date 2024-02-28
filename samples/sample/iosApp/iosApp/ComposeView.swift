import UIKit
import SwiftUI
import Combine
import SolivagantSampleAppShared

private struct ComposeView: UIViewControllerRepresentable {
  let savedStateSupport: NavigationSavedStateSupport

  func makeUIViewController(context: Context) -> UIViewController {
    print("[SecondView] makeUIViewController \(savedStateSupport)")
    return MainViewControllerKt.MainViewController(savedStateSupport: savedStateSupport)
  }

  func updateUIViewController(_ uiViewController: UIViewController, context: Context) { }
  
  static func dismantleUIViewController(_ uiViewController: UIViewController, coordinator: Void) {
    print("[SecondView] dismantleUIViewController")
  }
}


private class SecondViewModel: ObservableObject {
  let savedStateSupport = NavigationSavedStateSupport()

  init() {
    print("[SecondView] \(self)::init \(savedStateSupport)")
  }

  deinit {
    self.savedStateSupport.clear()
    print("[SecondView] \(self)::deinit \(savedStateSupport)")
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
      .onAppear { print("[SecondView] onAppear") }
      .onDisappear { print("[SecondView] onDisappear") }
      .navigationBarTitle("", displayMode: .inline)
  }
}
