import React, { useState } from "react";
import {
  Button,
  Icon,
  Grid,
  IconButton,
  Typography,
  TextField
} from "@material-ui/core";
import { useForm, Controller } from "react-hook-form";
import { ClusterItem, NodeItem} from "app/types";
export default () => {
  const [masterNode, setMasterNode] = useState<ClusterItem | null>(null);
  const [workerNodes, setWorkerNodes] = useState<NodeItem[]>([]);
  const [clusterName, setClusterName] = useState<String>('');
  const [desc, setDesc] = useState<String>('');

  const { handleSubmit, control, errors } = useForm();
  const onSubmit = handleSubmit((data: any) => {
    console.log('submit', data);
  })
  return (
    <div>
      <form onSubmit={onSubmit}>
        <Grid container spacing={6}>
          <Grid item lg={6} md={6} sm={12} xs={12}>
            <Controller 
              name="name" 
              defaultValue=""
              as={<TextField
                className="mb-4 w-full"
                label="集群名称"
                type="text"
                name="username"
                variant="outlined"
                value={clusterName}
                helperText={errors.name ? errors.name.message : null}
              />}
              rules={{required: true}}
              control={control}
            />
            <Controller
              name="desc"
              defaultValue=""
              as={<TextField
                className="mb-4 w-full"
                label="描述"
                type="text"
                name="desc"
                variant="outlined"
                value={desc}
              />}
              control={control}
            />  
            <div className="mb-4 w-full">
              <Typography component="h4">配置节点</Typography>
              <Grid container>
                <Grid item lg={12} md={12} sm={12} xs={12} className="mb-1">
                    <Typography>1. 选择Master节点</Typography>
                    <IconButton><Icon>add_circle</Icon></IconButton>
                </Grid>
                <Grid item lg={12} md={12} sm={12} xs={12} className="mb-1">
                    <Typography>2. 选择Worker节点</Typography>
                    <IconButton><Icon>add_circle</Icon></IconButton>
                </Grid>
              </Grid>
            </div>
          </Grid>
        </Grid>
        <Button color="primary" variant="contained" type="submit">
          <Icon>send</Icon>
          <span className="pl-2 capitalize">创建集群</span>
        </Button>
      </form>
    </div>
  )
}