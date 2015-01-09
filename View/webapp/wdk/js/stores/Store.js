import EventEmitter from 'events';
import Dispatcher from '../Dispatcher';


/**
 * Create a Store.
 *
 * The `spec` argument should contain two methods:
 *
 *   - dispatchHandler(action, emitChange)
 *     1. action: The action currently being dispatched.
 *     2. emitChange: A function to emit the change event.
 *
 *   - getState()
 *     Return the state of the Store. Store.prototype.getState delegates to this
 *     method. This method is also called to provide context to event listeners.
 *
 *
 * Example usage:
 *
 *     var items = [];
 *
 *     module.exports = new Store({
 *
 *       dispatchHandler(action, emitChange) {
 *         switch (action) {
 *           case ActionTypes.MY_ACTION:
 *             // do things to items
 *             emitChange();
 *             break;
 *         }
 *       },
 *
 *       getState() {
 *         return items;
 *       }
 *
 *     });
 *
 *
 * External API:
 *
 * A Store instance will have three methods:
 *
 *   - subscribe(callback)
 *     callback will be called with the result of getState() when emitChange() is called.
 *
 *   - unsubscribe(callback)
 *     callback will be removed from internal list of callbacks when emitChange() is called.
 *
 *   - getState()
 *     Returns the state of the Store, as specified by the producer (see `spec` properties above).
 *
 *
 * Notably, this class makes use of the "revealing constructor pattern" for better encapsulation.
 * (see https://blog.domenic.me/the-revealing-constructor-pattern/)
 *
 * @param {object} spec
 */

export default class Store {
  constructor(spec) {
    _.assign(this, createStore(spec));
  }
}

function createStore(spec) {
  var { dispatchHandler, getState } = spec;
  ensureFunction(dispatchHandler, "dispatchHandler");
  ensureFunction(getState, "getState");

  var emitter = new EventEmitter();
  Dispatcher.register(action => {
    dispatchHandler(action, function emitChange() {
      emitter.emit('change', getState());
    });
  });

  return {
    getState,
    subscribe(callback) {
      emitter.on('change', callback);
    },
    unsubscribe(callback) {
      emitter.removeListener('change', callback);
    }
  };
}

function ensureFunction(fn, name) {
  var message = "Store " + name + " " + fn + " is not a function";
  if (typeof fn !== 'function') throw new TypeError(message);
}
