import React from 'react';

import DateUtils from '../utils/DateUtils';
import DateSelector from './DateSelector';

class DateRangeSelector extends React.Component {
  constructor (props) {
    super(props);

    let { start, end } = props;

    start = DateUtils.isValidDateString(start)
      ? DateUtils.parseDate(start)
      : DateUtils.getEpochStart();

    end = DateUtils.isValidDateString(end)
      ? DateUtils.parseDate(end)
      : DateUtils.getEpochEnd();

    this.state = { start, end };
    this.handleMinValueChange = this.handleMinValueChange.bind(this);
    this.handleMaxValueChange = this.handleMaxValueChange.bind(this);
    this.componentWillReceiveProps = this.componentWillReceiveProps.bind(this)
  }

  componentWillReceiveProps (nextProps) {
    let { start, end } = nextProps;
    start = (start === this.props.start)
      ? this.state.start
      : DateUtils.isValidDateString(start)
        ? DateUtils.parseDate(start)
        : DateUtils.getEpochStart();
    end = (end === this.props.end)
      ? this.state.end
      : DateUtils.isValidDateString(end)
        ? DateUtils.parseDate(end)
        : DateUtils.getEpochEnd();
    this.setState({ start, end })
  }

  handleMinValueChange (min) {
    let { onChange, value } = this.props;
    let { max } = value;
    if (onChange) onChange({ min, max });
  }

  handleMaxValueChange (max) {
    let { onChange, value } = this.props;
    let { min } = value;
    if (onChange) onChange({ min, max });
  }

  render () {
    let { min, max } = this.props.value;
    let { start, end } = this.state;
    
    return (
      <div className="wdk-DateRangeSelector">
        <div className="control-row">
          <label>From</label>
          <DateSelector start={start} end={end} value={min} onChange={this.handleMinValueChange} />
        </div>
        <div className="control-row">
          <label>To</label>
          <DateSelector start={start} end={end} value={max} onChange={this.handleMaxValueChange} />
        </div>
      </div>
    )
  }
}

export default DateRangeSelector;
