/**
 * This a Param module. It exports the following:
 * - reduce
 * - actionCreators
 * - renderer
 */

import { reduce } from './State';
import * as ActionCreators from './ActionCreators';
import ParamComponent from '../../components/FilterParamNew';
import { Parameter, FilterParamNew } from '../../utils/WdkModel';

function isType(parameter: Parameter): parameter is FilterParamNew {
  return parameter.type === 'FilterParamNew';
}

export { ActionCreators, reduce, ParamComponent, isType };