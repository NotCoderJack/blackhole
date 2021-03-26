import React from 'react'
import {Breadcrumb} from 'designx';
import ClusterCard from './components/ClusterCard'
import clusterService from "../../services/clusterService";

export default function ClusterList() {
  const [list, setList] = React.useState([]);
  React.useEffect(() => {
    clusterService.getClusterList().then(data => {
      setList(data)
    });
  }, [])
  return <div className="m-sm-30">
    <Breadcrumb
      routeSegments={[
        { name: "集群管理", path: "/cluster/list" },
        { name: "集群" }
      ]}
    />
    <ClusterCard list={list}/>
  </div>
}