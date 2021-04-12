import React from 'react'

export default [
  {
    path: "/cluster/list",
    exact: true,
    component: React.lazy(() => import("./ClusterList")),
  },
  {
    path: "/cluster/list/:id",
    component: React.lazy(() => import("./ClusterDetail"))
  }
  // {
  //   path: "/cluster/create",
  //   component: React.lazy(() => import("./ClusterCreate"))
  // },
]