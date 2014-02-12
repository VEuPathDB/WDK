wdk.namespace('wdk.models.filter', function(ns) {

  /**
   * Creates a string description of the filter.
   * Provides filter methods based on type (string or number).
   */
  var Filter = Backbone.Model.extend({
    template: _.template('<%= field %> is <%= condition %>'),

    toString: function() {
      return this.template({
        field: this.attributes.field.slice(0,1).toUpperCase() +
          this.attributes.field.slice(1),
        condition: _.result(this, 'condition')
      });
    }
  });

  /**
   * Filters data by set membership of an attribute
   */
  var MemberFilter = Filter.extend({
    condition: function() {
      var condition = this.attributes.values.slice(0,-1).join(', ');
      if (this.attributes.values.length > 1) {
        condition = 'either ' + condition + ' or ' +
          this.attributes.values.slice(-1);
      }
      return condition;
    }
  });

  /**
  * Filters data by range inclusion of an attribute
  */
  var RangeFilter = Filter.extend({
    condition: function() {
      var condition;
      var attrs = this.attributes;
      if (attrs.min != null && attrs.max != null) {
        condition = 'between ' + attrs.min + ' and ' + attrs.max;
      } else if (attrs.min != null) {
        condition = 'at least ' + attrs.min;
      } else if (attrs.max != null) {
        condition = 'at most ' + attrs.max;
      }
      return condition;
    }
  });

  ns.Filter = Filter;
  ns.MemberFilter = MemberFilter;
  ns.RangeFilter = RangeFilter;

});
