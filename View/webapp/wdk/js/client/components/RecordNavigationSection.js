import React from 'react';
import RecordNavigationSectionCategories from './RecordNavigationSectionCategories';
import wrappable from '../utils/wrappable';

let RecordNavigationSection = React.createClass({

  propTypes: {
    categories: React.PropTypes.array,
    hiddenCategories: React.PropTypes.array,
    onVisibleChange: React.PropTypes.func,
    heading: React.PropTypes.node
  },

  mixins: [ React.addons.PureRenderMixin ],

  getDefaultProps() {
    return {
      onVisibleChange: function noop() {},
      heading: 'Categories'
    };
  },

  handleShowAll() {
  },

  handleShowNone() {
  },

  handleToggle(category, isVisible) {
    this.props.onVisibleChange({ category, isVisible });
  },

  render() {
    let { categories, hiddenCategories, heading } = this.props;
    return (
      <div className="wdk-RecordNavigationSection">
        <h3 className="wdk-RecordNavigationSectionHeader">{heading}</h3>
        <RecordNavigationSectionCategories
          categories={categories}
          hiddenCategories={hiddenCategories}
          onVisibleChange={this.handleToggle}
        />
      </div>
    );
  }
});

export default wrappable(RecordNavigationSection);
