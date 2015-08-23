# Architectural Overview

The WDK client employs the Flux architecture, as described at https://facebook.github.io/flux/docs/overview.html#content.


# Simple Flux Overview

The Flux architecture contains these Entities:
  * Dispatcher
  * Stores
  * Actions
  * Views


# WDK Implementation Details

WDK's implementation differs slightly from what is described above:

  * WDK uses a single Store for the application state.
  * This single Store exposes a dispatch function that takes the place of the Dispatcher.


The motivations behind these differences are twofold:

  1. With a single Store, we can ensure that all of the application state has
     been updated before notifying state observers.
  2. Combining the dispatch functionality with the Store results in a simpler object dependency graph.


_It's important to note that this change does not preclude the ability to create
multiple Stores that are managed by a single Dispatcher._
