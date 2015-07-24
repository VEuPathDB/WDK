import React from 'react';
import Tree from './Tree';
import wrappable from '../utils/wrappable';

let RecordNavigationSection = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

  getDefaultProps() {
    return {
      onVisibleChange: function noop() {}
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
    let { recordClass, hiddenCategories } = this.props;
    return (
      <div>
        <h3 className="wdk-RecordSidebarHeader">Categories</h3>
        <Tree
          items={recordClass.attributeCategories}
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
