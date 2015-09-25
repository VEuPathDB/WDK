/**
 * Utils related to timers and loops.
 */

export let requestAnimationFrame;
export let cancelAnimationFrame;

/** Normalize requestAnimationFrame functions */
(function() {
  requestAnimationFrame = window.requestAnimationFrame;
  cancelAnimationFrame = window.cancelAnimationFrame;

  let lastTime = 0;
  let vendors = ['webkit', 'moz'];
  for(let x = 0; x < vendors.length && !requestAnimationFrame; ++x) {
    requestAnimationFrame = window[vendors[x]+'RequestAnimationFrame'];
    cancelAnimationFrame =
      window[vendors[x]+'CancelAnimationFrame'] || window[vendors[x]+'CancelRequestAnimationFrame'];
  }

  if (!requestAnimationFrame)
    requestAnimationFrame = function(callback, element) {
      let currTime = new Date().getTime();
      let timeToCall = Math.max(0, 16 - (currTime - lastTime));
      let id = window.setTimeout(function() { callback(currTime + timeToCall); },
                                 timeToCall);
      lastTime = currTime + timeToCall;
      return id;
    };

    if (!cancelAnimationFrame)
      cancelAnimationFrame = function(id) {
        clearTimeout(id);
      };
}());


/**
 * Add and remove functions to be called using requestAnimationFrame.
 */
export class RequestLoop {

  constructor() {
    this._reqId = null;
    this._callbacks = [];
  }

  /**
   * Add a callback function to the list of callbacks. If this is the first
   * callback, it will cause the loop to start.
   *
   * @param {Function} callback
   */
  register(callback) {
    this._callbacks.push(callback);
    if (this._reqId == null) {
      this._reqId = requestAnimationFrame(() => this._loop());
    }
  }

  /**
   * Remove a callback function from the list of callbacks. If this is the last
   * callback, it will cause the loop to stop.
   *
   * @param {Function} callback
   */
  unregister(callback) {
    let index = this._callbacks.indexOf(callback);
    if (index > -1) this._callbacks.splice(index, 1);
    if (this._callbacks.length === 0) {
      cancelAnimationFrame(this._reqId);
      this._reqId = null;
    }
  }

  // Calls all callbacks in list and requests a new call
  _loop() {
    this._callbacks.forEach(callback => callback());
    this._reqId = requestAnimationFrame(() => this._loop());
  }

}
