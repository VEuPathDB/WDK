import React from 'react';
import classnames from 'classnames';
import { wrappable } from '../utils/componentUtils';
import { getId, getDisplayName } from '../utils/OntologyUtils';

let RecordNavigationItem = props => {
  if (props.node.children.length === 0) {
    return <noscript/>
  };

  let category = props.node;
  let parentEnumeration = props.parentEnumeration;
  let id = getId(category);
  let displayName = getDisplayName(category)

  let titleClassnames = classnames({
    'wdk-Record-sidebar-title': true,
    'wdk-Record-sidebar-title__active': props.activeCategory === category
  });

  let visible = props.isVisible(category);
  let collapsed = props.isCollapsed(category);

  let enumeration = parentEnumeration == null
    ? [ props.index + 1 ]
    : [ ...parentEnumeration, props.index + 1 ];

  return (
    <div className="wdk-RecordNavigationItem">

      {parentEnumeration == null && visible &&
        <input
          className="wdk-Record-sidebar-checkbox"
          type="checkbox"
          checked={!collapsed}
          onChange={(e) => void props.onCategoryToggle(category, !e.target.checked)}
        />
      }

      {visible &&
        <a
          href={'#' + id}
          className={titleClassnames}
          onClick={() => {
            if (collapsed) props.onCategoryToggle(category, false);
          }}
        > {enumeration.join('.') + ' ' + displayName} </a>
      }

      {props.showChildren &&
        <div style={{ paddingLeft: enumeration.length + 'em' }}>
          {React.Children.map(props.children, child => React.cloneElement(
            child,
            { parentEnumeration: enumeration }
          ))}
        </div>
      }
    </div>
  );
};

export default wrappable(RecordNavigationItem);
