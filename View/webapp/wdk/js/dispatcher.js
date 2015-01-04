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
module.exports = _.assign(new Dispatcher(), {
  handleServerAction: function(action) {
    var payload = { source: 'SERVER_ACTION', action };
    this.dispatch(payload);
  },

  handleViewAction: function(action) {
    var payload = { source: 'VIEW_ACTION', action };
    this.dispatch(payload);
  }
});
