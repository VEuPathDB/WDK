/* global $ */

/**
 * Methods to interface with the WDK REST API.
 *
 * @class ServiceAPI
 */

export default class ServiceAPI {

  constructor(serviceUrl) {
    if (typeof serviceUrl !== 'string') {
      throw new TypeError(`serviceUrl ${serviceUrl} must be a string.`);
    }
    this.serviceUrl = serviceUrl;
  }

  /**
   * Request a resource from the WDK Service.
   *
   * @param {string} type Can be one of 'GET', 'POST', 'PUT', 'PATCH', or
   * 'DELETE'
   * @param {string} resourcePath The service path to a resource, e.g. '/answer'
   * @param {object} data Data to include in request. For GET, these will be
   * added to the URL as query params. For all others, it will be included as
   * raw JSON in the body.
   * @returns {Promise} The returned Promise will resolve with the response, or
   * reject with the error string.
   */
  requestResource(type, resourcePath, data) {
    return new Promise((resolve, reject) => {
      $.ajax({
        type,
        url: this.serviceUrl + resourcePath,
        contentType: 'application/json',
        dataType: 'json',
        data: type === 'GET' ? data : JSON.stringify(data)
      }).then(function(data) {
          resolve(data);
        }, function(jqXHR, textStatus, error) {
          reject(error);
        });
    });
  }

  /** see requestResource */
  getResource(...args) {
    return this.requestResource('GET', ...args);
  }

  /** see requestResource */
  postResource(...args) {
    return this.requestResource('POST', ...args);
  }

  /** see requestResource */
  putResource(...args) {
    return this.requestResource('PUT', ...args);
  }

  /** see requestResource */
  patchResource(...args) {
    return this.requestResource('PATCH', ...args);
  }

  /** see requestResource */
  deleteResource(...args) {
    return this.requestResource('DELETE', ...args);
  }

}
