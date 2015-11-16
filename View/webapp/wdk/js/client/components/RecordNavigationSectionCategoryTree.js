import React from 'react';
import { wrappable } from '../utils/componentUtils';
import RecordNavigationItem from './RecordNavigationItem';

let noop = () => void 0;
let t = () => true;

let RecordNavigationSectionCategories = React.createClass({

  propTypes: {
    categories: React.PropTypes.array,
    showChildren: React.PropTypes.bool,
    onCategoryToggle: React.PropTypes.func,
    parentEnumeration: React.PropTypes.string,
    isCollapsed: React.PropTypes.func,
    isVisible: React.PropTypes.func,
    activeCategory: React.PropTypes.object
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
      isVisible,
      activeCategory
    } = this.props;

    if (categories == null) return null;

    return (
      <div>
        {categories.map((category, index) => {
          let enumeration = parentEnumeration == null
            ? index + 1
            : parentEnumeration + '.' + (index + 1);

          return (
            <RecordNavigationItem
              key={category.name}
              category={category}
              enumeration={enumeration}
              active={activeCategory === category}
              visible={isVisible(category)}
              collapsed={isCollapsed(category)}
              collapsible={parentEnumeration == null}
              onCategoryToggle={onCategoryToggle}
            >
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
            </RecordNavigationItem>
          );
        })}
      </div>
    );
  }
});

function makeEnumeration(parentEnumeration, index) {
  return parentEnumeration == null
    ? String(index + 1)
    : parentEnumeration + '.' + String(index + 1);
}

export default wrappable(RecordNavigationSectionCategories);
