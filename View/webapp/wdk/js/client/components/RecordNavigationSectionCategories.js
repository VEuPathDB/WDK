import React from 'react';
import wrappable from '../utils/wrappable';

let RecordNavigationSectionCategories = React.createClass({

  propTypes: {
    categories: React.PropTypes.array,
    hiddenCategories: React.PropTypes.array,
    onVisibleChange: React.PropTypes.func
  },

  getDefaultProperties() {
    return {
      hiddenCategories: [],
      onVisibleChange: function() {}
    };
  },

  render() {
    let { categories, hiddenCategories, onVisibleChange } = this.props;

    if (categories == null) return null;

    return (
      <div>
        {categories.map(category => {
          let isHidden = hiddenCategories.includes(category.name);
          return (
            <div key={String(category.name)} className="wdk-RecordNavigationItem">
              <input
                className="wdk-Record-sidebar-checkbox"
                type="checkbox"
                checked={!isHidden}
                onChange={(e) => {
                  onVisibleChange(category, isHidden);
                }}
              />
              <a
                href={'#' + category.name}
                className="wdk-Record-sidebar-title"
                onClick={() => {
                  if (isHidden) onVisibleChange(category, isHidden);
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
