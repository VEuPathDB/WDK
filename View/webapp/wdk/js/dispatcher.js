/* global _ */

/**
 * Flux Dispatcher for WDK
 * @module dispatcher
 */

var { Dispatcher } = require('flux');

/**
 * Singleton instance of dispatcher.
 *
 * See {@link http://facebook.github.io/flux/docs/dispatcher.html#content} for
 * full API.
 */
module.exports = new Dispatcher();
