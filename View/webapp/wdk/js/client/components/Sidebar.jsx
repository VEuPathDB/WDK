/**
 * Sidebar component that opens and closes
 */
import React from 'react';
import { wrappable } from '../utils/componentUtils';

function Sidebar(props) {
  return (
    <aside className="wdk-PageColumn wdk-PageColumn-sidebar">
      {props.children}
    </aside>
  );
}

export default wrappable(Sidebar);
