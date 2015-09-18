# Architectural Overview

The WDK client employs the Flux architecture, as described at https://facebook.github.io/flux/docs/overview.html#content.


Note: This document uses Facebook's
[Flow type annotations](http://flowtype.org/docs/quick-reference.html) and
[ES6 syntax](https://babeljs.io/docs/learn-es2015/). The reader is encouraged to
familiarize themselves with these syntaxes.


# Simple Flux Overview

The Flux architecture prescribes a unidirectional data flow, as depicted in the
following diagram:

                    Store --> View Controller --> Action --> Dispatcher
                    ^                                                 '
                    '-------------------------------------------------'


The Flux architecture contains these Entities:

## Dispatcher
Notifies Stores of Actions.

## Stores
Maintain and modify application state based on Actions.

## Actions
Message that describes a requested change to the application.

## View Controllers
Manages dispatching Actions and observing changes to Stores, and rendering Views based on these changes.

## Views
Renders a User Interface based on current application state, and sends user interactions to parent Controller Views.


# WDK Implementation Details

WDK's implementation differs slightly from what is described above:

  * WDK uses a single Store for the application state.
  * This single Store exposes a dispatch method that takes the place of the
    Dispatcher.
  * The application state is treated as an immutable tree.

WDK also adds a Router, whose job is to render a View Controller, passing the
View Controller information about the URL. The Router does this when the page
is initialized, and every time the URL changes.


In WDK, the diagram above looks more like:


                                          Router
                                            |
                                            | (renders with URL info)
                                            v


                                      View Controller
                                           |  ^
                      (sends Action via    |  |   (observes via
                       store.dispatch)     v  |    store.subscribe)


                                          Store


_The fact that arrows are touching some entities and not others is important.
This indicates where the relationship between two entities is defined. For
example, a View Controller defines both relationships with the Store._


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
  3. The Router allows some portion of the application state to be encoded in a
     URL. The View Controller manages decoding the URL into Actions, which the
     Store will commit to its state tree.


## Store

As mentioned above, the WDK Client contains a single Store. The Store is defined
by a single reduce function of the type:

    function reduce<State>(state: State, action: Action): State { ... }

[Syntax explanation](http://flowtype.org/docs/functions.html#polymorphic-functions)

In other words, for every Action that is dispatched, this function will return
a new State based on the old State. The Store holds a reference to the current
State, and it notifies observers when the State has changed due to a dispatch
call.


### Immutable state

The state object of the Store can be thought of as an immutable tree. What this
means is, whenever a part of the state object is change, a new object will be
created, thus the previous state and the new state will fail simple equality
checks. This fact makes it trivial to determine if the state has changed based
on an action being dispatched: all one has to do is perform a simple equality
check:

    if (oldState !== newState) {
      // state is different
    }

Furthermore, if a branch of the tree has no changes, it will retain the previous
reference. Thus, these equality checks can be performed deep within the tree.

To better visualize this, consider the following tree:

                A
               / \
              B   C
             / \   \
            D   E   F

and assume an action is dispatched that changes the value of E. Our new tree
will now look like this (where lowercase indicates a new reference):

                a
               / \
              b   C
             / \   \
            D   e   F

`C`, `D`, and `F` will retain their references, this the following statement
will be true (for brevity, we will assume the keys of the state object are
all lowercase):

    (oldState.b.d === newState.b.d) // => true


What follows from these facts is that our rendering code can be dramatically
simplified to avoid unnecessary re-renders or other expensive operations. A
caveat to this is that we must be careful in our reducers to always return
new objects when something changes. The justification is that both detecting
change and communicating change are complicated tasks. The trade-off is where to
put that complexity. By putting it in the reducers, our React Components can
focus on handling user interactions and rendering views.


### State reducers

The reduce function itself is composed of other reduce functions that operate on
a subset of the application state.

The organization of the reducers mirrors the organization of the tree. This
is by design to

  1. Make it easy to associate a part of the application state with a reducer.
  2. Make it easy to rearrange the state with minimal code rewriting.


The following examples use Flow type and ES6 syntax:

  * [Object types](http://flowtype.org/docs/objects.html)
  * [Array types](http://flowtype.org/docs/arrays.html)
  * [Enhanced Object Literals](https://babeljs.io/docs/learn-es2015/#enhanced-object-literals)
  * [Default + Rest + Spread](https://babeljs.io/docs/learn-es2015/#default-rest-spread)
  * [Let + Const](https://babeljs.io/docs/learn-es2015/#let-const)


For example, if our state has the following shape:

    State = {
      records: Array<Record>;
      questions: Array<Question>;
    }

we can construct two reducers, one for each top-level key (records and questions):

    function records(records = [], action) {
      switch (action.type) {
        case 'ADD_RECORD':
          return [ ...records, action.record ];
        case 'REMOVE_RECORD':
          return records.filter(function(r) {
            return r !== action.record;
          });
      }
    }

    function questions(questions = [], action) {
      switch (action.type) {
        case 'ADD_QUESTIONS':
          return [ ...questions, action.question ];
        case 'REMOVE_QUESTIONS':
          return questions.filter(function(q) {
            return q !== action.question;
          });
      }
    }

and our top-level state reducer can delegate to these:

    function reducer(state, action) {
      let recordsState = records(state.records, action);
      if (recordsState !== state.records) {
        state = Object.assign({}, state, {
          records: recordsState;
        });
      }

      let questionsState = questions(state.questions, action);
      if (questionsState !== state.questions) {
        state = Object.assign({}, state, {
          questions: questionsState;
        });
      }

      return state;
    }

Luckily, WDK provides a utility that makes defining a reducer like the one above
cleaner and less error-prone. It takes an object whose values are reducer
functions. It then uses the associated keys to construct an object with the same
keys, whose values are determined by the result of the associated reducer
function.

    let reducer = combineReducers({
      questions,
      records
    });

`combineReducers` will return a function just like the one we manually defined
above. Best of all, `combineReducers` can easily be composed to allow grouping
reducers into nested categories:

    // Create a reducer for view related state.
    let views = combineReducers({
      recordView,
      questionView
    });

    // Create a reducer for resource relate state.
    let resources = combineReducers({
      records,
      questions
    });

    // Combine view and resource related state into a single tree
    let reducer = combineReducers({
      views,
      resources
    });

and the resulting state shape will be:

    State = {
      views: {
        recordView: Object;
        quesitonView: Object;
      },
      resources: {
        records: Array<Records>;
        questions: Array<Questions>;
      }
    }


### Initial state

Another piece of the story involves defining initial state. When a Store is
first created, it will call the dispatch function with `undefined` as the state
argument, and a meaningless action. The purpose of this is for each reducer to
return some initial value. If you notice in the example reducer functions above,
there is an assignment syntax in the function arguments list. This is ES6 syntax
for default function argument assignment. In other words, if that argument is
undefined when called, the value on the RHS of the equal sign will be used. This
syntax allows for a terse expression of a reducer's default value. For the most
part, an initial value in a reducer will be something like an empty array, an
empty object, etc.


### State selectors

A companion to the so-called state reducers are state selectors. A state selector
is a function which selects discontinuous parts of the application state, and
rearranges them into a new object. A selector function has the following type:

    (state: State) => any

Since the application state is immutable, state selectors can be memoized. This
has the benefit of minimizing expensive object-lookup calls, and it will result
in fewer render() calls in React.

With a little bit of wiring, we can provide a general utility that wraps a
selector and hooks into React Component life cycle methods to automatically call
`setState`, leveraging all of the performance benefits of immutability with
little effort. It might look something like this:

    let ViewController = React.createClass({

      mixins: [ SelectorMixin ],

      selectState(state) {
        let record = state.resources.records[this.props.recordId];
        let { visibleCategories } = this.state.views.recordView;

        return {
          record,
          visibleCategories
        };
      },

      render() {
        return (
          <Record
            record={this.state.record}
            visibleCategories={this.state.visibleCategories}
          />
        );
      }

    });


## Dispatch Filters

WDK provides an API for enhancing the Store's base dispatch method via dispatch
filters. The primary purpose of dispatch filters is to encourage separating
dispatch functionality into composable pieces. The API provides a lot of
flexibility without encumbering the main logic of the Store's dispatch method.

A dispatch filter is simply a function or a method. It has the type:

    (store: Store, next: (action: any), action: any) => any?

where `next` is the next filter in the chain, and `action` is the action being
dispatched. A filter can do some work, synchronous or asynchronous, and then
call `next(action)` when done. `action` can be what is passed to the filter, or
it can be some derived value. The return type of a filter is optional, and can be anything.

A dispatch filter can also cancel a dispatch call, by not calling `next`. This
can be useful, for instance, in the case when we have already fetched a resource
from an external service.

Dispatch filters are passed to the Store's constructor as an array. When the
resulting dispatch method is called, each filter is applied from left to right,
ultimately calling the Store's base dispatch function (if it makes it that far).


A simple example of a logging filter is shown below. It simply logs the action
being dispatched and the state as a result of that action.

    function loggingFilter(store, next, action) {
      console.log('dispatching', action);
      let result = next(action);
      console.log('next state', store.getState());
      return result;
    }


A slightly more complicated example is a filter that returns a Promise. This
allows the caller of dispatch to await the resolution of a dispatch call.

    function promiseFilter(store, next, action) {
      return Promise.resolve().then(next(action));
    }


# API


## Types

- **State**
`Object`

- **Action**
`{ type: string; ... }`

- **Filter**
`(store: Store, next: (action: Action) => any?, action: Action) => any?`


## Store

- **create(reducer: (state: State, action: Action) => State, filters: Array<Filter>): Store**
Creates a new Store with the given reducer function.

- **dispatch(action: Action) => State**
Dispatches an Action that the Store will respond to. Returns a Promise that resolves to the next state; this is useful for scheduling asynchronous actions.

- **subscribe(callback: (state: State): void): { dispose: () => void; }**
Adds a callback to be called whenever the Store's state changes. Returns a Disposable for removing the callback.
