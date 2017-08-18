import React from 'react';
import PropTypes from 'prop-types';

/**
 * Widget for selecting a single number
 */
class NumberSelector extends React.Component {
  constructor (props) {
    super(props);
    this.handleChangeEvent = this.handleChangeEvent.bind(this);
  }

  handleChangeEvent (e) {
    let { onChange, start, end } = this.props;
    let { value } = e.currentTarget;
    value = value * 1;
    start = start * 1;
    end = end * 1;
    if (value < start) value = start;
    if (value > end) value = end;
    if (onChange) onChange(value);
  }

  render () {
    let { value, start, end, step, size } = this.props;
    if (!step) step = 1;
    if (!size) size = end;
    let style = { width: size.toString().length + 'em' };
    return (
      <span className="wdk-NumberSelector">
        <input type="number" style={style} min={start} max={end} step={step} value={value} onChange={this.handleChangeEvent} />
      </span>
    );
  }
}

NumberSelector.propTypes = {
  start: PropTypes.number,
  end: PropTypes.number,
  value: PropTypes.number,
  onChange: PropTypes.func,
  step: PropTypes.number,
  size: PropTypes.number
};

export default NumberSelector;
