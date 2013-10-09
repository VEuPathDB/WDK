//
// build up our angular app
//
// controller, directive, filter, and service callbacks
// are registered in their own file within the appropriate
// directory. ./*/index.js will create the depended module.

require('./controllers');
require('./directives');
require('./filters');
require('./services');

// create application module with dependecies
angular.module('wdk',[
  'wdk.controllers',
  'wdk.directives',
  'wdk.filters',
  'wdk.services'
]);

// bootstrap the wdk module
angular.element(document).ready(function() {
  angular.bootstrap(document, ['wdk']);
});

