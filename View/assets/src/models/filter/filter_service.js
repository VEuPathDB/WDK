/*global RSVP*/
wdk.namespace('wdk.models.filter', function(ns) {
  'use strict';

  var MemberFilter = wdk.models.filter.MemberFilter;
  var RangeFilter = wdk.models.filter.RangeFilter;

  var Filters = wdk.models.filter.Filters;
  var Fields = wdk.models.filter.Fields;
  var Field = wdk.models.filter.Field;

  // var Datum = Backbone.Model.extend({
  //   idAttribute: 'term',
  //   defaults: {
  //     ignored: false
  //   }
  // });

  // var Data = Backbone.Collection.extend({
  //   model: Datum
  // });

  // FIXME Treat metadata, metadataSpec, and values as separate
  // entities and operate in such a way.
  //
  // Thus, the data object below will contain those three
  // objects: { values, metadata, metadataSpec }
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

    /**
     * Collection of filterable fields
     */
    fields: null,

    /**
     * Collection of applied Filters
     */
    filters: null,

    defaults: {
      data: [],
      filteredData: [],
      title: 'Items'
    },

    constructor: function() {
      this.filters = new Filters();
      this.fields = new Fields();
      // this.data = new Data();
      // this.filteredData = new Data();

      Backbone.Model.apply(this, arguments);
    },

    parse: function(attrs) {
      this.fields.reset(attrs.fields);
      this.filters.reset(attrs.filters);
      // move these to specific instances
      // this.data.reset(attrs.data);
      // this.metadata = attrs.metadata;

      return _.omit(attrs, 'fields', 'filters');
    },

    initialize: function() {
      this.filterChangeSet = [];

      this.listenTo(this.filters, 'add remove reset', function(m) { this.filterChangeSet.push(m); });
      this.listenTo(this.filters, 'add remove reset', _.debounce(this.applyFilters, 100));
      this.applyFilters();
    },

    reset: function(attrs) {
      this.parse(attrs);
      this.applyFilters();
    },

    /**
     * Call getFilteredData and use result to reset filteredData
     *
     * TODO Add filterChangeSet to options
     */
    applyFilters: function() {
      var _this = this;
      _this.getFilteredData({ filters: _this.filters })
        .then(function(data) {
          _this.set('filteredData', data, {
            filterChangeSet: _this.filterChangeSet
          });
          _this.filterChangeSet = [];
        });

      return _this;
    },

    /**
     * Returns a subset of data with filters applied
     *
     * Implementing subclasses should override this method.
     */
    getFilteredData: function() { return this; }

  });

  // wrap return value in a Promise
  var toPromise = function(fn) {
    return function() {
      var args = arguments;
      var _this = this;
      return new RSVP.Promise(function(resolve, reject) {
        try { resolve(fn.apply(_this, args)); }
        catch(e) { reject(e); }
      });
    };
  };

  var LocalFilterService = FilterService.extend({

    getFieldDistribution: function(field) {
      var _this = this;
      var term = field.get('term');
      var metadata = this.get('metadata');

      var dataToTermValues = function(data) {
        return data
          .map(function(datum) {
            var value = metadata[datum.term][term];
            if (_.isUndefined(value)) {
              return Field.UNKNOWN_VALUE;
            } else if (field.get('type') == 'number') {
              return Number(value);
            }
            return value;
          });
      };

      return this.getFilteredData({ filters: this.filters, omit: [term] })
        .then(dataToTermValues)
        .then(function(filteredValues) {
          var values = dataToTermValues(_this.get('data'));
          var uniqValues = _.compose(_.sortBy, _.uniq)(values);

          // Key the counts array by position in uniqValues to prevent coercion.
          // This is important for UNKNOWNs.
          var counts = _.countBy(values, function(value) {
            return _.indexOf(uniqValues, value, true);
          });

          var filteredCounts = _.countBy(filteredValues, function(value) {
            return _.indexOf(uniqValues, value, true);
          });

          return uniqValues.map(function(value, index) {
            return {
              value: value,
              count: counts[index],
              filteredCount: filteredCounts[index]
            };
          });
        });
    },

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
      var _this = this;
      var filters = options.filters;
      var values;

      if (options && options.omit) {
        filters = filters.reject(function(filter) {
          return _.contains(options.omit, filter.get('field'));
        });
      }

      if (filters.length) {
        values = filters.reduce(function(values, filter) {
          if (filter instanceof MemberFilter) {
            return _this.applyMemberFilter(filter, values);
          } else if (filter instanceof RangeFilter) {
            return _this.applyRangeFilter(filter, values);
          }
        }, _this.get('data'));
      }
      return values || [];
    }),

    applyMemberFilter: function(filter, values) {
      var field = filter.get('field');
      var filterValues = filter.get('values');
      var metadata = this.get('metadata');

      var ret = values.filter(function(value) {
        var metadataValue = metadata[value.term][field] || Field.UNKNOWN_VALUE;
        var index = filterValues.length;

        // Use a for loop for efficiency
        while(index--) {
          if (filterValues[index] === metadataValue) break;
        }

        return index > -1;
      });
      return ret;
    },

    applyRangeFilter: function(filter, values) {
      var metadata = this.get('metadata');
      var field = filter.get('field');
      var min = filter.get('min');
      var max = filter.get('max');
      var test;

      if (min !== null && max !== null) {
        test = function(value) {
          var v = metadata[value.term][field];
          return v >= min && v <= max;
        };
      } else if (min !== null) {
        test = function(value) {
          return metadata[value.term][field] >= min;
        };
      } else if (max !== null) {
        test = function(value) {
          return metadata[value.term][field] <= max;
        };
      }

      return values.filter(test);
    }

  });

  ns.FilterService = FilterService;
  ns.LocalFilterService = LocalFilterService;

});
