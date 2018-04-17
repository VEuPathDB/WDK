import React from 'react';

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
    const wrapperStyle = {
      border: '1px solid orange',
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100vw',
      height: '100vh',
      pointerEvents: 'none',
      backgroundColor: 'rgba(0, 0,0,0.4)'
    };

    return !showText ? null : (
      <div className="Tooltip-Wrapper" style={wrapperStyle}>
        <div className="Tooltip-Text">
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
