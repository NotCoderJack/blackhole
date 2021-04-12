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
export default () => {
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
              name="host" 
              defaultValue=""
              as={<TextField
                className="mb-4 w-full"
                label="节点Host"
                size="small"
                type="text"
                name="username"
                variant="outlined"
              />}
              control={control}
              rules={{required: true}}
            />
            <Controller 
              name="name" 
              defaultValue=""
              as={<TextField
                className="mb-4 w-full"
                size="small"
                label="节点名称"
                type="text"
                name="username"
                variant="outlined"
                helperText={errors.name ? errors.name.message : null}
              />}
              control={control}
            />
            <Controller
              name="desc"
              defaultValue=""
              as={<TextField
                className="mb-4 w-full"
                label="描述"
                size="small"
                type="text"
                name="desc"
                variant="outlined"
              />}
              control={control}
            />  
          </Grid>
        </Grid>
        <Button color="primary" variant="contained" type="submit">
          <Icon>send</Icon>
          <span className="pl-2 capitalize">添加节点</span>
        </Button>
      </form>
    </div>
  )
}