wdk.namespace('wdk.controllers', function(ns) {
  'use strict';

  var LocalFilterService = wdk.models.filter.LocalFilterService;
  var FilterItemsView = wdk.views.filter.FilterItemsView;
  var FilterView = wdk.views.filter.FilterView;

  /**
   * options:
   *  - data: Array of data objects to filter
   *  - fields: Array of metadata terms
   *  - filters: Array of filter objects
   *  - ignored: Array of data ids to ignore
   *  - title: String name used in UI
   *  - trimMetadataTerms: Boolean, when true remove parents w/ one child
   *  - defaultColumns: Array of field names to show in results view
   */
  ns.FilterParam = wdk.views.View.extend({

    className: 'filter-param',

    initialize: function(options) {
      this.data = options.data;
      this.metadata = options.metadata;
      this.fields = options.fields;
      this.filters = options.filters;
      this.ignored = options.ignored || [];
      this.title = options.title;

      var trimMetadataTerms = Boolean(options.trimMetadataTerms);
      var defaultColumns = options.defaultColumns;
      var filterService = this.filterService = new LocalFilterService({
        data: this.data,
        metadata: this.metadata,
        fields: this.fields,
        filters: this.filters,
        title: this.title
      }, {
        parse: true,
        root: 'metadata'
      });

      var itemsView = new FilterItemsView(this.filterService, {
        model: this.filterService.filters,
        controller: this
      });

      var filterView = new FilterView({
        model: this.filterService,
        controller: this,
        defaultColumns: defaultColumns,
        trimMetadataTerms: trimMetadataTerms
      });

      // map strinigified version of user selections to value attribute of $el
      filterService.on('change:filteredData', function() {
        this.updateValue();
        this._setSelectedFieldDistribution();
      }.bind(this));

      this.$el
        .append(itemsView.render().el)
        .append(filterView.render().el);
    },

    // Update the UI with new data. This gets triggered when user changes the
    // value of a dependent parameter, so we can skip the init stuff.
    update: function(data) {
      this.filterService.reset(data);
      return this;
    },

    isIgnored: function(datum) {
      var term = _.isObject(datum) ? datum.term : datum;
      return _.contains(this.ignored, term);
    },

    toggleIgnored: function(datum, isIgnored) {
      var term = _.isObject(datum) ? datum.term : datum;
      var index = _.indexOf(this.ignored, term);
      var doUpdate = false;

      if (isIgnored && index === -1) {
        this.ignored.push(term);
        doUpdate = true;
      } else if (!isIgnored && index > -1) {
        this.ignored.splice(index, 1);
        doUpdate = true;
      }
      if (doUpdate) this.updateValue();
      return this;
    },

    // This is the user's selection of data with all
    // filters applied, and all ignored values removed.
    getSelectedData: function() {
      var ignored = this.ignored;
      return this.filterService.get('filteredData')
        .reduce(function(acc, data) {
          if (!_.contains(ignored, data.term)) acc.push(data);
          return acc;
        }, []);
    },

    // Trigger events with selected values.
    // value - serialization of param
    // selectedData - just the values selected
    updateValue: function() {
      var data = this.getSelectedData();
      var value = {
        values: _.pluck(data, 'term'),
        ignored: this.ignored,
        filters: this.filterService.filters
      };
      this.trigger('change:value', this, value);
      this.trigger('change:selectedData', this, data);
      return this;
    },

    _setSelectedFieldDistribution: function() {
      var field = this.selectedField;
      return field && this.filterService.getFieldDistribution(field)
        .then(function(distribution) {
          field.set('distribution', distribution);
        });
    },

    selectField: function(field) {
      this.selectedField = field;
      this._setSelectedFieldDistribution().then(function() {
        this.trigger('select:field', field);
      }.bind(this));
      return this;
    },

    getFieldFilter: function(field) {
      return this.filterService.filters.findWhere({
        field: field.get('term')
      });
    },

    // return metadata
    getMetadata: function(datum, field) {
      return this.metadata[datum.term][field.get('term')];
    }

  });

});
