import React from 'react';
import PropTypes from 'prop-types';

import NumberSelector from './NumberSelector';

class NumberRangeSelector extends React.Component {
  constructor (props) {
    super(props);
  }

  handleMinChange (min) {
    let { value, onChange } = this.props;
    let { max } = value;
    if (onChange) onChange({ min, max });
  }

  handleMaxChange (max) {
    let { value, onChange } = this.props;
    let { min } = value;
    if (onChange) onChange({ min, max });
  }

  render () {
    let { start, end, value, onChange } = this.props;
    return (
      <div className="wdk-NumberRangeSelector wdk-ControlGrid">
        <div className="label-column">
          <div className="label-cell">
            <label>From</label>
          </div>
          <div className="label-cell">
            <label>To</label>
          </div>
        </div>
        <div className="control-column">
          <div className="control-cell">
            <NumberSelector start={start} end={end} onChange={this.handleMinChange} value={value.min} />
          </div>
          <div className="control-cell">
            <NumberSelector start={start} end={end} onChange={this.handleMinChange} value={value.max} />
          </div>
        </div>
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
