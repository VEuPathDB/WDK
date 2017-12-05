/**
 * This a Param module. It exports the following:
 * - reduce
 * - actionCreators
 * - renderer
 */
import { reduce } from './State';
import paramEpic from './ActionCreators';
import { isType } from './Utils';
import ParamComponent from '../../components/FilterParamNew';
import { Parameter, FilterParamNew } from '../../utils/WdkModel';
import { ParamModule } from '../Utils';

export { reduce, ParamComponent, isType, paramEpic };