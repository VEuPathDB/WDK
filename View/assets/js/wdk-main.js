/*
 * This is the base configuration for requirejs.
 *
 *
 */

requirejs.config({
  baseUrl: "assets/js/lib",
  paths: {
    wdk: "../wdk"
  },
  shim: {
    "jqueryui": ["jquery"],
    "jquery.blockUI": ["jquery"],
    "jquery.cookie": ["jquery"],
    "jquery.form": ["jquery"],
    "jquery.html5-placeholder-shim": ["jquery"],
    "jquery.jstree": ["jquery"],
    "jquery.qtip": ["jquery"],
    "flexigrid": ["jquery"],
    "FixedColumns": ["jquery.dataTables"],
    "FixedHeader": ["jquery.dataTables"]
  }
});

require(["wdk/main"], function(wdk) {
});
