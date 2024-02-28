//
//  FirstView.swift
//  iosApp
//
//  Created by Hoc Nguyen T. on 28/02/2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import SwiftUI

struct FirstView: View {

  var body: some View {
    ZStack(alignment: .center) {
      NavigationLink("To Compose View") { SecondView() }
        .buttonStyle(.plain)
        .padding()
    }
  }
}
