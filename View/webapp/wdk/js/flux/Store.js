import invariant from 'react/lib/invariant';


/**
 * Creates a new Store object.
 *
 * This constructor allows us to define a Store as a reduce function, taking
 * the action being dispatched as input.
 *
 * The benefit of this approach is that the Store definition doesn't have to
 * worry about maintaing any internal state -- state is tracked by this
 * constructor.
 *
 * This also further enforces the idea that all application change should
 * happen through actions. By constraining the definition of a store to a
 * reduce function, there is no opportunity to trigger updates otherwise.
 * This is mostly due to the fact that we don't expose the dispatcher via the
 * constructor.
 *
 *
 * Both arguments are optional. If less then two arguments are provided, the
 * following rules are applied:
 *
 *   - If it is a function, it will be treated as `update` and `value` will
 *     be `undefined`.
 *
 *   - Otherwise, it will be treated as `value` and `update` will be a noop
 *     function.
 *
 * It follows that if no arguments are provided, `value` will be `undefined`
 * and `update` will be `noop`.
 *
 *
 * Examples:
 *
 * A store whose value increments by the amount defined in the action:
 *
 *    var countStore = createStore({
 *      initialize(handlers) {
 *        handlers.set(IncrementCount, this.increment);
 *        return 1;
 *      },
 *
 *      increment(count, action) {
 *        return count + action.incrementBy;
 *      }
 *    });
 *
 *    var countStore = createStore(1, function(value, action) {
 *      switch (action.type) {
 *        case IncrementBy:
 *          value += action.incrementBy;
 *          break;
 *      }
 *      return value;
 *    });
 *
 *
 * A store that includes a list of Todo items:
 *
 *    var todoStore = createStore({ todos: [] }, function(state, action) {
 *      if (action.type === TodoAdded) {
 *        state.todos.push(action.todo);
 *      }
 *      return state;
 *    });
 *
 *
 * A store that depends on another store:
 *
 *    var todoListStore = createStore({ lists: [] }, function(state, action, waitFor) {
 *      if (action.type === TodoSelected) {
 *        waitFor([ todoStore.dispatchToken ]);
 *        var list = action.todo.list;
 *        if (state.lists.indexOf(list) === -1) {
 *          state.lists.push(list);
 *        }
 *      }
 *      return state;
 *    });
 *
 * @param {any} [value] Initial value of store.
 * @param {function} [update] A reduce function to apply to store. If not
 *   provided, this will default to a noop function. If the provided function
 *   returns undefined, it will be treated as a noop. If you find yourself in
 *   need of representing an empty value, use null instead.
 */
function createStore(value, update) {
  invariant(
    arguments.length !== 0,
    'An initial value or an update function must be provided to `createStore`'
  );

  if (arguments.length < 2) {
    if (typeof value === 'function') {
      update = value;
      value = undefined;
    }
    else {
      update = noop;
    }
  }

  let dispatchToken;
  const observable = createObservable(value);

  return {
    register(dispatcher, context = {}) {
      invariant(
        dispatchToken === undefined,
        'A store can only be registered to one dispatcher.'
      );

      const waitFor = dispatcher.waitFor.bind(dispatcher);
      dispatchToken = dispatcher.register(function dispatcherCallback(action) {
        let value = observable.getValue();
        value = update(value, action, waitFor);
        if (value !== undefined) {
          observable.onNext(value);
        }
      });
    },

    unregister(dispatcher) {
      if (dispatchToken !== undefined) {
        dispatcher.unregister(dispatchToken);
        dispatchToken = undefined;
      }
    },

    subscribe(onNext) {
      return observable.subscribe(onNext);
    },

    get dispatchToken() {
      return dispatchToken;
    },

    get value() {
      return observable.getValue();
    }
  };
}

function createObservable(initialValue) {
  let value = initialValue;
  const callbacks = [];

  return {
    subscribe(callback) {
      if (value !== undefined) {
        callback(value);
      }
      callbacks.push(callback);
      return createDisposable(callback, callbacks);
    },

    onNext(newValue) {
      value = newValue;
      callbacks.forEach(callback => callback(value));
    },

    getValue() {
      return value;
    }
  };
}

function createDisposable(callback, callbacks) {
  return {
    dispose() {
      const index = callbacks.indexOf(callback);
      callbacks.splice(index, 1);
    }
  };
}

function noop() {}

export default {
  createStore
};
