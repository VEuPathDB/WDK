# Architectural Overview

The WDK client employs the Flux architecture, as described at https://facebook.github.io/flux/docs/overview.html#content.


# Simple Flux Overview

The Flux architecture contains these Entities:

## Dispatcher
Notifies Stores of Actions.

## Stores
Maintain and modify application state based on Actions.

## Actions
Message describe a requested change to the application.

## View Controllers
Manages dispatching Actions and observing changes to Stores, and rendering Views based on these changes.

## Views
Renders a User Interface based on current application state, and sends user interactions to parent Controller Views.


# WDK Implementation Details

WDK's implementation differs slightly from what is described above:

  * WDK uses a single Store for the application state.
  * This single Store exposes a dispatch method that takes the place of the Dispatcher.

The motivations behind these differences are twofold:

  1. With a single Store, we can ensure that all of the application state has
     been updated before notifying state observers, for any given Action that
     has been dispatched. Consider the following scenario with multiple Stores.
     If a View Controller is listening to changes on multiple Stores, and an
     Action results in each of these Stores changing, then the View Controller
     will cause a rerender for each Store update. It's possible to mitigate this
     with batching protocols, but things are arguably clearer when there is one
     Store.
  2. Combining the dispatch functionality with the Store results in a simpler
     object dependency graph.


_It's important to note that this change does not preclude the ability to create multiple Stores that are managed by a single Dispatcher._


## Store

As mentioned above, the WDK Client contains a single Store. The Store is defined by a single reduce function of the type `<S>(state: S, action: { type: string; ...}) => S`. In other words, for every Action that is dispatched, this function will return a new State based on the old State. The Store holds a reference to the current State, and it notifies observers when the State has changed due to a dispatch call.



# API


## Types

- **State**
`Object`

- **Action**
`{ type: string; ... }`

- **Filter**
`<X>(store: Store, next: (action: Action) => X, action: Action) => X`


## Store

- **create(reducer: (state: State, action: Action) => State, filters: Array<Filter>): Store**
Creates a new Store with the given reducer function.

- **dispatch(action: Action): Promise<State>**
Dispatches an Action that the Store will respond to. Returns a Promise that resolves to the next state; this is useful for scheduling asynchronous actions.

- **subscribe(callback: (state: State): void): { dispose: () => void; }**
Adds a callback to be called whenever the Store's state changes. Returns a Disposable for removing the callback.
