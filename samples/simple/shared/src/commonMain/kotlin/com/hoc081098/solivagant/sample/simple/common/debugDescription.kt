package com.hoc081098.solivagant.sample.simple.common

import com.hoc081098.kmp.viewmodel.ViewModel

val ViewModel.debugDescription: String
  get() = toString().substringAfterLast('.')
