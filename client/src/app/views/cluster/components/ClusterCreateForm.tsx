import React, { Component } from "react";
import {
  Button,
  Icon,
  Grid,
  IconButton,
  Typography,
  TextField
} from "@material-ui/core";

export default () => {
  const handleSubmit = () => {

  }
  return (
    <div>
      <ValidatorForm
        ref="form"
        onSubmit={handleSubmit}
        onError={errors => null}
      >
        <Grid container spacing={6}>
          <Grid item lg={6} md={6} sm={12} xs={12}>
            <TextField
              className="mb-4 w-full"
              label="集群名称"
              type="text"
              name="username"
              value={username}
              validators={[
                "required",
                "minStringLength: 4",
                "maxStringLength: 9"
              ]}
              variant="outlined"
              errorMessages={["this field is required"]}
            />
            <TextField
              className="mb-4 w-full"
              label="描述"
              onChange={this.handleChange}
              type="text"
              name="desc"
              value={firstName}
              validators={["required"]}
              errorMessages={["this field is required"]}
              variant="outlined"
            />
            <div className="mb-4 w-full">
              <Typography>配置节点</Typography>
              <Grid container>
                <Grid item lg={12} md={12} sm={12} xs={12} className="mb-1">
                    <Typography>选择Master节点</Typography>
                    <IconButton><Icon>add_circle</Icon></IconButton>
                </Grid>
                <Grid item lg={12} md={12} sm={12} xs={12} className="mb-1">
                    <Typography>选择Worker节点</Typography>
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
      </ValidatorForm>
    </div>
  )
}