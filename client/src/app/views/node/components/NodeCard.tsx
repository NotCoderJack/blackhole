import React from "react";
import { Grid, Card, Icon, IconButton, Tooltip } from "@material-ui/core";
import { withStyles } from "@material-ui/styles";
import { NodeItem } from "app/types"
import history from "history.js";
const styles = (theme: any) => ({
  icon: {
    fontSize: "44px",
    opacity: 0.6,
    color: theme.palette.primary.main
  }
});

interface NodeCardItem extends NodeItem {}

interface  NodeCardProps {
  display?: "card" | "list"
  list: NodeCardItem[]
}

const NodeCard = ({ list }: NodeCardProps) => {
  const handleGoToDetail = React.useCallback((id: number) => {
    history.push(`/node/list/${id}`)
  }, [])
  return (
    <Grid container spacing={3} className="mb-3 mt-3">
      {list.map(item => {
        return (<Grid item xs={12} md={6} key={item.id}>
          <Card className="play-card p-sm-24 bg-paper" elevation={6}>
            <div className="flex items-center">
              <Icon>cloud_circle</Icon>
              <div className="ml-3">
                <h6 className="m-0 mt-1 text-primary font-medium">{item.host}</h6>
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

export default withStyles(styles, { withTheme: true })(NodeCard);