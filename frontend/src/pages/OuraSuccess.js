import React, { useEffect } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import axios from "axios";

const OuraSuccess = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const code = searchParams.get("code");
    const token = localStorage.getItem("token");

    if (code && token) {
      axios.post("/api/oura/save-token", { code }, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      })
      .then(() => {
        navigate("/dashboard");
      })
      .catch((err) => {
        console.error("Token save failed", err);
      });
    }
  }, [searchParams, navigate]);

  return <p>Connecting your Oura account...</p>;
};

export default OuraSuccess;
