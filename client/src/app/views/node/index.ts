import React from 'react'
export default [
  {
    path: "/node/list",
    component: React.lazy(() => import("./NodeList"))
  },
  {
    path: "/node/create",
    component: React.lazy(() => import("./NodeCreate"))
  },
]