import invariant from 'react/lib/invariant';

/**
 * Creates a new Store object with an interface similar to Rx.Subject.
 *
 * This class allows us to define a Store as a reduce function, taking
 * the action being dispatched as input.
 *
 * The benefit of this approach is that the Store definition doesn't have to
 * worry about maintaing any internal state -- state is tracked internally.
 * This will make is possible to move to a single state model in the future if
 * we decide. A reason we may decide to do this is so that we can include a
 * user's current application state in bug reports.
 *
 * This also further enforces the idea that all application change should
 * happen through actions. By constraining the definition of a store to a
 * reduce function, there is no opportunity to trigger updates otherwise.
 *
 *
 * Examples:
 *
 * A store whose value increments by the amount defined in the action:
 *
 *    var countStore = new Store(
 *      dispatcher,
 *      1,
 *      function update(value, action) {
 *        switch (action.type) {
 *          case IncrementBy:
 *            value += action.incrementBy;
 *            break;
 *        }
 *        return value;
 *      }
 *    );
 *
 *
 * A store that includes a list of Todo items:
 *
 *    var todoStore = new Store(
 *      dispatcher,
 *      { todos: [] },
 *      function update(state, action) {
 *        if (action.type === TodoAdded) {
 *          state.todos.push(action.todo);
 *        }
 *        return state;
 *      }
 *    );
 *
 *
 * A store that depends on another store:
 *
 *    var todoListStore = new Store(
 *      dispatcher,
 *      { lists: [] },
 *      function update(state, action) {
 *        dispatcher.waitFor(todoStore.dispatchToken);
 *        if (action.type === TodoSelected) {
 *          var list = action.todo.list;
 *          if (state.lists.indexOf(list) === -1) {
 *            state.lists.push(list);
 *          }
 *        }
 *        return state;
 *      }
 *    );
 *
 * @param {object} dispatcher Application dispatcher.
 * @param {any} initialValue Initial value of the store.
 * @param {function} update A reduce function to apply to store. If the provided
 *   function returns undefined for an action, it will be treated as a noop. In
 *   this case, subscribers will not be notified of a change. If you find
 *   yourself in need of representing an empty value, use null instead.
 */
export default class Store {

  constructor(dispatcher, initialValue, update) {
    invariant(
      typeof update === 'function',
      'Store %s must implement the method `update`',
      this.name || this
    );

    this._callbacks = [];

    this._value = initialValue;

    this._dispatchToken = dispatcher.register(action => {
      const newValue = update(this._value, action);
      if (newValue !== undefined) {
        this._value = newValue;
        this._callbacks.forEach(notify, this);
      }
    });

  }

  subscribe(callback) {
    this._callbacks.push(callback);
    if (this._value !== undefined) callback(this._value);
    return { dispose: dispose.bind(this, callback) };
  }

  get dispatchToken() {
    return this._dispatchToken;
  }

  get value() {
    return this._value;
  }

}

// Called with application as context.
// E.g., getStoreValue.call(application, type);
function getStoreValue(Class) {
  const store = this.getStore(Class);
  this.dispatcher.waitFor(store.dispatchToken);
  return store.value;
}

function notify(callback) {
  callback(this._value);
}

function dispose(callback) {
  const index = this._callbacks.findIndex(callback);
  this._callbacks.splice(index, 1);
};
