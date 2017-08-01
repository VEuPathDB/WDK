import React from 'react';
import PropTypes from 'prop-types';

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
    let { value, onChange, start, end, step } = this.props;
    if (!step) step = 1;
    return (
      <span className="wdk-NumberSelector">
        <input type="number" min={start} max={end} step={step} value={value} onChange={this.handleChangeEvent} />
      </span>
    );
  }
};

NumberSelector.propTypes = {
  start: PropTypes.number,
  end: PropTypes.number,
  value: PropTypes.number,
  onChange: PropTypes.func
};

export default NumberSelector;
