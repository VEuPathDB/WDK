import React from 'react';
import PropTypes from 'prop-types';

class NumberSelector extends React.Component {
  constructor (props) {
    super(props);
    this.handleChangeEvent = this.handleChangeEvent.bind(this);
  }

  handleChangeEvent (e) {
    const { onChange, start, end } = this.props;
    const { value } = e.currentTarget;
    value = value * 1;
    start = start * 1;
    end = end * 1;
    if (value < start) value = start;
    if (value > end) value = end;
    if (onChange) onChange(value);
  }

  render () {
    let { value, onChange, start, end } = this.props;
    return (
      <span className="wdk-NumberSelector">
        <input type="number" min={start} max={end} value={value} onChange={this.handleChangeEvent} />
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
