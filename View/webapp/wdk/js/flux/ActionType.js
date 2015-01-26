/**
 * Constants used as action types.
 *
 * The naming convention is to use ALL_CAPS as the identifier, and camelCase as
 * the value. E.g. export const APP_LOADING = 'appLoading'.
 *
 * Reusing an identifier is a syntax error. However, reusing a value is not but
 * should be avoided.
 *
 *
 * These can be imported into other modules in one of two ways:
 *
 *     import { ANSWER_LOADING } from './ActionType';
 *     // do something with ANSWER_LOADING
 *
 * or
 *
 *     import * as ActionType from './ActionType';
 *     // do something with ActionType.ANSWER_LOADING;
 */

export const APP_LOADING = 'appLoading';
export const APP_ERROR = 'appError';

export const ANSWER_INIT = 'answerInit';
export const ANSWER_LOADING = 'answerLoading';
export const ANSWER_LOAD_SUCCESS = 'answerLoadSuccess';
export const ANSWER_LOAD_ERROR = 'answerLoadError';
export const ANSWER_MOVE_COLUMN = 'answerMoveColumn';
export const ANSWER_CHANGE_ATTRIBUTES = 'answerChangeAttributes';

export const QUESTION_LIST_LOADING = 'questionListLoading';
export const QUESTION_LIST_LOAD_SUCCESS = 'questionListLoadSuccess';
export const QUESTION_LIST_LOAD_ERROR = 'questionListLoadError';

export const PROJECT_LOADING = 'projectLoading';
export const PROJECT_LOAD_SUCCESS = 'projectLoadSuccess';
export const PROJECT_LOAD_ERROR = 'projectLoadError';

export const USER_LOADING = 'userLoading';
export const USER_LOAD_SUCCESS = 'userLoadSuccess';
export const USER_LOAD_ERROR = 'userLoadError';
