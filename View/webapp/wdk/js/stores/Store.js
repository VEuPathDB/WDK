/**
 * Base class for Flux Stores.
 *
 * Example usage:
 *
 *     class MyStore extends Store {
 *
 *       getItems() {
 *         return this._items;
 *       }
 *
 *       handleDispatch(action) {
 *         switch (action) {
 *           case ActionTypes.MY_ACTION:
 *             // do things...
 *             this.emitChange();
 *             break;
 *         }
 *       }
 *
 *     }
 *
 *     module.exports = new MyStore();
 *
 */

var { EventEmitter } = require('events');
var dispatcher = require('../Dispatcher');


class Store {

  constructor() {
    this._emitter = new EventEmitter();
    this.dispatchToken = dispatcher.register(this.handleDispatch);
  }

  emitChange() {
    this._emitter.emit('change');
  }

  subscribe(callback) {
    this._emitter.on('change', callback);
  }

  unsubscribe(callback) {
    this._emitter.removeListener('change', callback);
  }

}


module.exports = Store;
