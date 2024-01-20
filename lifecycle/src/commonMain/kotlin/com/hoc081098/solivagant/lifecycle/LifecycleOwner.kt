package com.hoc081098.solivagant.lifecycle

/**
 * A class that has an lifecycle.
 *
 * @see Lifecycle
 */
public interface LifecycleOwner {
  /**
   * Returns the Lifecycle of the provider.
   *
   * @return The lifecycle of the provider.
   */
  public val lifecycle: Lifecycle
}
