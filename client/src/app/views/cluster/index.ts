import React from 'react'

export default [
  {
    path: "/cluster/list",
    component: React.lazy(() => import("./ClusterList"))
  },
  {
    path: "/cluster/create",
    component: React.lazy(() => import("./ClusterCreate"))
  },
]