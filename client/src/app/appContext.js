import React from "react";

const AppContext = React.createContext({});

const useAppContext = () => {
  const context = React.useContext(AppContext)
  return context
}

export default AppContext;
