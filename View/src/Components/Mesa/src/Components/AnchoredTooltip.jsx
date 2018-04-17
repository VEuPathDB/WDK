import React from 'react';

import Tooltip from './Tooltip';

class AnchoredTooltip extends React.Component {
  constructor (props) {
    super(props);
    this.getOffsetPosition = this.getOffsetPosition.bind(this);
  }

  getOffsetPosition (position, offset) {
    if (!typeof offset !== 'object' || typeof position !== 'object') return position;
    const output = {};
    for (let key in position) {
      output[key] = position[key] + (key in offset ? offset[key] : 0);
    };
    return output;
  }

  render () {
    const defaults = { top: 0, left: 0, width: 0, right: 0 };
    const { offset } = this.props;

    const { top, left, width, height, right } = (this.anchor ? Tooltip.getOffset(this.anchor) : defaults);
    const position = { top: top + height, left: right + width };
    const offsetPosition = this.getOffsetPosition(position, offset);
    const tooltipProps = Object.assign({}, this.props, { position: offsetPosition });
    return (
      <div className="AnchoredTooltip" style={{ display: 'inline-block' }}>
        <span className="AnchoredTooltip-Anchor" style={{ float: 'right' }} ref={(a) => this.anchor = a} />
        <Tooltip {...tooltipProps} />
      </div>
    );
  }
};

export default AnchoredTooltip;
