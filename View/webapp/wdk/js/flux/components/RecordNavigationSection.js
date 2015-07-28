import React from 'react';
import Tree from './Tree';
import wrappable from '../utils/wrappable';

let RecordNavigationSection = React.createClass({

  propTypes: {
    categories: React.PropTypes.array,
    hiddenCategories: React.PropTypes.array,
    onVisibleChange: React.PropTypes.func,
    heading: React.PropTypes.string
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

  handleToggle(e, category) {
    this.props.onVisibleChange({
      category,
      isVisible: e.target.checked
    });
  },

  render() {
    let { categories, hiddenCategories, heading } = this.props;
    return (
      <div className="wdk-Record-sidebar">
        <h3 className="wdk-RecordSidebarHeader">{heading}</h3>
        <Tree
          items={categories}
          maxDepth={1}
          childrenProperty="subCategories"
          getKey={item => String(item.name)}
          renderItem={category => {
            return (
              <div className="wdk-RecordNavigationItem">
                <input
                  className="wdk-Record-sidebar-checkbox"
                  type="checkbox"
                  checked={!hiddenCategories.includes(category.name)}
                  onChange={(e) => {
                    this.handleToggle(e, category);
                  }}
                />
                <a href={'#' + category.name} className="wdk-Record-sidebar-title">
                  <strong>{category.displayName}</strong>
                </a>
              </div>
            );
          }}
        />
      </div>
    );
  }
});

export default wrappable(RecordNavigationSection);
