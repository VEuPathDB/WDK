var $ = window.jQuery;

/**
 * Methods to interface with the WDK REST API.
 */

export default function ServiceAPI(serviceUrl) {

  if (typeof serviceUrl !== 'string') {
    throw new TypeError(`serviceUrl ${serviceUrl} must be a string.`);
  }

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
   * @param {string} resourcePath The service path to a resource, e.g. '/answer'
   * @param {object} [data] Data to include in request. For GET, these will be
   * added to the URL as query params. For all others, it will be included as
   * raw JSON in the body.
   * @returns {Promise} The returned Promise will resolve with the response, or
   * reject with the error string.
   */
  function requestResource(type, resourcePath, data) {
    if (arguments.length === 0) return requestResource;
    if (arguments.length === 1) return requestResource.bind(null, type);
    return new Promise((resolve, reject) => {
      $.ajax({
        type,
        url: serviceUrl + resourcePath,
        contentType: 'application/json',
        dataType: 'json',
        data: type === 'GET' ? data : JSON.stringify(data)
      })
      .then(function(data) {
        resolve(data);
      }, function(jqXHR, textStatus, error) {
        reject(error);
      });
    });
  }

  return {
    requestResource:  requestResource,
    getResource:      requestResource('GET'),
    postResource:     requestResource('POST'),
    putResource:      requestResource('PUT'),
    deleteResource:   requestResource('DELETE'),
    patchResource:    requestResource('PATCH'),
  };
}
