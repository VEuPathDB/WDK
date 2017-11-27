import { getLeaves } from '../params/FilterParamNew/Utils';
import { debounce, get, memoize, pick } from 'lodash';
import React from 'react';

import { Props as ParamProps } from '../params';
import * as ActionCreators from '../params/FilterParamNew/ActionCreators';
import { MemberFieldState, State } from '../params/FilterParamNew/State';
import { Field, Filter } from '../utils/FilterService';
import { FilterParamNew as TFilterParamNew } from '../utils/WdkModel';
import Loading from './Loading';
import _ServerSideAttributeFilter from './ServerSideAttributeFilter';

const ServerSideAttributeFilter: any = _ServerSideAttributeFilter;

type FieldState = State['fieldStates'][string];

type Props = ParamProps<TFilterParamNew, State>

/**
 * FilterParamNew component
 */
export default class FilterParamNew extends React.PureComponent<Props> {
  constructor(props: Props) {
    super(props);
    this._getFiltersFromValue = memoize(this._getFiltersFromValue);
    this._handleActiveFieldChange = this._handleActiveFieldChange.bind(this);
    this._handleFilterChange = this._handleFilterChange.bind(this);
    this._handleMemberSort = this._handleMemberSort.bind(this);
    this._updateCounts = debounce(this._updateCounts, 1000);
  }

  _getFieldMap = memoize((parameter: Props['parameter']) =>
    new Map(parameter.ontology.map(o => [ o.term, o] as [string, Field])))

  _countLeaves = memoize((parameter: Props['parameter']) =>
    getLeaves(parameter.ontology).toArray().length);

  _getFiltersFromValue(value: Props['value']) {
    let { filters = [] } = JSON.parse(value);
    return filters as Filter[];
  }

  _handleActiveFieldChange(term: string) {
    this.props.dispatch(ActionCreators.updateActiveField(
      this.props.ctx,
      term,
      this._getFiltersFromValue(this.props.value),
      get(this.props.uiState.fieldStates, [ term, 'ontologyTermSummary' ]) == null
    ));
  }

  _handleFilterChange(filters: Filter[]) {
    const {
      ctx,
      dispatch,
      onParamValueChange,
      value,
      uiState: { activeOntologyTerm }
    } = this.props;

    onParamValueChange(JSON.stringify({ filters }));
    this._updateCounts(filters);
  }

  _handleMemberSort(field: Field, sort: MemberFieldState['sort']) {
    this.props.dispatch(ActionCreators.updateMemberFieldSort(this.props.ctx, field.term, this.props.uiState.fieldStates[field.term] as MemberFieldState, sort));
  }

  _updateCounts(filters: Filter[]) {
    const { ctx, dispatch, uiState: { activeOntologyTerm } } = this.props;
    dispatch(ActionCreators.updateFilters(ctx, filters, activeOntologyTerm));
    dispatch(ActionCreators.updateSummaryCounts(ctx, filters));
  }

  render() {
    let { parameter, uiState } = this.props;
    let filters = this._getFiltersFromValue(this.props.value);
    let activeFieldSummary = (
      uiState.activeOntologyTerm &&
      uiState.fieldStates[uiState.activeOntologyTerm].ontologyTermSummary
    );
    let activeFieldState = uiState.activeOntologyTerm != null
        ? pick(uiState.fieldStates[uiState.activeOntologyTerm], 'sort') as Pick<MemberFieldState, 'sort'>
        : undefined;
    let numLeaves = this._countLeaves(parameter);

    return (
      <div className="filter-param">
        {uiState.errorMessage && <pre style={{color: 'red'}}>{uiState.errorMessage}</pre>}
        {uiState.loading && <Loading/>}
        <ServerSideAttributeFilter
          displayName={parameter.filterDataTypeDisplayName || parameter.displayName}

          activeField={uiState.activeOntologyTerm}
          activeFieldSummary={activeFieldSummary}
          activeFieldState={activeFieldState}
          fields={this._getFieldMap(parameter)}
          filters={filters}
          dataCount={uiState.unfilteredCount}
          filteredDataCount={uiState.filteredCount}

          hideFilterPanel={numLeaves === 1}
          hideFieldPanel={numLeaves === 1}

          onFiltersChange={this._handleFilterChange}
          onActiveFieldChange={this._handleActiveFieldChange}
          onMemberSort={this._handleMemberSort}
        />
      </div>
    )
  }
}
