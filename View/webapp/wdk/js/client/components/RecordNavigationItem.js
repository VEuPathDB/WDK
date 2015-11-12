import React from 'react';
import classnames from 'classnames';
import { wrappable } from '../utils/componentUtils';
import RecordCategoryEnumeration from './RecordCategoryEnumeration';

class RecordNavigationItem extends React.Component {

  render() {
    let {
      active,
      visible,
      category,
      enumeration,
      collapsible,
      collapsed,
      onCategoryToggle
    } = this.props;

    let titleClassnames = classnames({
      'wdk-Record-sidebar-title': true,
      'wdk-Record-sidebar-title__active': active
    });

    return (
      <div key={String(category.name)} className="wdk-RecordNavigationItem">
        {collapsible && visible &&
          <input
            className="wdk-Record-sidebar-checkbox"
            type="checkbox"
            checked={!collapsed}
            onChange={(e) => void onCategoryToggle(category, !e.target.checked)}
          />
        }
        {visible &&
          <a
            href={'#' + category.name}
            className={titleClassnames}
            onClick={() => {
              if (collapsed) onCategoryToggle(category, false);
            }}
          >
            <RecordCategoryEnumeration enumeration={enumeration}/> {category.displayName}
          </a>
        }
        {this.props.children}
      </div>
    );
  }
}

export default wrappable(RecordNavigationItem);
