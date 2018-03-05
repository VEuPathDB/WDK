import { curry, get, includes, keyBy, uniq } from 'lodash';
import React from 'react';
import { MesaController as Mesa } from 'mesa';

import { Seq } from 'Utils/IterableUtils';
import Tooltip from 'Components/Overlays/Tooltip';
import { isRange } from 'Components/AttributeFilter/FilterServiceUtils';

import StackedBar from 'Components/AttributeFilter/internal/AttributeFilter/StackedBar';
import { shouldAddFilter } from 'Components/AttributeFilter/internal/AttributeFilter/Utils';

const getCountType = curry((countType, summary, value) =>
  get(summary.valueCounts.find(count => count.value === value), countType, NaN))
const getCount = getCountType('count');
const getFilteredCount = getCountType('filteredCount');

export default class MultiFieldFilter extends React.Component {

  constructor(props) {
    super(props);
  }

  // Invoke callback with filters array
  handleFieldFilterChange(field, value, includeUnknown, valueCounts) {
    this.props.onFiltersChange(this.updateFilter(this.props.filters, { field, value, includeUnknown, valueCounts }));
  }

  // Returns a new filters array with the provided filter details included
  updateFilter(filters, { field, value, includeUnknown, valueCounts }) {
    let nextFilters = filters.filter(f => f.field !== field.term);
    return shouldAddFilter(field, value, includeUnknown,
      valueCounts, this.props.selectByDefault)
      ? nextFilters.concat({ field: field.term, type: field.type, isRange: isRange(field), value, includeUnknown })
      : nextFilters;
  }

  render() {
    const filtersByField = keyBy(this.props.filters, 'field');
    const values = Seq.from(this.props.activeFieldSummary)
      .flatMap(summary => summary.valueCounts)
      .map(valueCountsEntry => valueCountsEntry.value)
      .uniq()
      .toArray();

    return <div className="wdk-MultiFieldFilter">
      <Mesa
        options={{
          useStickyHeader: true,
          tableBodyMaxHeight: '80vh'
        }}
        eventHandlers={{
          onSort: () => {}
        }}
        rows={this.props.activeFieldSummary}
        filteredRows={this.props.activeFieldSummary}
        columns={[
          {
            key: 'display',
            name: this.props.activeField.display,
            className: 'wdk-MultiFieldFilterDisplayCell',
            renderCell: ({ row }) => this.props.fields.get(row.term).display
          },
          ...values.map((value, index) => ({
            key: value,
            className: 'wdk-MultiFieldFilterValueCell',
            renderHeading: () => {
              // checked if all child fields include value in associated filter
              const checked = this.props.activeFieldSummary.every(childSummary => {
                const childFilter = filtersByField[childSummary.term];
                return (
                  childFilter != null &&
                  childFilter.value != null &&
                  childFilter.value.includes(value)
                );
              });
              const handleChange = event => {
                // add value to all child filters
                const filters = this.props.activeFieldSummary
                  .reduce((prevFilters, childSummary) => {
                    const childField = this.props.fields.get(childSummary.term);
                    const childFilter = prevFilters.find(filter => filter.field === childSummary.term);
                    const childFilterValue = childFilter && childFilter.value
                      ? ( event.target.checked
                        ? uniq(childFilter.value.concat(value))
                        : childFilter.value.filter(v => v !== value)
                      )
                      : ( event.target.checked
                        ? [ value ]
                        : []
                      );
                    return this.updateFilter(prevFilters, {
                      field: childField,
                      value: childFilterValue,
                      includeUnknown: false,
                      valueCounts: childSummary.valueCounts
                    });
                  }, this.props.filters);
                this.props.onFiltersChange(filters)
              }
              return (
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>{value}&nbsp;</div>
                  <Tooltip
                    content={`All ${this.props.activeField.display} = ${value}`}
                    showDelay={0}
                    hideDelay={0}
                  >
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={handleChange}
                    />
                  </Tooltip>
                </div>
              );
            },
            renderCell: ({ row }) => {
              const field = this.props.fields.get(row.term);
              const filter = filtersByField[row.term];
              const filterValue = filter ? filter.value : [];
              const fieldValues = row.valueCounts.map(count => count.value);
              const checked = includes(filterValue, value);
              const disabled = !fieldValues.includes(value);
              const handleChange = event =>
                this.handleFieldFilterChange(
                  this.props.fields.get(row.term),
                  ( event.target.checked
                    ? [value].concat(filterValue)
                    : filterValue.filter(item => item !== value)
                  ),
                  false,
                  row.valueCounts
                );
              return (
                <div style={{ marginTop: index * 2 + 'em', lineHeight: '2em' }}>
                  <Tooltip
                    content={`${field.display} = ${value}`}
                    position={{ at: 'left center', my: 'right center' }}
                    showDelay={0}
                    hideDelay={0}
                  >
                    <input
                      type="checkbox"
                      disabled={disabled}
                      checked={checked && !disabled}
                      onChange={handleChange}
                    />
                  </Tooltip>
                </div>
              )
            }
          })),
          {
            key: 'filtered',
            name: <div>Remaining {this.props.displayName}</div>,
            renderCell: ({ row }) => values.map(value =>
              <div style={{ lineHeight: '2em' }} key={value}>
                {getFilteredCount(row, value).toLocaleString()} <small>({Math.round(getFilteredCount(row, value)/(row.internalsCount || this.props.dataCount) * 100)}%)</small>
              </div>
            )
          },
          {
            key: 'total',
            name: <div>All {this.props.displayName}</div>,
            renderCell: ({ row }) => values.map(value =>
              <div style={{ lineHeight: '2em' }} key={value}>
                {getCount(row, value).toLocaleString()} <small>({Math.round(getCount(row, value)/(row.internalsCount || this.props.dataCount) * 100)}%)</small>
              </div>
            )
          },
          {
            key: 'distribution',
            name: 'Distribution',
            renderCell: ({ row }) => values.map(value =>
              <div style={{ height: '2em', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                <StackedBar
                  count={getCount(row, value)}
                  filteredCount={getFilteredCount(row, value)}
                  populationSize={row.internalsCount || this.props.dataCount}
                />
              </div>
            )
          },
          {
            key: '%',
            name: '%',
            renderCell: ({ row }) => values.map(value =>
              <div style={{ lineHeight: '2em' }} key={value}>
                {String(getFilteredCount(row, value) / getCount(row, value) * 100)}%
              </div>
            )
          }
        ]}
      />
    </div>
  }
};
