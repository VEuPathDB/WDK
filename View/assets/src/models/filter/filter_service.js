wdk.namespace('wdk.models.filter', function(ns) {

  var Filter = wdk.models.filter.Filter;
  var MemberFilter = wdk.models.filter.MemberFilter;
  var RangeFilter = wdk.models.filter.RangeFilter;

  var Filters = Backbone.Collection.extend({
    model: function(attrs, options) {
      if (attrs.operation === 'equals') {
        return new MemberFilter(attrs, options);
      } else if (attrs.operation === 'between') {
        return new RangeFilter(attrs, options);
      } else {
        throw new TypeError('Unkown operation: "' + attrs.operation + '". ' +
          'Supported operations are "equals" and "between"');
      }
    }
  });

  /**
   * Base class for FilterService classes.
   *
   * A filter service should only need to implement an
   * `applyFilters` method. This method's job is to apply
   * added filters to the data.
   * 
   */
  var FilterService = Backbone.Model.extend({

    defaults: {
      fields: [],
      data: []
    },

    /**
     * Filter collection
     */
    filters: null,

    initialize: function() {
      this.unfilteredData = _.clone(this.attributes.data);
      this.filters = new Filters;
    }
  });

  var LocalFilterService = FilterService.extend({

    applyFilters: function() {
      var data = this.unfilteredData;
      var service = this;

      this.filters.forEach(function(filter) {
        if (filter instanceof MemberFilter) {
          data = service._applyMemberFilter(filter, data);
        } else if (filter instanceof RangeFilter) {
          data = service._applyRangeFilter(filter, data);
        }
      });

      this.set('data', data);
    },

    _applyMemberFilter: function(filter, data) {
      var field = filter.get('field');
      var values = filter.get('values');
      return data.filter(function(d) {
        return _.contains(values, d.metadata[field]);
      });
    },

    _applyRangeFilter: function(filter, data) {
      var field = filter.get('field');
      var min = filter.get('min');
      var max = filter.get('max');
      var test;

      if (min != null && max != null) {
        test = function(d) {
          return d.metadata[field] >= min && d.metadata[field] <= max;
        };
      } else if (min != null) {
        test = function(d) {
          return d.metadata[field] >= min;
        };
      } else if (max != null) {
        test = function(d) {
          return d.metadata[field] <= max;
        };
      }

      return data.filter(test);
    }

  });

  ns.FilterService = FilterService;
  ns.LocalFilterService = LocalFilterService;

});
