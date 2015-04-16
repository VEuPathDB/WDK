import EventEmitter from 'events';
import warnInvariant from './utils/warnInvariant';

/**
 * Base class for a Flux Store.
 *
 * This base class handles creating the callback used with the Dispatcher.
 * Derived classes can register callbacks for specific actions within the
 * #init() method hook (see example below).
 *
 * The point of this class is to encapsulate the internals of the application
 * from derived classes so that we can more seamlessly transition to reactive
 * components, if we decide to.
 *
 * Currently, all aspects of the original Flux architecture are supported,
 * including handling store dependencies via `waitFor`. The main difference
 * is that derived classes do not need to worry about `emitChange` since this
 * class handles that. Furthermore, this class provides a method to create an
 * obervable proxy (#asObservable()) that aproximated Rx.Observable semantics.
 *
 *
 * __EXAMPLE__
 *
 * A derived class that updates it's state when an action with type
 * SOME_ACTION_TYPE is dispatched. Note the lack of the cumbersome switch
 * statement, and the lack of explicit call to emitChange.
 *
 *     class MyStore extends Store {
 *       init() {
 *         this.state = { value: 0 };
 *         this.handleAction(SOME_ACTION_TYPE, this.updateValue);
 *       }
 *
 *       updateValue(action) {
 *         this.state.value = action.value;
 *       }
 *     }
 */
export default class Store {

  /**
   * Basic internal housekeeping and initialization of properties.
   *
   * Call derived class's #init() (defaults to noop... should log warning?)
   * and register a callback function with dispatcher.
   *
   * @param {Dispatcher} dispatcher Application dispatcher.
   * @param {Application} application Application context.
   * @constructor
   */
  constructor(dispatcher, application) {
    this._dispatcher = dispatcher;
    this._application = application;
    this._methods = new Map();
    this._emitter = new EventEmitter();
    this.init();
    warnInvariant(
      this.state !== undefined,
      'state was not defined during init(). Check the definition of %s.',
      this.constructor.name
    );
    this.dispatchToken = dispatcher.register(this._dispatchHandler.bind(this));
  }

  /**
   * Callback used with dispatcher. This method will lookup any methods
   * registered with #handleAction. If one is found, it will be called with the
   * action that was dispatched. It will then notify any listeners with the new
   * state.
   *
   * TODO If class declares it's state as immutable, do an equality check to
   *      decide of listeners should be notified.
   */
  _dispatchHandler(action) {
    const method = this._methods.get(action.type);
    if (method === undefined) return;

    if (method.waitFor) {
      const tokens = method.waitFor.map(storeClass => {
        const store = this._application.get(storeClass)
        if (store === undefined) {
          throw Error('Could not find Store', storeClass);
        }
        return store.dispatchToken;
      });
      this._dispatcher.waitFor(...storeClasses);
    }

    method.call(this, action);
    this._emitter.emit('change', this.state);
  }

  /**
   * Register a callback method to be called when an action whose type is
   * `actionType` is dispatched. This method should be called by derived
   * classes within the #init() method hook.
   *
   * @param {string} actionType The type of action.
   * @param {method} method The callback method that will be called in the
   *     context of the constructed object.
   */
  handleAction(actionType, method) {
    this._methods.set(actionType, method);
  }


  /**
   * Create an observable interface to be used by Components. This prevents
   * access to methods that may mutate state, such as those called by the
   * dispatch handler.
   *
   * This loosely follows the Rx.Observable API. An eventual goal may be to
   * create stores as Rx.Observables.
   * See https://github.com/Reactive-Extensions/RxJS/blob/master/doc/api/core/operators/asobservable.md
   * and https://github.com/Reactive-Extensions/RxJS/blob/master/doc/api/core/operators/subscribe.md
   *
   *
   * Example usage:
   *
   *     var store = new Store();
   *     var observableStore = store.asObservable();
   *     var subscription = observableStore.subscribe(function(state) {
   *       component.setState(state);
   *     });
   *
   *     ... some time later ...
   *
   *     subscription.dispose();
   *
   * Since we call `callback` immediately within subscribe, we don't need to
   * add a getter to access the current state of the store.
   *
   * `subscribe` returns a disposable object (see below).
   */
  asObservable() {
    return createObservable(this);
  }


  /**
   * Helper decorator for callback methods passed to #handleAction().
   * When found, the dispatch handler will pass listed stores as addition
   * parameters to the method.
   *
   * TODO Implement passing stores in dispatcher. This requires the application
   * container to be implemented properly.
   *
   * TODO Enable decorators. For now, this must be used manually.
   *
   * Example usage where we want to wait for OtherStore to handle SOME_ACTION:
   *
   *     // without decorators
   *     class MyStore extends Store {
   *
   *       init() {
   *         this.state = {};
   *         this.handleAction(SOME_ACTION, Store.waitFor(this.update, OtherStore));
   *       }
   *
   *       update(action, otherStore) {
   *         ...
   *       }
   *
   *     }
   *
   *     // with decorators
   *     class MyStore extends Store {
   *
   *       init() {
   *         this.state = {};
   *         this.handleAction(SOME_ACTION, this.udpate);
   *       }
   *
   *       @Store.waitFor(OtherStore);
   *       udpate(action, otherStore) {
   *         ...
   *       }
   *
   *     }
   *
   */
  static waitFor(method, ...Stores) {
    method.waitFor = Stores;
    return method;
  }

  // template method hooks

  init() {
    warnInvariant(
      false,
      'Store did not implement an init() method. Check the definition of %s.',
      this.constructor.name
    );
  }

}


// Creates an observable proxy object to the store.
function createObservable(store) {
  return {
    subscribe(callback) {
      store._emitter.on('change', callback);
      callback(store.state);
      return createDisposable(store, callback);
    }
  };
}

// This will provide a method to remove a callback function
// from a store. This is loosely based on Rx.Observables.
function createDisposable(store, callback) {
  return {
    dispose() {
      store._emitter.removeListener('change', callback);
    }
  };
}
