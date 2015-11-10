import React from 'react';
import { wrappable } from '../utils/componentUtils';
import RecordCategoryEnumeration from './RecordCategoryEnumeration';

let noop = () => void 0;
let t = () => true;

let RecordNavigationSectionCategories = React.createClass({

  propTypes: {
    categories: React.PropTypes.array,
    showChildren: React.PropTypes.bool,
    onCategoryToggle: React.PropTypes.func,
    parentEnumeration: React.PropTypes.string,
    isCollapsed: React.PropTypes.func,
    isVisible: React.PropTypes.func
  },

  getDefaultProperties() {
    return {
      onCategoryToggle: noop,
      showChildren: false,
      isCollapsed: t,
      isVisible: t
    };
  },

  render() {
    let {
      showChildren,
      categories,
      onCategoryToggle,
      parentEnumeration,
      isCollapsed,
      isVisible
    } = this.props;

    if (categories == null) return null;

    return (
      <div>
        {categories.map((category, index) => {
          let shouldDisplay = isVisible(category);
          let enumeration = parentEnumeration == null
            ? index + 1
            : parentEnumeration + '.' + (index + 1);
          return (
            <div key={String(category.name)} className="wdk-RecordNavigationItem">
              {parentEnumeration == null && shouldDisplay &&
                <input
                  className="wdk-Record-sidebar-checkbox"
                  type="checkbox"
                  checked={!isCollapsed(category)}
                  onChange={(e) => {
                    onCategoryToggle(category, !e.target.checked);
                  }}
                />
              }
              {shouldDisplay &&
                <a
                  href={'#' + category.name}
                  className="wdk-Record-sidebar-title"
                  onClick={() => {
                    if (isCollapsed(category)) onCategoryToggle(category, false);
                  }}
                >
                  <RecordCategoryEnumeration enumeration={enumeration}/> <strong>{category.displayName}</strong>
                </a>
              }
              {showChildren && (
                <RecordNavigationSectionCategories
                  {...this.props}
                  parentEnumeration={enumeration}
                  categories={category.subCategories}
                  onCategoryToggle={(subCategory, collapsed) => {
                    if (isCollapsed(subCategory)) onCategoryToggle(subCategory, collapsed);
                    if (isCollapsed(category)) onCategoryToggle(category, collapsed);
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
