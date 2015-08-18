import invariant from 'react/lib/invariant';

var $ = window.jQuery;

/**
 * Functions to interface with the WDK REST API. This module mainly
 * encapsulates header and formatting details of requests.
 */

function create(endpoint) {
  /**
   * Request a resource from the WDK Service.
   *
   * This function has curry2 applied to it. This means that if only one param
   * is passed, it will return a partially applied function. This is used to
   * create the {type}Request methods below. If two or more params are passed,
   * the function will be invoked.
   *
   * @param {string} type Can be one of 'GET', 'POST', 'PUT', 'PATCH', or
   * 'DELETE'
   * @param {string} resourcePath The service path to a resource, e.g. '/serviceEndpoint/answer'
   * @param {object} [data] Data to include in request. For GET, these will be
   * added to the URL as query params. For all others, it will be included as
   * raw JSON in the body.
   * @returns {Promise} The returned Promise will resolve with the response, or
   * reject with the error string.
   */
  function requestResource(type, resourcePath, data) {
    if (arguments.length === 0) return requestResource;
    if (arguments.length === 1) return requestResource.bind(null, type);
    let promise = new Promise(function resourcePromise(resolve, reject) {
      $.ajax({
        type,
        url: endpoint + resourcePath,
        contentType: 'application/json',
        dataType: 'json',
        data: type === 'GET' ? data : JSON.stringify(data),
        success: function resolvePromise(data) {
          resolve(data);
        },
        error: function rejectPromise(jqXHR, textStatus, error) {
          reject(error);
        }
      });
    });
    promise.catch(function(error) {
      console.error('Uncaught', error);
    });
    return promise;
  }

  return {
    requestResource:  requestResource,
    getResource:      requestResource('GET'),
    postResource:     requestResource('POST'),
    putResource:      requestResource('PUT'),
    deleteResource:   requestResource('DELETE'),
    patchResource:    requestResource('PATCH')
  };
}

export default { create };
