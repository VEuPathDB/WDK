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
    return !showText ? null : (
      <div className="Tooltip-Text">
        {text}
      </div>
    );
  }

  render () {
    const { children } = this.props;
    const TextBox = this.renderTextBox;
    return (
      <div className="Tooltip" onMouseEnter={this.showTooltip} onMouseLeave={this.hideTooltip}>
        {children}
        <TextBox />
      </div>
    )
  }
};

export default Tooltip;
