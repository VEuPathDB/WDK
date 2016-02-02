/**
 * Creates a container that allows a user to cycle among all tabbable elements.
 * This is useful for components such as dialogs and dropdown menus.
 */
import React from 'react';
import ReactDOM from 'react-dom';
import { wrappable } from '../utils/componentUtils';

let $ = window.jQuery;

let TabbableContainer = React.createClass({

  componentDidMount() {
    let node = ReactDOM.findDOMNode(this);
    this.tabbables = $(':tabbable', node);
    node.focus();
  },

  handleKeyDown(e) {
    if (typeof this.props.onKeyDown === 'function') {
      this.props.onKeyDown(e);
    }

    this.containTab(e);
  },

  // prevent user from tabbing out of dropdown
  // manually tab since os x removes some controls from the tabindex by default
  containTab(e) {
    if (e.key !== 'Tab') { return; }
    let { tabbables } = this;
    let l = tabbables.length;
    let index = tabbables.index(e.target);
    let inc = e.shiftKey ? l - 1 : 1;
    let nextIndex = (index + inc) % l;
    let nextTarget = tabbables[nextIndex];
    if (nextTarget == null) {
      nextTarget = tabbables[0];
    }
    nextTarget.focus();
    e.preventDefault();
  },

  render() {
    return (
      <div tabIndex="-1" {...this.props} onKeyDown={this.handleKeyDown}>
        {this.props.children}
      </div>
    );
  }

});

export default wrappable(TabbableContainer);
