import React from 'react';
import { escape } from 'lodash';
import { withRouter } from 'react-router';
import { History } from 'history';
import { wrappable } from '../utils/componentUtils';
import { UserDataset } from '../utils/WdkModel';
import { bytesToHuman } from '../utils/Converters';
import DataTable from './DataTable';
import Loading from './Loading';
import { User } from "../utils/WdkUser";

type Props = {
  user: User;
  userDatasets: UserDataset[];
  history: History;
};

const getRowId = (row: { id: number }) => row.id;

const na = '<span style="color: lightgray">N/A</span>';

const columns = [
  { name: 'id', displayName: 'ID' },
  { name: 'name', displayName: 'Name' },
  { name: 'summary', displayName: 'Summary' },
  { name: 'type', displayName: 'Type' },
  { name: 'installed', displayName: 'Installed' },
  { name: 'owner', displayName: 'Shared By' },
  { name: 'shared', displayName: 'Shared With Others' },
  { name: 'created', displayName: 'Created' },
  { name: 'modified', displayName: 'Modified' },
  { name: 'size', displayName: 'Size' },
  { name: 'quota', displayName: 'Quota Usage %' }
];

const mapUserDatasets = (userDatasets: UserDataset[], history: History, user: User) =>
  userDatasets.length === 0 ? userDatasets : userDatasets.map(ud => ({
    id: `<a href="${history.createHref({ pathname: `/workspace/datasets/${ud.id}` })}">${ud.id}</a>`,
    name: escape(ud.meta.name),
    summary: escape(ud.meta.summary),
    type: `${ud.type.name} ${ud.type.version}`,
    installed: ud.isInstalled ? 'Yes' : 'No',
    owner: user.id === ud.ownerUserId ? na : ud.owner,
    shared: user.id === ud.ownerUserId ? (ud.sharedWith.length ? 'Yes' : 'No') : na,
    created: new Date(ud.created).toLocaleDateString(),
    modified: new Date(ud.modified).toLocaleDateString(),
    size: bytesToHuman(ud.size),
    quota: ud.percentQuotaUsed == null ? '' : `${ud.percentQuotaUsed}%`
  }));

const UserDatasetList = (props: Props) => (
  <div>
    <h1>User Data Sets</h1>
    <DataTable columns={columns}
               data={mapUserDatasets(props.userDatasets, props.history, props.user)}
               getRowId={getRowId} />
  </div>
)

export default wrappable(withRouter(UserDatasetList));
