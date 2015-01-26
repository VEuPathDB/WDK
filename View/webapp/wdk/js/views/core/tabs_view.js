wdk.namespace('wdk.views.core', function(ns) {
  'use strict';

  //
  // View container to create a tabbed interface
  //
  //
  // OPTIONS
  //   - tabs Array of objects { title: String, view: View }
  //     (View should be a Backbone View, but any object with properties `el` and `cid` should work)
  //
  //
  // EXAMPLE
  //     new TabsView({ tabs: [{ title: 'View 1', view: view1 }, { title: 'View 2', view: view2 }] });
  //

  ns.TabsView = Backbone.View.extend({

    initialize: function(options) {
      this.tabs = options.tabs;
      this.render();
    },

    render: function() {
      var frag = $('<div><ul/></div>');
      var nav = frag.find('ul');
      this.tabs.forEach(function(tab) {
        var id = this._createId(tab);
        nav.append('<li><a href="#' + id + '">' + tab.title + '</a></li>');
        frag.append($('<div id="' + id + '"/>').append(tab.view.el));
      }.bind(this));
      this.$el.append(frag.tabs({
        activate: function(event, ui) {
          var newIndex = _.result(ui.newTab, 'index');
          var oldIndex = _.result(ui.oldTab, 'index');

          if (newIndex) _.result(this.tabs[newIndex].view, 'show');
          if (oldIndex) _.result(this.tabs[oldIndex].view, 'hide');
        }.bind(this)
      }));
      return this;
    },

    _createId: function(tab) {
      var id = [ this.cid, 'tab', tab.view.cid ].join('-');
      return id;
    }

  });
});
