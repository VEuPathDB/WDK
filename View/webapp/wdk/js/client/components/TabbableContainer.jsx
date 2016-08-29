import { PropTypes, Component } from 'react';
import $ from 'jquery';
import { wrappable } from '../utils/componentUtils';

/**
 * Creates a container that allows a user to cycle among all tabbable elements.
 * This is useful for components such as dialogs and dropdown menus.
 */
class TabbableContainer extends Component {

  constructor(props) {
    super(props);
    this.node = null;
    this.handleKeyDown = this.handleKeyDown.bind(this);
  }

  componentDidMount() {
    this.node.focus();
  }

  handleKeyDown(e) {
    if (typeof this.props.onKeyDown === 'function') {
      this.props.onKeyDown(e);
    }

    this.containTab(e);
  }

  // prevent user from tabbing out of dropdown
  // manually tab since os x removes some controls from the tabindex by default
  containTab(e) {
    if (e.key !== 'Tab') { return; }
    let tabbables = $(':tabbable', this.node);
    let l = tabbables.length;
    let index = tabbables.index(e.target);
    let delta = e.shiftKey ? l - 1 : 1;
    let nextIndex = (index + delta) % l;
    let nextTarget = tabbables[nextIndex];
    if (nextTarget == null) {
      nextTarget = tabbables[0];
    }
    nextTarget.focus();
    e.preventDefault();
  }

  render() {
    return (
      <div ref={node => this.node = node} tabIndex="-1" {...this.props} onKeyDown={this.handleKeyDown}>
        {this.props.children}
      </div>
    );
  }

}

TabbableContainer.propTypes = {
  onKeyDown: PropTypes.func,
  children: PropTypes.oneOfType([
    PropTypes.element,
    PropTypes.arrayOf(PropTypes.element)
  ]).isRequired
};

export default wrappable(TabbableContainer);
