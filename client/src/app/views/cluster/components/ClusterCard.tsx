import React from "react";
import { Grid, Card, Icon, IconButton, Tooltip, Typography } from "@material-ui/core";
import { withStyles } from "@material-ui/styles";
import { ClusterItem } from "app/types/cluster";
import history from "history.js"
const styles = (theme: any) => ({
  icon: {
    fontSize: "44px",
    opacity: 0.6,
    color: theme.palette.primary.main
  }
});

interface ClusterCardItem extends ClusterItem {}

interface  ClusterCardProps {
  list: ClusterCardItem[]
}

const ClusterCard = ({ list }: ClusterCardProps) => {
  const handleGoToDetail = React.useCallback((id: number) => {
    history.push(`/cluster/list/${id}`)
  }, [])
  return (
    <Grid container spacing={3} className="mb-3 mt-3">
      {list.map(item => {
        return (<Grid item xs={12} md={6} key={item.id}>
          <Card className="play-card p-sm-24 bg-paper" elevation={6}>
            <div className="flex items-center">
              <Icon>group</Icon>
              <div className="ml-3">
                <Typography component="div" className="m-0 mt-1 text-primary font-medium" onClick={() => handleGoToDetail(item.id)}>{item.desc}</Typography>
                <small className="text-muted">{item.name}</small>
              </div>
            </div>
            <Tooltip title="View Details" placement="top">
              <IconButton onClick={() => handleGoToDetail(item.id)}>
                <Icon>arrow_right_alt</Icon>
              </IconButton>
            </Tooltip>
          </Card>
        </Grid>)
      })}
    </Grid>
  );
};

export default withStyles(styles, { withTheme: true })(ClusterCard);