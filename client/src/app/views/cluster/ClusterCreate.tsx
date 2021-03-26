import React from 'react'
import {Breadcrumb} from "designx";
import ClusterCreateForm from "./components/ClusterCreateForm";
import { Card } from "@material-ui/core";

export default function ClusterCreate() {
  return <div className="m-sm-30">
    <div className="mb-sm-30">
      <Breadcrumb 
        routeSegments={[{ name: "集群管理", path: "/cluster/list" },
          { name: "创建集群" }
        ]}
      />
    </div>
    <Card className="px-6 pt-2 pb-4"><ClusterCreateForm /></Card>
  </div>
}