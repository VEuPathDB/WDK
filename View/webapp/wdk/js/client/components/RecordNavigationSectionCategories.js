import React from 'react';
import { wrappable } from '../utils/componentUtils';
import RecordCategoryEnumeration from './RecordCategoryEnumeration';

let RecordNavigationSectionCategories = React.createClass({

  propTypes: {
    categories: React.PropTypes.array,
    expanded: React.PropTypes.bool,
    collapsedCategories: React.PropTypes.array,
    onCategoryToggle: React.PropTypes.func,
    parentEnumeration: React.PropTypes.string
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
      parentEnumeration
    } = this.props;

    if (categories == null) return null;

    return (
      <div>
        {categories.map((category, index) => {
          let isCollapsed = collapsedCategories.includes(category.name);
          let enumeration = parentEnumeration == null
            ? index + 1
            : parentEnumeration + '.' + (index + 1);
          return (
            <div key={String(category.name)} className="wdk-RecordNavigationItem">
              {parentEnumeration == null &&
                <input
                  className="wdk-Record-sidebar-checkbox"
                  type="checkbox"
                  checked={!isCollapsed}
                  onChange={(e) => {
                    onCategoryToggle(category, !isCollapsed);
                  }}
                />
              }
              <a
                href={'#' + category.name}
                className="wdk-Record-sidebar-title"
                onClick={() => {
                  if (isCollapsed) onCategoryToggle(category, !isCollapsed);
                }}
              >
                <RecordCategoryEnumeration enumeration={enumeration}/> <strong>{category.displayName}</strong>
              </a>
              {expanded && (
                <RecordNavigationSectionCategories
                  {...this.props}
                  parentEnumeration={enumeration}
                  categories={category.subCategories}/>
                )}
            </div>
          );
        })}
      </div>
    );
  }
});

export default wrappable(RecordNavigationSectionCategories);
