import React, { useEffect, useRef } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import axios from "axios";

const OuraSuccess = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const hasAttemptedSave = useRef(false); // To ensure API call is made only once

  useEffect(() => {
    const code = searchParams.get("code");
    const appToken = localStorage.getItem("token"); // This is your app's JWT

    if (code && appToken && !hasAttemptedSave.current) {
      hasAttemptedSave.current = true; // Mark that we are attempting the save
      console.log("OuraSuccess: Found Oura code and appToken, attempting to save Oura token with backend...");

      axios.post("/api/oura/save-token", { code }, {
        headers: {
          Authorization: `Bearer ${appToken}`,
          "Content-Type": "application/json",
        },
      })
      .then((response) => {
        console.log("OuraSuccess: Backend processed Oura token successfully:", response.data);
        navigate("/dashboard");
      })
      .catch((err) => {
        console.error("OuraSuccess: Backend failed to process Oura token.", err.response ? err.response.data : err.message, err);
        // Navigate to dashboard, possibly with an error query parameter
        // This allows the user to land on the dashboard and potentially try a manual sync later
        // or see a general error message if you implement that on the dashboard.
        navigate("/dashboard?oura_connection_error=true");
      });
    } else if (!appToken) {
        console.error("OuraSuccess: Application JWT (appToken) not found in localStorage. Redirecting to login.");
        navigate("/login"); // Redirect to login if no app token
    } else if (!code) {
        console.error("OuraSuccess: Oura authorization code not found in URL parameters. Navigating to dashboard.");
        // If no code, something went wrong with the Oura redirect, but user is logged in.
        // Go to dashboard, maybe they can try connecting again.
        navigate("/dashboard?oura_redirect_issue=true");
    } else if (hasAttemptedSave.current) {
        console.log("OuraSuccess: Save attempt already made. Preventing duplicate call.");
        // Optional: if you want to redirect immediately if already attempted and still on this page
        // navigate("/dashboard");
    }
    // Dependencies:
    // searchParams: to get the 'code'. If this changes, we might need to re-evaluate.
    // navigate: standard practice to include if used.
    // The appToken is fetched from localStorage inside the effect. Since localStorage changes
    // don't trigger re-renders, hasAttemptedSave.current is the primary guard against
    // multiple POSTs if the component happens to re-render for other reasons.
  }, [searchParams, navigate]);

  return <p>Finalizing your Oura account connection. Please wait...</p>;
};

export default OuraSuccess;