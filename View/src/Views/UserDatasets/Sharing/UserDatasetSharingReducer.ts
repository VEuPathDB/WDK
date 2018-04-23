import { differenceWith, unionWith } from 'lodash';

import { Action } from 'Utils/ActionCreatorUtils';
import { composeReducers, matchAction } from 'Utils/ReducerUtils';
import { UserDataset, UserDatasetShare } from 'Utils/WdkModel';

import { SharingSuccessAction } from '../UserDatasetsActionCreators';

type State = Record<string, {
  isLoading: boolean;
  resource?: UserDataset
}>;

function isSharingAction(action: Action): action is SharingSuccessAction {
  return action.type === 'user-datasets/sharing-success';
}

export default matchAction([
  [ isSharingAction, composeReducers(handleMethod(false), handleMethod(true)) ]
])

function handleMethod(add: boolean) {
  return function (state: State, action: SharingSuccessAction): State {
    const sharesByTargetId = action.payload.response[add ? 'add' : 'delete'];

    if (sharesByTargetId == null) return state;

    return Object.entries(sharesByTargetId).reduce((state, [userDatasetId, shares]) => {
      const entry = state[userDatasetId];
      // entry can be undefined
      if (entry == null || entry.resource == null || shares == null) {
        return state;
      }
      const operator = add ? unionWith : differenceWith;
      const sharedWith = operator(entry.resource.sharedWith, shares, shareComparator);

      return {
        ...state,
        [userDatasetId]: {
          ...entry,
          resource: {
            ...entry.resource,
            sharedWith
          }
        }
      };
    }, state)
  }
}

function shareComparator(share1: UserDatasetShare, share2: UserDatasetShare) {
  return share1.user === share2.user;
}
