/**
 * Sidebar component that opens and closes
 */
import React from 'react';
import { wrappable } from '../utils/componentUtils';

let Sidebar = React.createClass({

  render() {
    return (
      <aside className="wdk-PageColumn wdk-PageColumn-sidebar">
        {this.props.children}
      </aside>
    );
  }

});

export default wrappable(Sidebar);
