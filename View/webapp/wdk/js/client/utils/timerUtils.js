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
 * Holds a list of callback functions that are invoked repeatedly with a fixed
 * time delay between each call, as provided to the constructor. The default is
 * 200ms. The interval will stop when the list is empty and resume when it is
 * not empty.
 */
export class IntervalList {

  /**
   * @param {number} interval Time in ms.
   */
  constructor(interval = 200) {
    this._interval = interval;
    this._callbacks = [];
    this._id = null;
  }

  /**
   * Add a callback to the list. If the list was empty before this action,
   * the interval will be started.
   *
   * @param {Function} callback
   */
  add(callback) {
    this._callbacks.push(callback);
    if (this._id == null) {
      this.start();
    }
  }

  /**
   * Remove a callback from the list. If this action results in an empty list,
   * the interval will be stopped.
   *
   * @param {Function} callback
   */
  remove(callback) {
    let index = this._callbacks(callback);
    if (index < 0) return false;
    this._callbacks.splice(index, 1);
    if (this._callbacks.length === 0) {
      this.stop();
    }
  }

  isRunning() {
    return this._id != null;
  }

  /**
   * Start the interval.
   */
  start() {
    if (this.isRunning()) {
      throw new Error("Attempting to start an interval that is already running.");
    }

    let loop = () => {
      this._id = setTimeout(() => {
        this._callbacks.forEach(invoke);
        loop();
      }, this._interval);
    };

    loop();

  }

  /**
   * Stop the interval.
   */
  stop() {
    if (!this.isRunning()) {
      throw new Error("Attemping to stop an interval that is already stopped.");
    }
    this.clearTimeout(this._id);
    this._id = null;
  }

}

function invoke(fn) { fn(); }
