import React from 'react';
import PropTypes from 'prop-types';

import NumberSelector from './NumberSelector';

class NumberRangeSelector extends React.Component {
  constructor (props) {
    super(props);

    this.handleMinChange = this.handleMinChange.bind(this);
    this.handleMaxChange = this.handleMaxChange.bind(this);
  }

  handleMinChange (min) {
    let { value, onChange } = this.props;
    let { max } = value;
    max = max * 1;
    min = min * 1;
    if (onChange) onChange({ min, max });
  }

  handleMaxChange (max) {
    let { value, onChange } = this.props;
    let { min } = value;
    max = max * 1;
    min = min * 1;
    if (onChange) onChange({ min, max });
  }

  render () {
    let { start, end, value, onChange } = this.props;
    return (
      <div className="wdk-NumberRangeSelector">
        <NumberSelector start={start} end={end} onChange={this.handleMinChange} value={value.min} />
        <label>&nbsp; to &nbsp;</label>
        <NumberSelector start={start} end={end} onChange={this.handleMaxChange} value={value.max} />
      </div>
    );
  }
};

NumberRangeSelector.propTypes = {
  start: PropTypes.number,
  end: PropTypes.number,
  value: PropTypes.shape({
    min: PropTypes.number,
    max: PropTypes.number
  }),
  onChange: PropTypes.func
};

export default NumberRangeSelector;
