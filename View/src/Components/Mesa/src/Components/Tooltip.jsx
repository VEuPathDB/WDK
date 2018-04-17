import React from 'react';

function getRealOffset (el) {
  let top = 0;
  let left = 0;
  do {
    top += el.offsetTop || 0;
    left += el.offsetLeft || 0;
    el = el.offsetParent;
  } while (el);
  return { top, left };
};

class Tooltip extends React.Component {
  constructor (props) {
    super(props);
    this.state = { showText: false }
    this.showTooltip = this.showTooltip.bind(this);
    this.hideTooltip = this.hideTooltip.bind(this);
    this.renderTextBox = this.renderTextBox.bind(this);
  }

  showTooltip () {
    const showText = true;
    this.setState({ showText })
  }

  hideTooltip () {
    const showText = false;
    this.setState({ showText });
  }

  renderTextBox () {
    const { text } = this.props;
    const { showText } = this.state;

    const position = this.anchor ? getRealOffset(this.anchor) : { top: 0, left: 0 };

    const wrapperStyle = {
      border: '1px solid orange',
      position: 'absolute',
      top: 0,
      left: 0,
      width: '100vw',
      height: '100%',
      pointerEvents: 'none',
      backgroundColor: 'rgba(0, 0,0,0.4)'
    };

    const textStyle = {
      position: relative,
      top: position.top + 'px',
      left: position.left + 'px'
    };

    return !showText ? null : (
      <div className="Tooltip-Wrapper" style={wrapperStyle}>
        <div className="Tooltip-Text" style={textStyle}>
          {text}
        </div>
      </div>
    );
  }

  render () {
    const { children } = this.props;
    const className = 'Tooltip' + (this.props.className ? ' ' + this.props.className : '');
    const TextBox = this.renderTextBox;
    return (
      <div
        ref={(ref) => this.anchor = ref}
        className={className}
        onMouseEnter={this.showTooltip}
        onMouseLeave={this.hideTooltip}>
        {children}
        <TextBox />
      </div>
    )
  }
};

export default Tooltip;
