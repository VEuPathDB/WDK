/* global Spinner */

// TODO Use customElement API (not supported by IE8)

// FIXME Move visible logic to a utility function and remove immediate attribute flag.
// Instead, use add a manual flag: when true, don't load ajax immediately. Defaults to false.

wdk.namespace('wdk.components.ajaxElement', function(ns, $) {
  'use strict';

  var elementNearViewport = wdk.util.elementNearViewport;

  // we do this for IE8 - see https://docs.angularjs.org/guide/ie
  document.createElement('wdk-ajax');

  // load the resource at @url via ajax
  function loadUrl(el) {
    var spinner = new Spinner().spin();
    el.appendChild(spinner.el);
    el.setAttribute('triggered', true);
    $(el).load(el.getAttribute('url'));
  }

  // scan DOM for <wdk-ajax> elements and call load
  function triggerElements($el) {
    $el.find('wdk-ajax:not([triggered])')
      // load if [immediate]
      .filter('[immediate]')
        .each(function(i, e) { loadUrl(e); })
        .end()
      // load if :visible and in viewport
      .not('[immediate]')
        .filter(':visible')
        .filter(function(i, e) { return elementNearViewport(e); })
        .each(function(i, e) { loadUrl(e); });
  }

  ns = _.extend(ns, {
    loadUrl: loadUrl,
    triggerElements: triggerElements
  });
});
