/**
 * Utilities for working with Promises.
 */

// A Promise that never leaves the pending state.
let pendingPromise = { then() { } };

/**
 * Given a function that returns a Promise, this will return a new
 * function that returns a Promise such that only the latest created
 * Promise will resolve or reject.
 *
 * A pattern where this is useful is if you are listening to events that are
 * fired repeatedly, but you only care about the latest event, such as a
 * keypress in an input box. Each keypress can invoke a function that
 * makes an ajax request and returns a Promise that resolves with the response.
 * By applying `latestPromise` to this function, there is no need to cancel the
 * previous requests, or to track which is the latest.
 *
 * @param {Function} promiseFactory A function that returns a Promise.
 * @returns {Function} A function that returns a Promise.
 */
export function latest(promiseFactory) {
  let latestPromise = null;
  return function createPromise(...args) {
    let thisPromise = latestPromise = promiseFactory(...args);
    return thisPromise.then(
      data => {
        if (thisPromise === latestPromise) {
          return data;
        }
        else {
          return pendingPromise;
        }
      },
      reason => {
        if (thisPromise === latestPromise) {
          throw reason;
        }
        else {
          return pendingPromise;
        }
      }
    );
  };
}
