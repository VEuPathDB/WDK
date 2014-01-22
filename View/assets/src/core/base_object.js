wdk.namespace('wdk.core', function(ns) {
  'use strict';

  var create = wdk.core.c_properties.create,
      extend = wdk.core.c_properties.extend;

  var BaseObject = function () {
  }

  BaseObject.create = create;

  BaseObject.extend = extend;

  ns.BaseObject = BaseObject;

});
