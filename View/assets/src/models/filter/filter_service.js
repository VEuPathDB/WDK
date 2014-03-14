wdk.namespace('wdk.models.filter', function(ns) {

  var Filter = wdk.models.filter.Filter;
  var MemberFilter = wdk.models.filter.MemberFilter;
  var RangeFilter = wdk.models.filter.RangeFilter;
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

    /**
     * Collection of unfiltered data
     */
    data: null,

    /**
     * Collection of filterable fields
     */
    fields: null,

    /**
     * Collection of data with filters applied
     */
    filteredData: null,

    /**
     * Collection of applied Filters
     */
    filters: null,

    constructor: function() {
      this.data = new Backbone.Collection();
      this.filteredData = new Backbone.Collection();
      this.fields = new Backbone.Collection();
      this.filters = new Filters();

      Backbone.Model.apply(this, arguments);
    },

    parse: function(data, options) {
      this.data.reset(data.data);
      this.fields.reset(data.fields);
      this.setFieldValues();
      return { title: data.title };
    },

    initialize: function() {
      var debounceApplyFilters = _.debounce(this.applyFilters, 50);
      this.listenTo(this.filters, 'add remove', debounceApplyFilters);
      this.listenTo(this.filteredData, 'reset', this.setFieldFilteredValues);
    },

    /**
     * Pluck out values from data for each field and set to values attribute
     */
    setFieldValues: function() {
      var fs = this;
      this.fields.forEach(function(field) {
        var values = _(fs.data.pluck('metadata')).pluck(field.get('term'));
        field.set('values', values);
      });
    },

    /**
     * Pluck out values from filtered data for each field and set to values
     * attribute, omitting filters applied to the field. This is useful for
     * visualizing the distribution of other fiters with respect to the field.
     */
    setFieldFilteredValues: function() {
      var fs = this;
      this.fields.forEach(function(field) {
        var values = fs.getFilteredData({ omit: [field] })
          .map(function(d) { return d.get('metadata')[field.get('term')]; });
        //var values = _(fs.filteredData.pluck('metadata')).pluck(field.get('term'));
        field.set('filteredValues', values);
      });
    },

    /**
     * Call getFilteredData and use result to reset filteredData
     */
    applyFilters: function() {
      var data = this.getFilteredData();
      this.filteredData.reset(data);
      return this;
    },

    /**
     * Returns a subset of data with filters applied
     *
     * Implementing subclasses should override this method.
     */
    getFilteredData: function() { return this; }

  });

  var LocalFilterService = FilterService.extend({

    /**
     * Applies filters and returns the raw data
     *
     * Optionally, provide options to affect the filtering algorithm.
     * Available options are:
     *   * omit: A list of fields to omit. This is useful when you want
     *           a distribution of values caused by other filters.
     *
     * @param {Object} options Options for the filtering algorithm
     */
    getFilteredData: function(options) {
      var service = this;
      var filters = this.filters;
      var data;

      if (options && options.omit) {
        filters = filters.reject(function(filter) {
          return _.contains(options.omit, filter.get('field'));
        });
      }

      if (filters.length) {
        data = filters.reduce(function(data, filter) {
          if (filter instanceof MemberFilter) {
            return service.applyMemberFilter(filter, data);
          } else if (filter instanceof RangeFilter) {
            return service.applyRangeFilter(filter, data);
          }
        }, service.data);
      }
      return data || [];
    },

    applyMemberFilter: function(filter, data) {
      var field = filter.get('field').get('term');
      var values = filter.get('values');
      return data.filter(function(d) {
        return _.contains(values, d.get('metadata')[field]);
      });
    },

    applyRangeFilter: function(filter, data) {
      var field = filter.get('field').get('term');
      var min = filter.get('min');
      var max = filter.get('max');
      var test;

      if (min != null && max != null) {
        test = function(d) {
          var v = d.get('metadata')[field]
          return v >= min && v <= max;
        };
      } else if (min != null) {
        test = function(d) {
          return d.get('metadata')[field] >= min;
        };
      } else if (max != null) {
        test = function(d) {
          return d.get('metadata')[field] <= max;
        };
      }

      return data.filter(test);
    }

  });

  ns.FilterService = FilterService;
  ns.LocalFilterService = LocalFilterService;

});
