import React from 'react'
import {Breadcrumb } from "designx";
import NodeCard from './components/NodeCard'
import nodeService from 'app/services/nodeService';

export default function NodeList() {
  const [nodeList, setNodeList] = React.useState([]);
  React.useEffect(() => {
    nodeService.getNodeList().then(res => {
      setNodeList(res)
    })
  }, [])
  return <div className="m-sm-30">
    <Breadcrumb
      routeSegments={[
        { name: "节点管理", path: "/node/list" },
        { name: "节点" }
      ]}
    />
    <NodeCard list={nodeList}/>
  </div>
}