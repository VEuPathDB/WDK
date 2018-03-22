import { DispatchAction } from 'Core/CommonTypes';
import { Epic } from 'Utils/ActionCreatorUtils';
import { Parameter, ParameterValues } from 'Utils/WdkModel';

// Types
// -----

// FIXME Add full question, paramUIState and groupUIState
export type Context<T extends Parameter> = {
  questionName: string;
  parameter: T;
  paramValues: ParameterValues;
}

export type Props<T extends Parameter, S> = {
  ctx: Context<T>;
  parameter: T;
  value: string;
  uiState: S;
  dispatch: DispatchAction;
  onParamValueChange: (value: string) => void;

}

export type ParamModule<T extends Parameter, S> = {
  isType: (p: Parameter) => p is T;
  ParamComponent: React.ComponentType<Props<T, S>>;
  reduce?: (state: S, action: any) => S;
  paramEpic?: Epic
}


// Type guards (see https://www.typescriptlang.org/docs/handbook/advanced-types.html#user-defined-type-guards)
// -----------------------------------------------------------------------------------------------------------

export function isPropsType<T extends Parameter>(
  props: Props<Parameter, any>,
  predicate: (parameter: Parameter) => parameter is T
): props is Props<T, any> {
  return predicate(props.parameter);
}

export function isContextType<T extends Parameter>(
  context: Context<Parameter>,
  predicate: (parameter: Parameter) => parameter is T
): context is Context<T> {
  return predicate(context.parameter);
}
