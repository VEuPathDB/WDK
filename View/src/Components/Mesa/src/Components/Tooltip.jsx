import React from 'react';
import PropTypes from 'prop-types';
class Tooltip extends React.Component {
  constructor (props) {
    super(props);
    this.state = { showText: false }
    this.showTooltip = this.showTooltip.bind(this);
    this.hideTooltip = this.hideTooltip.bind(this);
    this.renderTextBox = this.renderTextBox.bind(this);
    this.engageTooltip = this.engageTooltip.bind(this);
    this.disengageTooltip = this.disengageTooltip.bind(this);
    this.componentDidMount = this.componentDidMount.bind(this);
  }

  static getOffset (node) {
    return node.getBoundingClientRect();
  }

  componentDidMount () {
    const { addModal, removeModal } = this.context;
    if (typeof addModal !== 'function' || typeof removeModal !== 'function') {
      throw new Error(`
        Tooltip Error: No "addModal" or "removeModal" detected in context.
        Please use a <ModalBoundary> in your element tree to catch modals.
      `);
    }
  }

  showTooltip () {
    this.engageTooltip();
    if (this.id) return;
    const { addModal } = this.context;
    const textBox = { render: this.renderTextBox };
    this.id = addModal(textBox);
  }

  engageTooltip () {
    console.log('calling engagetooltip');
    if (this.timeout) clearTimeout(this.timeout);
  }

  disengageTooltip () {
    console.log('calling disengagetooltip');
    this.timeout = setTimeout(this.hideTooltip, 1000);
  }

  hideTooltip () {
    if (!this.id) return;
    const { removeModal } = this.context;
    removeModal(this.id);
    this.id = null;
  }

  renderTextBox () {
    const { text, position } = this.props;
    let { top, left } = position ? position : { top: 0, left: 0 };

    const textStyle = {
      top,
      left,
      display: 'block',
      position: 'absolute',
      zIndex: 1000000
    };

    return (
      <div
        style={textStyle}
        className="Tooltip-Text"
        onMouseEnter={this.showTooltip}
        onMouseLeave={this.disengageTooltip}>
        {text}
      </div>
    );
  }

  render () {
    const { children } = this.props;
    const className = 'Tooltip' + (this.props.className ? ' ' + this.props.className : '');
    return (
      <div
        className={className}
        onMouseEnter={this.showTooltip}
        onMouseLeave={this.disengageTooltip}>
        {children}
      </div>
    )
  }
};

Tooltip.contextTypes = {
  addModal: PropTypes.func,
  removeModal: PropTypes.func
};

export default Tooltip;
