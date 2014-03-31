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

  var Fields = ns.Fields = Backbone.Collection.extend({
    model: Field
  });

});
