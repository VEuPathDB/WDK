/* global Spinner */

// TODO Use customElement API (not supported by IE8)

wdk.namespace('wdk.components.wdkAjax', function(ns, $) {
  'use strict';

  // we do this for IE8 - see https://docs.angularjs.org/guide/ie
  document.createElement('wdk-ajax');

  // Load the resource at @url via ajax
  // Returns a jQuery Deferred object which resolves with the element and jqXHR
  // object, when the remote content is added to the DOM.
  //
  //
  // EXAMPLE
  //
  //     load(myElement).then(function(el, jqXHR) {
  //       ...
  //     });
  //
  function load(el) {
    var spinner = new Spinner().spin();
    var deferred = $.Deferred();

    el.appendChild(spinner.el);
    el.setAttribute('triggered', true);
    $(el).load(el.getAttribute('url'), function(jqXHR) {
      deferred.resolve(el, jqXHR);
    });

    return deferred;
  }

  // scan DOM for <wdk-ajax> elements and call load
  // unless attribute manual is present
  function init($root) {
    $root.find('wdk-ajax:not([triggered])')
      .filter(':not([manual])')
      .each(function(i, e) { load(e); });
  }

  ns = _.extend(ns, {
    load: load,
    init: init
  });
});
