import React from 'react';
import RecordNavigationSectionCategories from './RecordNavigationSectionCategories';
import { wrappable } from '../utils/componentUtils';

let RecordNavigationSection = React.createClass({

  propTypes: {
    categories: React.PropTypes.array,
    collapsedCategories: React.PropTypes.array,
    onCollapsedChange: React.PropTypes.func,
    heading: React.PropTypes.node
  },

  mixins: [ React.addons.PureRenderMixin ],

  getDefaultProps() {
    return {
      onCollapsedChange: function noop() {},
      heading: 'Categories'
    };
  },

  handleShowAll() {
  },

  handleShowNone() {
  },

  handleToggle(category, isCollapsed) {
    this.props.onCollapsedChange({ category, isCollapsed });
  },

  render() {
    let { categories, collapsedCategories, heading } = this.props;
    return (
      <div className="wdk-RecordNavigationSection">
        <h2 className="wdk-RecordNavigationSectionHeader">{heading}</h2>
        <RecordNavigationSectionCategories
          categories={categories}
          collapsedCategories={collapsedCategories}
          onCollapsedChange={this.handleToggle}
        />
      </div>
    );
  }
});

export default wrappable(RecordNavigationSection);
