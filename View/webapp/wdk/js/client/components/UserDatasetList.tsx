import React from 'react';
import { withRouter, InjectedRouter } from 'react-router';
import { wrappable } from '../utils/componentUtils';
import { UserDataset } from '../utils/WdkModel';
import DataTable from './DataTable';
import Loading from './Loading';

type Props = {
  userDatasets: UserDataset[],
  router: InjectedRouter
};

const getRowId = (row: { id: number }) => row.id;

const columns = [
  { name: 'id', displayName: 'ID' },
  { name: 'name', displayName: 'Name' },
  { name: 'summary', displayName: 'Summary' },
  { name: 'type', displayName: 'Type' },
  { name: 'installed', displayName: 'Installed' },
  { name: 'owner', displayName: 'Owner' },
  { name: 'shared', displayName: 'Shared' },
  { name: 'created', displayName: 'Created' },
  { name: 'modified', displayName: 'Modified' },
  { name: 'size', displayName: 'Size' },
  { name: 'quota', displayName: 'Quota Usage' }
];

const mapUserDatasets = (userDatasets: UserDataset[], router: InjectedRouter) =>
  userDatasets.length === 0 ? userDatasets : userDatasets.map(ud => ({
    id: `<a href="${router.createHref(`/workspace/datasets/${ud.id}`)}">${ud.id}</a>`,
    name: ud.meta.name,
    summary: ud.meta.summary,
    type: Object.keys(ud.type)[0],
    installed: '&mdash;',
    owner: ud.owner,
    shared: ud.sharedWith.join(', '),
    created: new Date(ud.created).toDateString(),
    modified: new Date(ud.modified).toDateString(),
    size: ud.size,
    quota: ud.percentQuotaUsed
  }));

const UserDatasetList = (props: Props) =>
  <div>
    <h1>User Data Sets</h1>
    <DataTable columns={columns}
               data={mapUserDatasets(props.userDatasets, props.router)}
               getRowId={getRowId} />

    {/* TODO For development - remove before release */}
    <div style={{ marginTop: '1em' }}>
      <details>
        <summary>Raw data used for table</summary>
        <pre>{JSON.stringify(props.userDatasets, null, 4)}</pre>
      </details>
    </div>
  </div>

export default wrappable(withRouter(UserDatasetList));
