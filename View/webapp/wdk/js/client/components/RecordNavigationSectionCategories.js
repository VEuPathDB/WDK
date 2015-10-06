import React from 'react';
import { wrappable } from '../utils/componentUtils';

let RecordNavigationSectionCategories = React.createClass({

  propTypes: {
    categories: React.PropTypes.array,
    collapsedCategories: React.PropTypes.array,
    onCategoryToggle: React.PropTypes.func
  },

  getDefaultProperties() {
    return {
      collapsedCategories: [],
      onCategoryToggle: function() {}
    };
  },

  render() {
    let { categories, collapsedCategories, onCategoryToggle } = this.props;

    if (categories == null) return null;

    return (
      <div>
        {categories.map(category => {
          let isCollapsed = collapsedCategories.includes(category.name);
          return (
            <div key={String(category.name)} className="wdk-RecordNavigationItem">
              <input
                className="wdk-Record-sidebar-checkbox"
                type="checkbox"
                checked={!isCollapsed}
                onChange={(e) => {
                  onCategoryToggle(category, !isCollapsed);
                }}
              />
              <a
                href={'#' + category.name}
                className="wdk-Record-sidebar-title"
                onClick={() => {
                  if (isCollapsed) onCategoryToggle(category, !isCollapsed);
                }}
              >
                <strong>{category.displayName}</strong>
              </a>
            </div>
          );
        })}
      </div>
    );
  }
});

export default wrappable(RecordNavigationSectionCategories);
