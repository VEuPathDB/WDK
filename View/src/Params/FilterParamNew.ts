import observeParam from 'Params/FilterParamNew/ActionCreators';
import Component from 'Params/FilterParamNew/FilterParamNew';
import { reduce } from 'Params/FilterParamNew/State';

import { isParamValueValid, isType } from './FilterParamNew/Utils';
import { createParamModule } from './Utils';

export default createParamModule({
  isType,
  isParamValueValid,
  reduce,
  Component,
  observeParam
});
