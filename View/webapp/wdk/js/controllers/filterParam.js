/* global RSVP, Spinner */
wdk.namespace('wdk.controllers', function(ns) {
  'use strict';


  // Imports
  // -------

  // models
  var LocalFilterService = wdk.models.filter.LocalFilterService;
  var Fields = wdk.models.filter.Fields;
  var Field = wdk.models.filter.Field;

  // views
  var TabsView = wdk.views.core.TabsView;
  var CollapsibleView = wdk.views.core.CollapsibleView;
  var FilterItemsView = wdk.views.filter.FilterItemsView;
  var FilterFieldsView = wdk.views.filter.FilterFieldsView;
  var ResultsView = wdk.views.filter.ResultsView;


  // Helpers
  // -------

  var countByValues = _.compose(_.countBy, _.flatten, _.values);
  var numericSort = function(a, b){ return a > b; };
  var stringSort; // passing this to [].sort() will use the default sort


  // FilterParam
  // -----------

  // options:
  //   - data: Array of data objects to filter
  //   - fields: Array of metadata terms
  //   - filters: Array of filter objects
  //   - ignored: Array of data ids to ignore
  //   - title: String name used in UI. Defaults to "Items".
  //   - trimMetadataTerms: Boolean, when true remove parents w/ one child
  //   - defaultColumns: Array of field names to show in results view
  // 
  ns.FilterParam = wdk.views.core.View.extend({

    className: 'filter-param',

    initialize: function(options) {
      this.metadataXhrQueue = {};
      this.data = options.data;
      this.metadata = options.metadata || {};
      this.ignored = options.ignored || [];
      this.title = options.title || 'Items';
      this.questionName = options.questionName;
      this.name = options.name;
      this.fields = new Fields(options.fields, {
        trimMetadataTerms: Boolean(options.trimMetadataTerms)
      });
      this.defaultColumns = options.defaultColumns;
      this.spinner = new Spinner({
        lines: 11, // The number of lines to draw
        length: 3, // The length of each line
        width: 2, // The line thickness
        radius: 4, // The radius of the inner circle
        corners: 1, // Corner roundness (0..1)
        rotate: 0, // The rotation offset
        direction: 1, // 1: clockwise, -1: counterclockwise
        color: '#000', // #rgb or #rrggbb or array of colors
        speed: 1, // Rounds per second
        trail: 60, // Afterglow percentage
        shadow: false, // Whether to render a shadow
        hwaccel: false, // Whether to use hardware acceleration
        className: 'spinner', // The CSS class to assign to the spinner
        zIndex: 2e9, // The z-index (defaults to 2000000000)
        top: '34px', // Top position relative to parent
        left: '-222px' // Left position relative to parent
      });


      // Load initial metadata
      // ---------------------

      var filterFields = (options.filters || [])
        .map(function(filter) {
          return this.fields.get(filter.field);
        }.bind(this));
      var defaultFields = _.map(this.defaultColumns, this.fields.get, this.fields);
      var initialFields = _.union(defaultFields, filterFields);
      var metadataPromises = _.map(initialFields, this.getMetadata, this);


      // Wait for metadata to load to prevent blank UI sections
      RSVP.all(metadataPromises).then(function() {

        // Create FilterService instance
        // -----------------------------

        this.filterService = new LocalFilterService({
          data: this.data,
          metadata: this.metadata
        });
        this.filterService.filters.set(options.filters);


        // Set up listeners
        // ----------------

        var debouncedUpdateValue = _.debounce(this.updateValue.bind(this), 200);
        this.listenTo(this.filterService.filters, 'add remove', debouncedUpdateValue);
        this.listenTo(this.filterService, 'change:filteredData', debouncedUpdateValue);
        this.listenTo(this.filterService, 'change:filteredData', this._setSelectedFieldDistribution);
        debouncedUpdateValue();


        // Create views
        // ------------

        // List of selected filters
        var itemsView = new FilterItemsView(this.filterService, {
          model: this.filterService.filters,
          controller: this
        });

        // Main selection UI
        var filterFieldsView = new FilterFieldsView({
          model: this.filterService,
          controller: this,
          defaultColumns: this.defaultColumns,
          trimMetadataTerms: this.trimMetadataTerms
        });

        // Results of selection
        var resultsView = new ResultsView({
          model: this.filterService,
          controller: this
        });

        // Use a tabbed interface for selection and results
        var tabsView = new TabsView({
          className: 'filter-param-tabs',
          tabs: [
            { title: 'Select ' + this.title, view: filterFieldsView },
            { title: 'View selection (<span class="count">0</span>)', view: resultsView }
          ]
        }).listenTo(this, 'change:selectedData', function(ctrl, data) {
          this.$('.count').text(data.length);
        });

        // Allow the tabbed interface to be collapsed
        var collapsibleView = new CollapsibleView({
          className: 'filter-view',
          expandString: 'Select ' + this.title,
          collapse: false,
          collapseTemplate: _.template([
            '<div class="collapse-wrapper">',
            '  <a href="#"><%= collapseString %></a>',
            '</div>'
          ].join('')),
          view: tabsView
          }).listenTo(this, 'select:field', function() {
            this.collapse(false);
          }).listenTo(this.filterService.filters, 'add remove', function(filter) {
            this.$expand.html(filter.length ? 'Refine selection' : this.expandString);
          });


        // Set default values
        // ---------------------------

        var leaves = this.fields.where({'leaf': 'true'});
        var defaultSelection = leaves.length === 1
          ? leaves[0]
          : filterFields[0];
        this.selectField(defaultSelection);


        // Append view to DOM and trigger 'ready'
        // --------------------------------------

        this.$el.append(itemsView.el, collapsibleView.el);
        this.trigger('ready', this);

      }.bind(this));
    },

    showSpinner: function() {
      this.$('.filter-param-tabs').append(this.spinner.spin().el);
    },

    hideSpinner: function() {
      this.spinner.stop();
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
      return field && this.getFieldDistribution(field)
        .then(function(distribution) {
          field.set('distribution', distribution);
        });
    },

    // returns a promise that resolves to metadata for a property:
    //     [ { data_term: metadata_value }, ... ]
    getMetadata: function(field) {
      return new RSVP.Promise(function(resolve, reject) {
        var term = field.get('term');
        var type = field.get('type');

        // if it's cached, return a promise that resolves immediately
        if (this.metadata[term]) {
          resolve(this.metadata[term]);
          return;
        }

        var metadataUrl = wdk.webappUrl('getMetadata.do');
        var metadataUrlParams = {
          questionFullName: this.questionName,
          name: this.name,
          json: true,
          property: term
        };

        this.showSpinner();
        (this.metadataXhrQueue[term] = $.getJSON(metadataUrl, metadataUrlParams))
          .then(function(metadata) {
            metadata = _.indexBy(metadata, 'sample');
            // cache metadata and transform to a dict
            this.metadata[term] = this.data
              .reduce(function(acc, d) {
                var values = _.result(metadata[d.term], 'values');
                acc[d.term] = _.isUndefined(values)
                  ? [ Field.UNKNOWN_VALUE ]
                  : type == 'number' ? values.map(Number) : values;
                return acc;
              }, {});
            resolve(this.metadata[term]);
          }.bind(this))
          .fail(function(err) {
            // TODO Show user an error message
            reject(err);
          })
          .always(function() {
            delete this.metadataXhrQueue[term];
            if (_.size(this.metadataXhrQueue) === 0) {
              this.hideSpinner();
            }
          }.bind(this));
      }.bind(this));
    },

    // returns a promse which resolves to an array of objects:
    // { value, count, filteredCount }
    getFieldDistribution: function(field) {
      var term = field.get('term');
      var type = field.get('type');

      // Retrieve metadata and filtered data and return a promise
      return RSVP.hash({
        metadata: this.getMetadata(field),
        filteredData: this.filterService.getFilteredData({
          filters: this.filterService.filters,
          omit: [ term ]
        })
      }).then(function(results) {
        var filteredMetadata = results.filteredData
          .reduce(function(acc, fd) {
            acc[fd.term] = results.metadata[fd.term];
            return acc;
          }, {});
        var counts = countByValues(results.metadata);
        var filteredCounts = countByValues(filteredMetadata);

        return _.uniq(_.flatten(_.values(results.metadata)))
          .sort(type == 'number' ? numericSort : stringSort)
          .map(function(value) {
            return {
              value: value,
              count: Number(counts[value]),
              filteredCount: filteredCounts[value]
            };
          });
      }.bind(this));
    },

    // if field, abort current metadata requests
    // and trigger request for new field
    selectField: function(field) {
      if (field && field !== this.selectedField) {
        this.abortMetadataRequest(this.selectedField);
        this.selectedField = field;
        this._setSelectedFieldDistribution().then(function() {
          this.trigger('select:field', field);
        }.bind(this));
      }
      return this;
    },

    abortMetadataRequest: function(field) {
      if (field) {
        return _.result(this.metadataXhrQueue[field.get('term')], 'abort');
      }
    },

    getFieldFilter: function(field) {
      return this.filterService.filters.findWhere({
        field: field.get('term')
      });
    }

  });

});
