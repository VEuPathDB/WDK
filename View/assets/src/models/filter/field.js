wdk.namespace('wdk.models.filter', function(ns) {
  'use strict';

  var Field = ns.Field = Backbone.Model.extend({
    defaults: {
      filteredValues: []
    }
  });

  var Fields = ns.Fields = Backbone.Collection.extend({
    model: Field
  });

});
