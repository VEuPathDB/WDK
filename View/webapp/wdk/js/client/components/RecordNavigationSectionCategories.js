import React from 'react';
import { wrappable } from '../utils/componentUtils';
import RecordCategoryEnumeration from './RecordCategoryEnumeration';

let RecordNavigationSectionCategories = React.createClass({

  propTypes: {
    categories: React.PropTypes.array,
    expanded: React.PropTypes.bool,
    collapsedCategories: React.PropTypes.array,
    onCategoryToggle: React.PropTypes.func,
    parentEnumeration: React.PropTypes.string,
    query: React.PropTypes.string
  },

  getDefaultProperties() {
    return {
      collapsedCategories: [],
      onCategoryToggle: function() {},
      expanded: false
    };
  },

  render() {
    let {
      expanded,
      categories,
      collapsedCategories,
      onCategoryToggle,
      parentEnumeration,
      query
    } = this.props;

    if (categories == null) return null;

    return (
      <div>
        {categories.map((category, index) => {
          let isCollapsed = collapsedCategories.includes(category.name);
          let matchesQuery = category.displayName.toLowerCase().includes(query);
          let enumeration = parentEnumeration == null
            ? index + 1
            : parentEnumeration + '.' + (index + 1);
          return (
            <div key={String(category.name)} className="wdk-RecordNavigationItem">
              {parentEnumeration == null && matchesQuery &&
                <input
                  className="wdk-Record-sidebar-checkbox"
                  type="checkbox"
                  checked={!isCollapsed}
                  onChange={(e) => {
                    onCategoryToggle(category, !isCollapsed);
                  }}
                />
              }
              {matchesQuery &&
                <a
                  href={'#' + category.name}
                  className="wdk-Record-sidebar-title"
                  onClick={() => {
                    onCategoryToggle(category, false);
                  }}
                >
                  <RecordCategoryEnumeration enumeration={enumeration}/> <strong>{category.displayName}</strong>
                </a>
              }
              {expanded && (
                <RecordNavigationSectionCategories
                  {...this.props}
                  parentEnumeration={enumeration}
                  categories={category.subCategories}
                  onCategoryToggle={(subCategory, isCollapsed) => {
                    onCategoryToggle(subCategory, isCollapsed);
                    onCategoryToggle(category, isCollapsed);
                  }}
                />
              )}
            </div>
          );
        })}
      </div>
    );
  }
});

export default wrappable(RecordNavigationSectionCategories);
