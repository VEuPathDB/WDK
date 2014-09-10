wdk.namespace('wdk.views.filter', function(ns) {
  'use strict';

  var MemberView = wdk.views.View.extend({

    events: {
      'click': 'toggleSelected',
    },

    className: 'member',

    tagName: 'tr',

    template: wdk.templates['filter/member.handlebars'],

    initialize: function(options) {
      this.options = options;
      this.listenTo(this.model, 'change', this.render);
    },

    render: function() {
      this.$el.html(this.template(this.model.attributes));
      // this.$el.tooltip({ title: this.model.get('value'), placement: 'left', delay: 400 });
      this.$el.toggleClass('selected', this.model.get('selected'));
      return this;
    },

    toggleSelected: function() {
      this.model.set('selected', !this.model.get('selected'));
    }

  });

  ns.MembershipFilterView = wdk.views.View.extend({

    events: {
      'click .read-more a'         : 'expandDescription',
      'click [href="#select-all"]' : 'selectAll',
      'click [href="#clear-all"]'  : 'clearAll'
    },

    memberViews: null,

    template: wdk.templates['filter/membership_filter.handlebars'],

    constructor: function(filterService) {
      var initArgs = [].slice.call(arguments, 1);
      this.filterService = filterService;
      this.memberViews = [];
      wdk.views.View.apply(this, initArgs);
    },

    initialize: function(options) {
      var filters = this.filterService.filters;
      this.options = options;
      this.listenTo(filters, 'add', _.partial(this.handleFilterUpdate, true));
      this.listenTo(filters, 'remove', _.partial(this.handleFilterUpdate, false));
    },

    handleFilterUpdate: function(isSelected, filter, filters, options) {
      var update = options.origin !== this &&
        filter.get('field') === this.model.get('term');
      var values = filter.get('values');

      if (update) {
        // set selected to true or false
        this.memberViews.forEach(function renderViews(memberView) {
          var member = memberView.model;
          if (values.indexOf(member.get('value')) > -1) {
            member.set('selected', isSelected);
          }
        });
      }
    },

    render: function() {
      var field = this.model;
      var filterService = this.filterService;
      var filter = this.controller.getFieldFilter(field);
      var filterValues = filter ? filter.get('values') : [];
      var members = this.members = new Backbone.Collection();

      this.$el.html(this.template({
        field: this.model.attributes,
        options: this.options
      }));

      members.on('change:selected', _.debounce(function() {
        var type = this.model.get('type');
        var values = members.where({selected: true}).map(function(member) {
          return type === 'number'
            ? Number(member.get('value'))
            : member.get('value');
        });
        var filters = filterService.filters;

        filters.remove(filters.where({ field: field.get('term') }), { origin: this });

        if (values.length) {
          filters.add({
            field: field.get('term'),
            operation: field.get('filter'),
            values: values
          }, { origin: this });
        }
      }.bind(this), 50));


      var distribution = this.model.get('distribution');
      var counts = _.pluck(distribution, 'count');

      var scale = _.max(counts) + 10;
      var size = counts.reduce(function(acc, count){ return acc + count; });

      this.memberViews = distribution
        .map(function(valueDist) {
          var value = valueDist.value;
          var count = valueDist.count;
          var fcount = valueDist.filteredCount || 0;

          var member = members.add({
            value: value,
            count: count,
            percent: (count / size * 100).toFixed(2),
            distribution: (count / scale * 100).toFixed(2),
            filteredDistribution: (fcount / scale * 100).toFixed(2),
            selected: !!(_.contains(filterValues, value))
          });

          return new MemberView({
            model: member,
            controller: this.controller
          });
        }.bind(this));

      _.invoke(this.memberViews, 'render');
      this.$('tbody').append(_.pluck(this.memberViews, 'el'));

      // activate Read more link if text is overflowed
      var p = this.$('.description p').get(0);
      if (p && p.scrollWidth > p.clientWidth) {
        this.$('.description .read-more').addClass('visible');
      }

      // add border and scrollbar when members a long
      var panel = this.$('.membership-table-panel');
      if (panel.get(0).scrollHeight > panel.get(0).clientHeight) {
        panel.addClass('overflowed');
      }

      return this;
    },

    expandDescription: function(e) {
      var p = this.$('.description p').toggleClass('expanded');
      e.currentTarget.innerHTML = p.hasClass('expanded') ? 'read less' : 'read more';
      e.preventDefault();
    },

    selectAll: function(e) {
      e.preventDefault();
      this.members.invoke('set', 'selected', true);
    },

    clearAll: function(e) {
      e.preventDefault();
      this.members.invoke('set', 'selected', false);
    },

    didRemove: function() {
      _.invoke(this.memberViews, 'remove');
    }

  });

});
