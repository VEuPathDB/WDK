/**
 * Creates a container that allows a user to cycle among all tabbable elements.
 * This is useful for components such as dialogs and dropdown menus.
 */
import React from 'react';

let $ = window.jQuery;

let TabbableContainer = React.createClass({

  componentDidMount() {
    this.node = React.findDOMNode(this);
    this.node.focus();
    $(this.node).on('keydown', this.containTab);
  },

  componentWillUnmount() {
    $(this.node).off('keydown', this.containTab);
  },

  // prevent user from tabbing out of dropdown
  containTab(e) {
    if (e.keyCode !== $.ui.keyCode.TAB) {
      return;
    }

    var tabbables = $(':tabbable', this.node),
      first = tabbables.filter(':first'),
      last  = tabbables.filter(':last');

    if (e.target === last[0] && !e.shiftKey) {
      first.focus(1);
      return false;
    } else if (e.target === first[0] && e.shiftKey) {
      last.focus(1);
      return false;
    }
  },

  render() {
    return (
      <div tabIndex="-1" {...this.props}>
        {this.props.children}
      </div>
    );
  }

});

export default TabbableContainer;
