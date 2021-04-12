import React from 'react'
import {Breadcrumb} from 'designx';
import { Grid, Card, Typography, Box, FormControl, InputLabel, OutlinedInput, InputAdornment, Button } from "@material-ui/core";
import { useParams } from "react-router-dom"
import { NodeItem } from "app/types"
import nodeService from "../../services/nodeService";
import { Terminal, Upload } from "designx"

interface ParamsType {
  host: string
}

export default function NodeList() {
  const [nodeInfo, setNodeInfo] = React.useState<NodeItem | null>(null);
  const host = useParams<ParamsType>().host;
  
  React.useEffect(() => {
    nodeService.getNodeByHost(host).then(data => {
      setNodeInfo(data)
    });
  }, [])

  const terminalService = (command: string) => {
    return nodeService.executeSSH(host, command)
  }


  return <div className="m-sm-30">
    <Breadcrumb
      routeSegments={[
        { name: "节点管理", path: "/node/list" },
        { name: "节点" + host }
      ]}
    />
    <Card className="px-4 py-3 mt-2">
      <Typography gutterBottom variant="h6" component="h5">
        节点基本信息
      </Typography>
      <Box>
        {nodeInfo?.name}
      </Box>
    </Card>
    <Card className="px-4 py-3 mt-2">
      <Typography gutterBottom variant="h6" component="h5">
        SSH功能
      </Typography>
      <Box>
        <Terminal label="SSH" service={terminalService}/>
      </Box>
    </Card>
    <Card className="px-4 py-3 mt-2">
      <Typography gutterBottom variant="h6" component="h5">
        SCP功能
        <Upload>
          <Button color="primary" variant="contained">上传</Button>
        </Upload>
      </Typography>
    </Card>
  </div>
}