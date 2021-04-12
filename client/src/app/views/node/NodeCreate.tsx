import React from 'react'
import {Breadcrumb} from 'designx';
import NodeCreateForm from "./components/NodeCreateForm"
import { Card } from "@material-ui/core";
export default function NodeCreate() {
  return <div className="m-sm-30">
      <Breadcrumb
        routeSegments={[
          { name: "节点管理", path: "/node/list" },
          { name: "添加节点" }
        ]}
      />
    <Card className="px-6 pt-4 pb-4 mt-4"><NodeCreateForm /></Card>
  </div>
}