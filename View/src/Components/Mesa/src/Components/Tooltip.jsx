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
    if (this.hideTimeout) clearTimeout(this.hideTimeout);
  }

  engageTooltip () {
    let { showDelay } = this.props;
    showDelay = typeof showDelay === 'number' ? showDelay : 250;
    this.showTimeout = setTimeout(() => {
      this.showTooltip();
      if (this.hideTimeout) clearTimeout(this.hideTimeout);
    }, showDelay);
  }

  disengageTooltip () {
    let { hideDelay } = this.props;
    hideDelay = typeof hideDelay === 'number' ? hideDelay : 500;
    if (this.showTimeout) clearTimeout(this.showTimeout);
    this.hideTimeout = setTimeout(this.hideTooltip, hideDelay);
  }

  hideTooltip () {
    if (!this.id) return;
    const { removeModal } = this.context;
    removeModal(this.id);
    this.id = null;
  }

  renderTooltipBox () {
    const { content, position, style, renderHtml } = this.props;
    let { top, left, right } = position ? position : { top: 0, left: 0, right: 0 };

    const boxStyle = Object.assign({}, {
      top,
      left,
      right,
      display: 'block',
      position: 'absolute',
      pointerEvents: 'auto',
      zIndex: 1000000
    }, style && Object.keys(style).length ? style : {});

    return (
      <div
        style={boxStyle}
        className={'Tooltip-Content'}
        onMouseEnter={this.engageTooltip}
        onMouseLeave={this.disengageTooltip}>
        {renderHtml ? <div dangerouslySetInnerHtml={{ __html: content }} /> : content}
      </div>
    );
  }

  render () {
    const { children } = this.props;
    const className = 'Tooltip' + (this.props.className ? ' ' + this.props.className : '');
    return (
      <div
        tabIndex={0}
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
