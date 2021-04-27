import React from "react";
import { Redirect } from "react-router-dom";

import sessionRoutes from "./views/sessions/SessionRoutes";
import clusterRoutes from './views/cluster'
import nodeRoutes from './views/node'

const redirectRoute = [
  {
    path: "/",
    exact: true,
    component: () => <Redirect to="/cluster/list" />
  }
];

const errorRoute = [
  {
    component: () => <Redirect to="/session/404" />
  }
];

const routes = [
  ...sessionRoutes,
  ...clusterRoutes,
  ...nodeRoutes,
  ...redirectRoute,
  ...errorRoute,
];

export default routes;