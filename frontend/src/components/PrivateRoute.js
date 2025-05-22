import React from "react";
import { Navigate } from "react-router-dom";

const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem("token"); // adjust this if you store elsewhere

  if (!token) {
    return <Navigate to="/login" />;
  }

  return children;
};

export default PrivateRoute;
