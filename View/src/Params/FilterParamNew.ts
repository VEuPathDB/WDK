/**
 * This a Param module. It exports the following:
 * - reduce
 * - actionCreators
 * - renderer
 */
import ParamComponent from 'Params/FilterParamNew/FilterParamNew';
import { Parameter, FilterParamNew } from 'Utils/WdkModel';
import { ParamModule } from './Utils';

import { reduce } from 'Params/FilterParamNew/State';
import paramEpic from 'Params/FilterParamNew/ActionCreators';
import { isType } from './FilterParamNew/Utils';

export { reduce, ParamComponent, isType, paramEpic };
