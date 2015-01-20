"use strict";

/* global $, _ */

/**
 * Functions to interface with the WDK REST API.
 */

var serviceUrl;

export function config(spec) {
  serviceUrl = spec.serviceUrl;
}

/**
 * The order of the args allows us to create functions like
 *
 *     var getRecords = _.partial(request, 'GET', '/records');
 *
 */
export function requestResource(type, resourcePath, data) {
  return new Promise(function(resolve, reject) {
    $.ajax({
      type,
      url: serviceUrl + resourcePath,
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

export var getResource = _.partial(requestResource, 'GET');
export var postResource = _.partial(requestResource, 'POST');
export var putResource = _.partial(requestResource, 'PUT');
export var patchResource = _.partial(requestResource, 'PATCH');
export var deleteResource = _.partial(requestResource, 'DELETE');
