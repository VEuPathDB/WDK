import React from 'react';
import PropTypes from 'prop-types';

/**
 * Widget for selecting a single number
 */
class NumberSelector extends React.Component {
  constructor (props) {
    super(props);
    this.handleBlurEvent = this.handleBlurEvent.bind(this);
    this.handleChangeEvent = this.handleChangeEvent.bind(this);
    this.state = { internalValue: props.value };
  }

  componentWillReceiveProps(nextProps) {
    this.setState({ internalValue: nextProps.value });
  }

  handleChangeEvent (e) {
    this.setState({ internalValue: e.target.value });
  }

  handleBlurEvent (e) {
    let { onChange, start, end } = this.props;
    let { value } = e.currentTarget;
    value = value * 1;
    start = start * 1;
    end = end * 1;
    if (value < start) value = start;
    if (value > end) value = end;
    this.setState({ internalValue: value });
    if (onChange) onChange(value);
  }

  render () {
    let { start, end, step, size } = this.props;
    let { internalValue: value } = this.state;
    if (!step) step = 1;
    if (!size) size = end + step;
    let width = Math.max(size.toString().length, 4);
    let style = { width: width + 'em' };
    return (
      <span className="wdk-NumberSelector">
        <input
          type="number"
          style={style}
          min={start}
          max={end}
          step={step}
          value={value}
          onChange={this.handleChangeEvent}
          onBlur={this.handleBlurEvent}
        />
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
