import { differenceWith, flow, unionWith } from 'lodash';

import { UserDataset, UserDatasetShare } from 'Utils/WdkModel';

import { SharingSuccessAction } from '../UserDatasetsActionCreators';

type State = Record<string, {
  isLoading: boolean;
  resource?: UserDataset
}>;

export default function reduce(state: State, action: SharingSuccessAction): State {
  switch(action.type) {

    case 'user-datasets/sharing-success': {
      const { response } = action.payload;
      const compositeHandler = flow(
        handleMethod(response.add, true),
        handleMethod(response.delete, false)
      );
      return compositeHandler(state);
    }
    default: return state;
  }
}

function handleMethod(sharesBySelfId: Record<string, UserDatasetShare[] | void>, add: boolean) {
  return function (state: State): State {
    if (sharesBySelfId == null) return state;

    return Object.entries(sharesBySelfId).reduce((userDatasetsById, [userDatasetId, shares]) => {
      const entry = userDatasetsById[userDatasetId];
      if (entry.resource == null || shares == null) {
        return userDatasetsById;
      }
      const operator = add ? unionWith : differenceWith;
      const sharedWith = operator(entry.resource.sharedWith, shares, shareComparator);

      return Object.assign(userDatasetsById, {
        [userDatasetId]: {
          ...entry,
          resource: {
            ...entry.resource,
            sharedWith
          }
        }
      })
    }, { ...state })
  }
}

function shareComparator(share1: UserDatasetShare, share2: UserDatasetShare) {
  return share1.user === share2.user;
}
