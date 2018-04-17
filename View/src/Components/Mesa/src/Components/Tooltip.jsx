import React from 'react';
import PropTypes from 'prop-types';

import { EventsFactory } from '../Utils/Events';

class Tooltip extends React.Component {
  constructor (props) {
    super(props);
    this.showTooltip = this.showTooltip.bind(this);
    this.hideTooltip = this.hideTooltip.bind(this);
    this.renderTooltipBox = this.renderTooltipBox.bind(this);
    this.engageTooltip = this.engageTooltip.bind(this);
    this.disengageTooltip = this.disengageTooltip.bind(this);
    this.componentDidMount = this.componentDidMount.bind(this);
    this.state = { isFocus: false, isHovered: false };
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
    if (!this.el) {
      console.error(`
        Tooltip Error: Can't setup focusIn/focusOut events.
        Element ref could not be found; was render interrupted?
      `);
    } else {
      this.events = new EventsFactory(this.el);
      this.events.use({
        focusIn: () => this.setState({ isFocus: true }),
        keypress: () => this.setState({ isFocus: true }),
        focusOut: () => this.setState({ isFocus: false }),
        mouseEnter: () => this.setState({ isHovered: true }),
        mouseLeave: () => this.setState({ isHovered: false })
      });
    }
  }

  componentWillUnmount () {
    if (this.events) this.events.clearAll();
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
    if (!this.id || this.state.isFocus || this.state.isHovered) return;
    const { removeModal } = this.context;
    removeModal(this.id);
    this.id = null;
  }

  getCornerClass () {
    const { corner } = this.props;
    if (typeof corner !== 'string' || !corner.length) return 'no-corner';
    return corner.split(' ').filter(s => s).join('-');
  }

  renderTooltipBox () {
    const { content, position, style, renderHtml } = this.props;
    let { top, left, right } = position ? position : { top: 0, left: 0, right: 0 };
    const cornerClass = this.getCornerClass();
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
        className={'Tooltip-Content ' + cornerClass}
        onMouseEnter={this.engageTooltip}
        onMouseLeave={this.disengageTooltip}>
        {renderHtml ? <div dangerouslySetInnerHTML={{ __html: content }} /> : content}
      </div>
    );
  }

  render () {
    const { isFocus, isHovered } = this.state;
    if (this.el && (isFocus || isHovered)) this.engageTooltip();
    else this.disengageTooltip();

    const { children, className } = this.props;
    const fullClassName = 'Tooltip' + (className ? ' ' + className : '');
    return (
      <div
        tabIndex={0}
        className={fullClassName}
        ref={(el) => this.el = el}>
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
  corner: PropTypes.string,
  position: PropTypes.object
};

Tooltip.contextTypes = {
  addModal: PropTypes.func,
  removeModal: PropTypes.func
};

export default Tooltip;
