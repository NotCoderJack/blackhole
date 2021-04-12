import React from 'react'
export default [
  {
    path: "/node/list",
    exact: true,
    component: React.lazy(() => import("./NodeList")),
  },
  {
    path: "/node/list/:id",
    component: React.lazy(() => import("./NodeDetail")),
  },
  {
    path: "/node/create",
    component: React.lazy(() => import("./NodeCreate"))
  },
]