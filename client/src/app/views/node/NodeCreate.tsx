import React from 'react'
import {Breadcrumb} from 'designx';
export default function NodeCreate() {
  return <div className="m-sm-30">
    <Breadcrumb
      routeSegments={[
        { name: "节点管理", path: "/node/list" },
        { name: "添加节点" }
      ]}
    />
  </div>
}