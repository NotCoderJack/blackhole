import React from 'react'
import {Breadcrumb } from "designx";
import ClusterCard from '../cluster/components/ClusterCard'
export default function NodeList() {
  return <div className="m-sm-30">
    <Breadcrumb
      routeSegments={[
        { name: "节点管理", path: "/node/list" },
        { name: "节点" }
      ]}
    />
    <ClusterCard list={[]}/>
  </div>
}