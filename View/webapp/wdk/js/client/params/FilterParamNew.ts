/**
 * This a Param module. It exports the following:
 * - reduce
 * - actionCreators
 * - renderer
 */
import ParamComponent from '../components/FilterParamNew';
import { Parameter, FilterParamNew } from '../utils/WdkModel';
import { ParamModule } from './Utils';

import { reduce } from './FilterParamNew/State';
import paramEpic from './FilterParamNew/ActionCreators';
import { isType } from './FilterParamNew/Utils';

export { reduce, ParamComponent, isType, paramEpic };
