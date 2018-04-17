import React from 'react';

import Tooltip from './Tooltip';

class AnchoredTooltip extends React.Component {
  constructor (props) {
    super(props);
    this.getOffsetPosition = this.getOffsetPosition.bind(this);
  }

  getOffsetPosition (position, offset) {
    const output = {};
    for (let key in position) {
      output[key] = position[key] + (key in offset ? offset[key] : 0);
    };
    return output;
  }

  render () {
    const defaults = { top: 0, left: 0 };
    const { children, content, style, offset } = this.props;

    const { top, width, right, left } = this.anchor ? Tooltip.getOffset(this.anchor) : defaults;
    const position = { top, left: left + (width)  };
    const offsetPosition = offset ? this.getOffsetPosition(position, offset) : position;
    const tooltipProps = { content, style, children, position: offsetPosition };
    return (
      <div className="AnchoredTooltip" style={{ display: 'inline-block' }}>
        <Tooltip {...tooltipProps} />
        <span ref={(a) => this.anchor = a} />
      </div>
    );
  }
};

export default AnchoredTooltip;
