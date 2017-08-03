import React from 'react';
import PropTypes from 'prop-types';

import InputRange from 'react-input-range';
import 'react-input-range/lib/css/index.css';

class RangeSelector extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    let { start, end, value, onChange } = this.props;
    return (
      <span className="wdk-NumberRangeSelector">
        <InputRange {...this.props} minValue={start} maxValue={end} value={value} onChange={onChange} />
      </span>
    );
  }
};

RangeSelector.propTypes = {
  start: PropTypes.number,
  end: PropTypes.number,
  value: PropTypes.shape({
    min: PropTypes.number,
    max: PropTypes.number
  }),
  onChange: PropTypes.func
};

export default RangeSelector;
