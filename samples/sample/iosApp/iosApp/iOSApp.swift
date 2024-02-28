//
//  ThirdView.swift
//  iosApp
//
//  Created by Petrus Nguyen Thai Hoc on 28/02/2024.
//  Copyright © 2024 orgName. All rights reserved.
//

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
        FirstView()
      }.navigationViewStyle(.stack)
    }
  }
}
