import React from 'react';
import RecordNavigationSectionCategories from './RecordNavigationSectionCategories';
import { wrappable } from '../utils/componentUtils';

let RecordNavigationSection = React.createClass({

  propTypes: {
    categories: React.PropTypes.array,
    collapsedCategories: React.PropTypes.array,
    onCategoryToggle: React.PropTypes.func,
    heading: React.PropTypes.node
  },

  mixins: [ React.addons.PureRenderMixin ],

  getDefaultProps() {
    return {
      onCategoryToggle: function noop() {},
      heading: 'Categories'
    };
  },

  handleShowAll() {
  },

  handleShowNone() {
  },

  render() {
    let { categories, collapsedCategories, heading } = this.props;
    return (
      <div className="wdk-RecordNavigationSection">
        <h2 className="wdk-RecordNavigationSectionHeader">{heading}</h2>
        <RecordNavigationSectionCategories
          categories={categories}
          collapsedCategories={collapsedCategories}
          onCategoryToggle={this.props.onCategoryToggle}
        />
      </div>
    );
  }
});

export default wrappable(RecordNavigationSection);
