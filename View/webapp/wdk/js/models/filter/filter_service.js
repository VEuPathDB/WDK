/*global RSVP*/
wdk.namespace('wdk.models.filter', function(ns) {
  'use strict';

  var MemberFilter = wdk.models.filter.MemberFilter;
  var RangeFilter = wdk.models.filter.RangeFilter;

  var Field = wdk.models.filter.Field;
  var Filters = wdk.models.filter.Filters;

  /**
   * Base class for FilterService classes.
   *
   * A subclass should only need to implement an
   * `applyFilters` method. This method's job is to apply
   * added filters to the data. This may be done locally
   * using JavaScript functions, or it may use a remote
   * service to retrieve filtered results.
   *
   */
  var FilterService = Backbone.Model.extend({
    defaults: {
      data: [],
      filteredData: [],
      metadata: []
    },

    initialize: function() {
      // track which filters are being applied
      this.filterChangeSet = [];
      this.filters = new Filters();

      this.listenTo(this.filters, 'add remove', function(m) { this.filterChangeSet.push(m); });
      this.listenTo(this.filters, 'add remove', _.debounce(this.applyFilters, 100));
    },

    applyFilters: function() {
      this.getFilteredData({ filters: this.filters })
        .then(function(data) {
          this.set('filteredData', data, {
            filterChangeSet: this.filterChangeSet
          });
          this.filterChangeSet = [];
        }.bind(this));
      return this;
    }
  });

  // wrap return value in a Promise
  var toPromise = function(fn) {
    return function() {
      var args = arguments;
      return new RSVP.Promise(function(resolve, reject) {
        try { resolve(fn.apply(this, args)); }
        catch(e) { reject(e); }
      }.bind(this));
    };
  };

  var LocalFilterService = FilterService.extend({

    /**
     * Applies filters and returns a promise which resolves
     * with a filtered copy of the data.
     *
     * Optionally, provide options to affect the filtering algorithm.
     * Available options are:
     *   * filters: A list of filters to apply.
     *   * omit: A list of fields to omit. This is useful when you want
     *           a distribution of values caused by other filters.
     *
     * @param {Object} options Options for the filtering algorithm
     */
    getFilteredData: toPromise(function(options) {
      var filters = options.filters;
      var data;

      if (options && options.omit) {
        filters = filters.reject(function(filter) {
          return _.contains(options.omit, filter.get('field'));
        });
      }

      if (filters.length) {
        data = filters.reduce(function(data, filter) {
          if (filter instanceof MemberFilter) {
            return this.applyMemberFilter(filter, data);
          } else if (filter instanceof RangeFilter) {
            return this.applyRangeFilter(filter, data);
          }
        }.bind(this), this.get('data'));
      }
      return data || [];
    }),

    applyMemberFilter: function(filter, data) {
      var field = filter.get('field');
      var filterValues = filter.get('values');
      var metadata = this.get('metadata')[field];

      var ret = data.filter(function(datum) {
        var metadataValue = metadata[datum.term] || Field.UNKNOWN_VALUE;
        var index = filterValues.length;

        // Use a for loop for efficiency
        while(index--) {
          if (filterValues[index] === metadataValue) break;
        }

        return index > -1;
      });
      return ret;
    },

    applyRangeFilter: function(filter, data) {
      var field = filter.get('field');
      var metadata = this.get('metadata')[field];
      var min = filter.get('min');
      var max = filter.get('max');
      var test;

      if (min !== null && max !== null) {
        test = function(datum) {
          var v = metadata[datum.term];
          return v >= min && v <= max;
        };
      } else if (min !== null) {
        test = function(datum) {
          return metadata[datum.term] >= min;
        };
      } else if (max !== null) {
        test = function(datum) {
          return metadata[datum.term] <= max;
        };
      }

      return data.filter(test);
    }

  });

  ns.FilterService = FilterService;
  ns.LocalFilterService = LocalFilterService;

});
