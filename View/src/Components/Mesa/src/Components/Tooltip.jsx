import React from 'react';
import PropTypes from 'prop-types';

class Tooltip extends React.Component {
  constructor (props) {
    super(props);
    this.showTooltip = this.showTooltip.bind(this);
    this.hideTooltip = this.hideTooltip.bind(this);
    this.renderTooltipBox = this.renderTooltipBox.bind(this);
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
    if (this.id) return;
    const { addModal } = this.context;
    const textBox = { render: this.renderTooltipBox };
    this.id = addModal(textBox);
  }

  engageTooltip () {
    if (this.timeout) clearTimeout(this.timeout);
    this.showTooltip();
  }

  disengageTooltip () {
    let { hideDelay } = this.props;
    hideDelay = typeof hideDelay === 'number' ? hideDelay : 500;
    this.timeout = setTimeout(this.hideTooltip, hideDelay);
  }

  hideTooltip () {
    if (!this.id) return;
    const { removeModal } = this.context;
    removeModal(this.id);
    this.id = null;
  }

  renderTooltipBox () {
    const { content, position } = this.props;
    let { top, left } = position ? position : { top: 0, left: 0 };

    const boxStyle = {
      top,
      left,
      display: 'block',
      position: 'absolute',
      pointerEvents: 'auto',
      zIndex: 1000000
    };

    return (
      <div
        style={boxStyle}
        className={'Tooltip-Content'}
        onMouseEnter={this.engageTooltip}
        onMouseLeave={this.disengageTooltip}>
        {content}
      </div>
    );
  }

  render () {
    const { children } = this.props;
    const className = 'Tooltip' + (this.props.className ? ' ' + this.props.className : '');
    return (
      <div
        tabindex={0}
        className={className}
        onFocus={this.engageTooltip}
        onBlur={this.disengageTooltip}
        onMouseEnter={this.engageTooltip}
        onMouseLeave={this.disengageTooltip}>
        {children}
      </div>
    )
  }
};

Tooltip.propTypes = {
  hideDelay: PropTypes.number,
  children: PropTypes.node,
  className: PropTypes.string,
  content: PropTypes.node,
  position: PropTypes.object
};

Tooltip.contextTypes = {
  addModal: PropTypes.func,
  removeModal: PropTypes.func
};

export default Tooltip;
