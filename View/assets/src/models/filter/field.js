wdk.namespace('wdk.models.filter', function(ns) {
  'use strict';

  var Field = ns.Field = Backbone.Model.extend({
    idAttribute: 'term',

    defaults: {
      filteredValues: []
    },

    select: function() {
      this.trigger('select', this);
    }
  });

  Field.UNKNOWN_VALUE = new String('Unknown');

  var Fields = ns.Fields = Backbone.Collection.extend({
    model: Field
  });

});
