import React from 'react';
import PropTypes from 'prop-types';
import { clamp, debounce, get } from 'lodash';

import Histogram from './Histogram';
import FilterLegend from './FilterLegend';

/**
 * Generic Histogram field component
 *
 * TODO Add binning
 * TODO Use bin size for x-axis scale <input> step attribute
 * TODO Interval snapping
 */
export default class HistogramField extends React.Component {

  static getHelpContent(props) {
    return (
      <div>
        Select a range of {props.field.display} values with the graph below.
      </div>
    );
   /*
   return (
      <div>
        <div>
          The graph below shows the distribution of {props.field.display} values.
          The red bar indicates the number of {props.displayName} that have the
          {props.field.display} value and your other selection criteria.
        </div>
        <div>
          The slider to the left of the graph can be used to scale the Y-axis.
        </div>
      </div>
    )
    */
  }

  constructor(props) {
    super(props);
    this.updateFilterValueFromSelection = debounce(this.updateFilterValueFromSelection.bind(this), 50);
    this.handleMinInputBlur = this.handleMinInputBlur.bind(this);
    this.handleMinInputKeyPress = this.handleMinInputKeyPress.bind(this);
    this.handleMinInputChange = this.handleMinInputChange.bind(this)
    this.handleMaxInputBlur = this.handleMaxInputBlur.bind(this);
    this.handleMaxInputKeyPress = this.handleMaxInputKeyPress.bind(this);
    this.handleMaxInputChange = this.handleMaxInputChange.bind(this)
    this.handleUnknownCheckboxChange = this.handleUnknownCheckboxChange.bind(this)
    this.handleRangeScaleChange = this.handleRangeScaleChange.bind(this);
    this.cacheDistributionOperations(this.props);

    this.state = {
      includeUnknown: get(props.filter, 'includeUnknown', true),
      minInputValue: get(props.filter, 'value.min', this.distributionRange.min),
      maxInputValue: get(props.filter, 'value.max', this.distributionRange.max)
    };
  }

  componentWillReceiveProps(nextProps) {
    let distributionChanged = this.props.distribution !== nextProps.distribution;
    let filterChanged = this.props.filter !== nextProps.filter;

    if (distributionChanged) this.cacheDistributionOperations(nextProps);

    if (distributionChanged || filterChanged) {
      this.setState({
        minInputValue: get(nextProps.filter, 'value.min', this.distributionRange.min),
        maxInputValue: get(nextProps.filter, 'value.max', this.distributionRange.max)
      });
    }
  }

  cacheDistributionOperations(props) {
    this.convertedDistribution = props.distribution.map(entry =>
      Object.assign({}, entry, { value: props.toHistogramValue(entry.value)}));
    var values = this.convertedDistribution.map(entry => entry.value);
    var min = Math.min(...values);
    var max = Math.max(...values);
    this.convertedDistributionRange = { min, max };
    this.distributionRange = { min: props.toFilterValue(min), max: props.toFilterValue(max) };
  }

  formatRangeValue(value) {
    const { min, max } = this.convertedDistributionRange;
    return value ? this.props.toFilterValue(clamp(this.props.toHistogramValue(value), min, max)) : null;
  }

  handleMinInputChange(event) {
    this.setState({ minInputValue: event.target.value });
  }

  handleMinInputBlur() {
    this.updateFilterValueFromState();
  }

  handleMinInputKeyPress(event) {
    if (event.key === 'Enter') this.updateFilterValueFromState();
  }

  handleMaxInputChange(event) {
    this.setState({ maxInputValue: event.target.value });
  }

  handleMaxInputBlur() {
    this.updateFilterValueFromState();
  }

  handleMaxInputKeyPress(event) {
    if (event.key === 'Enter') this.updateFilterValueFromState();
  }

  handleRangeScaleChange(range) {
    if (this.props.onRangeScaleChange != null) {
      this.props.onRangeScaleChange(this.props.field, range);
    }
  }

  updateFilterValueFromState() {
    const min = this.formatRangeValue(this.state.minInputValue);
    const max = this.formatRangeValue(this.state.maxInputValue);
    this.updateFilterValue({ min, max });
  }

  updateFilterValueFromSelection(range) {
    const min = this.formatRangeValue(range.min);
    const max = this.formatRangeValue(range.max);
    this.updateFilterValue({ min, max });
  }

  updateFilterValue(range) {
    // only emit change if range differs from filter
    if (this.rangeIsDifferent(range)) this.emitChange(range);
  }

  /**
   * @param {React.ChangeEvent.<HTMLInputElement>} event
   */
  handleUnknownCheckboxChange(event) {
    const includeUnknown = event.target.checked;
    this.setState({ includeUnknown });
    this.emitChange(get(this.props, 'filter.value'), includeUnknown);
  }

  rangeIsDifferent({ min, max }) {
    return this.props.filter == null
      ? min > this.distributionRange.min || max < this.distributionRange.max
      : min !== this.props.filter.min || max !== this.props.filter.max;
  }

  emitChange(range, includeUnknown = this.state.includeUnknown) {
    // Use range if strict subset, otherwise use undefined, which indicates
    // that the user wants everything known.
    const filterValue = (
      range &&
      range.min <= this.distributionRange.min &&
      range.max >= this.distributionRange.max
    ) ? undefined : range;

    this.props.onChange(this.props.field, filterValue, includeUnknown);

    this.setState({
      minInputValue: get(filterValue, 'min', this.distributionRange.min),
      maxInputValue: get(filterValue, 'max', this.distributionRange.max)
    });
  }

  render() {
    var {
      field,
      filter,
      displayName,
      unknownCount,
      fieldState,
      selectByDefault
    } = this.props;

    var distMin = this.distributionRange.min;
    var distMax = this.distributionRange.max;

    var filterValue = get(filter, 'value');

    var min = filterValue == null
      ? (selectByDefault ? distMin : null)
      : filterValue.min;

    var max = filterValue == null
      ? (selectByDefault ? distMax : null)
      : filterValue.max;

    var includeUnknown = get(filter, 'includeUnknown', this.state.includeUnknown);

    var selectedMin = min == null ? null : this.props.toHistogramValue(min);
    var selectedMax = max == null ? null : this.props.toHistogramValue(max);

    var selectionTotal = filter && filter.selection && filter.selection.length;

    var selection = selectionTotal != null
      ? " (" + selectionTotal + " selected) "
      : null;

    return (
      <div className="range-filter">

        <div className="overview">
          {this.props.overview}
        </div>

        <div>
          {'Select ' + field.display + ' between '}
          <input
            type="text"
            size="6"
            placeholder={distMin}
            value={this.state.minInputValue || ''}
            onChange={this.handleMinInputChange}
            onKeyPress={this.handleMinInputKeyPress}
            onBlur={this.handleMinInputBlur}
          />
          {' and '}
          <input
            type="text"
            size="6"
            placeholder={distMax}
            value={this.state.maxInputValue || ''}
            onChange={this.handleMaxInputChange}
            onKeyPress={this.handleMaxInputKeyPress}
            onBlur={this.handleMaxInputBlur}
          />
          {unknownCount > 0 && (
            <label className="include-unknown">
              {' '}
              <input
                type="checkbox"
                checked={includeUnknown}
                onChange={this.handleUnknownCheckboxChange}
              /> Include {unknownCount} Unknown
            </label>
          )}
          <span className="selection-total">{selection}</span>
        </div>

        <Histogram
          distribution={this.convertedDistribution}
          onSelected={this.updateFilterValueFromSelection}
          selectedMin={selectedMin}
          selectedMax={selectedMax}
          chartType={field.type}
          timeformat={this.props.timeformat}
          xaxisLabel={field.display}
          yaxisLabel={displayName}
          uiState={fieldState}
          onUiStateChange={this.handleRangeScaleChange}
        />

        <FilterLegend {...this.props} />

      </div>
    );
  }

}

HistogramField.propTypes = {
  distribution: PropTypes.array.isRequired,
  toFilterValue: PropTypes.func.isRequired,
  toHistogramValue: PropTypes.func.isRequired,
  selectByDefault: PropTypes.bool.isRequired,
  onChange: PropTypes.func.isRequired,
  field: PropTypes.object.isRequired,
  filter: PropTypes.object,
  overview: PropTypes.node.isRequired,
  displayName: PropTypes.string.isRequired,
  unknownCount: PropTypes.number.isRequired,
  timeformat: PropTypes.string,
  fieldState: PropTypes.object,
  onRangeScaleChange: PropTypes.func
};
