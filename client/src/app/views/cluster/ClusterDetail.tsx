import React from 'react'
import {Breadcrumb} from 'designx';
import { Grid, Card, Typography, Box } from "@material-ui/core";
import { useParams } from "react-router-dom"
import { Terminal } from "designx";
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
    <Card className="px-4 py-3 mt-2">
      <Typography component="h3">集群基础信息</Typography>
    </Card>
    <Card className="px-4 py-3 mt-2">
      <Typography gutterBottom variant="h6" component="h5">
        kubectl
      </Typography>
      <Box>
        <Terminal label="kubectl" service={clusterService.executeKubectl}/>
      </Box>
    </Card>
  </div>
}