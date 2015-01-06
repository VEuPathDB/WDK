/* global $, _ */

/**
 * Functions to interface with the WDK REST API.
 */

// FIXME Determine base URL
var serviceBaseUrl = '/service/';

/**
 * The order of the args allows us to create functions like
 *
 *     var getRecords = _.partial(request, 'GET', '/records');
 *
 */
function request(type, url, data) {
  var contentType = 'applicaton/json;',
      dataType = 'json';
  url = service + url;

  // FIXME Wrap jQuery deferred in Promise
  return $.ajax({ type, url, data, contentType, dataType });
}

// TODO It might be nice to expose the exact REST API available here. For
// example, if we only support GET (by POST) for /answer, then we can export
//
//     exports.getAnswer = _.partial('POST', '/answer');
//
// which can be used like this
//
//     getAnswer({ questionName: 'MyQuestion', params: { param1: 'value1' }})
//       .then(function(answer) { /* ... */ });
//
// For now, we will leave things like this up to the ActionCreators.

exports.request = request;
exports.get = _.partial(request, 'GET');
exports.post = _.partial(request, 'POST');
exports.put = _.partial(request, 'PUT');
exports.patch = _.partial(request, 'PATCH');
exports.delete = _.partial(request, 'DELETE');
