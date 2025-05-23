import React, { useState, useEffect, useCallback } from "react";
import axios from "axios";

const TodayStats = () => {
  const [ouraData, setOuraData] = useState(null);
  const [isOuraConnected, setIsOuraConnected] = useState(false); // Start as false
  const [isLoading, setIsLoading] = useState(true);
  const [syncMessage, setSyncMessage] = useState("");
  const appUserToken = localStorage.getItem("token");

  const fetchTodayData = useCallback(async () => {
    if (!appUserToken) {
      console.log("TodayStats: No app user token found. User is not logged in.");
      setIsOuraConnected(false); // Definitely not connected
      setOuraData(null);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    try {
      const res = await axios.get("/api/oura/today", {
        headers: { Authorization: `Bearer ${appUserToken}` },
      });

      console.log("🟢 TodayStats: Backend responded for /api/oura/today:", res);

      if (res.status === 200 && res.data && Object.keys(res.data).length > 0) {
        setOuraData({
          readiness: res.data.readinessScore,
          sleepScore: res.data.sleepScore,
          activityScore: res.data.activityScore,
          heartRate: res.data.heartRate,
          stress: res.data.stressScore,
        });
        setIsOuraConnected(true); // Data fetched, so Oura connection is working
      } else if (res.status === 204) {
        console.log("ℹ️ TodayStats: /api/oura/today returned 204. No Oura data in DB for today.");
        setOuraData(null);
        // For a 204, we don't definitively know if an Oura token exists yet
        // just from this call. Let's assume not truly "connected" for UI purposes
        // until a successful sync or data fetch.
        // The user might have an app token but never clicked "+ Connect Oura".
        // We will set isOuraConnected to true *after* a successful /save-token or successful manual sync.
        // For now, if it's just a 204 on page load, let's keep isOuraConnected based on prior state or assume false if no data.
        // A better way would be a dedicated endpoint to check Oura connection status.
        // For now, let's only set isOuraConnected to true if we get actual data (status 200).
        // If it's 204, it means backend is fine, but we don't have Oura data. The button logic will handle it.
        // If isOuraConnected was ALREADY true (e.g. from a previous successful sync), it stays true.
        // If it was false, it stays false, prompting "Connect Oura".
        // This means the initial load for a new user will keep isOuraConnected as false.
      } else {
        console.log("⚠️ TodayStats: No data content or unexpected status for /api/oura/today. Status:", res.status);
        setOuraData(null);
        setIsOuraConnected(false); // Problem fetching, assume not connected
      }
    } catch (err) {
      console.error("🔴 TodayStats: Failed to fetch today's Oura data from /api/oura/today:", err.response || err);
      setOuraData(null);
      setIsOuraConnected(false); // Error fetching, assume not connected
    } finally {
      setIsLoading(false);
    }
  }, [appUserToken]);

  useEffect(() => {
    if (appUserToken) {
      fetchTodayData();
    } else {
      setIsLoading(false);
      setIsOuraConnected(false);
    }
  }, [appUserToken, fetchTodayData]);

  const connectOura = () => {
    window.location.href = "http://localhost:8080/api/oura/start";
  };

  const syncOuraData = async () => {
    if (!appUserToken) {
      setSyncMessage("Please log in to sync Oura data.");
      return;
    }
    if (!isOuraConnected) { // Check if we *think* Oura is connected
        // This message might be slightly off if isOuraConnected is true due to a 204 but no token actually exists.
        // The backend sync calls will fail gracefully if no token, as seen in logs.
        setSyncMessage("Attempting to sync. If this fails, please try connecting Oura first.");
    }

    try {
      setSyncMessage("Syncing Readiness...");
      // The backend /sync- routes will internally check for an Oura token.
      // If not present, they will log a warning and do nothing, which is what we saw.
      await axios.get("/api/oura/sync-today", { headers: { Authorization: `Bearer ${appUserToken}` } });
      setSyncMessage("Syncing Sleep...");
      await axios.get("/api/oura/sync-sleep", { headers: { Authorization: `Bearer ${appUserToken}` } });
      setSyncMessage("Syncing Activity...");
      await axios.get("/api/oura/sync-activity", { headers: { Authorization: `Bearer ${appUserToken}` } });
      setSyncMessage("Syncing Heart Rate...");
      await axios.get("/api/oura/sync-heart-rate", { headers: { Authorization: `Bearer ${appUserToken}` } });

      setSyncMessage("Fetching updated data...");
      await fetchTodayData(); // This will update ouraData and potentially isOuraConnected
      if (ouraData || (await axios.get("/api/oura/today", { headers: { Authorization: `Bearer ${appUserToken}` } })).status === 200) {
        setIsOuraConnected(true); // Confirm connection after sync
        setSyncMessage("Oura data sync complete!");
      } else {
        // If after sync, /api/oura/today still gives 204, it means sync didn't populate data
        // but the Oura token itself might be fine.
        setIsOuraConnected(true); // Assume token flow worked if syncs didn't hard error for no token
        setSyncMessage("Sync attempted. No new data found for today.");
      }
    } catch (err) {
      console.error("Failed to sync Oura data:", err.response || err);
      const errorMsg = err.response?.data?.message || err.response?.data || err.message || "Unknown error";
      setSyncMessage(`Sync failed: ${errorMsg}. Ensure Oura is connected.`);
      // If sync fails, it's possible the Oura connection is problematic.
      // However, OuraDataService handles "no token" gracefully.
      // Let's not flip isOuraConnected to false here unless error is 401/403.
       if (err.response && (err.response.status === 401 || err.response.status === 403)) {
        setIsOuraConnected(false);
      }
    }
  };

  if (isLoading) {
    return (
      <div className="bg-white p-4 shadow rounded-lg">
        <h2 className="text-xl font-bold mb-4">Today’s Metrics</h2>
        <p>Loading Oura stats...</p>
      </div>
    );
  }

  return (
    <div className="bg-white p-4 shadow rounded-lg">
      <h2 className="text-xl font-bold mb-4">Today’s Metrics</h2>

      {/* Show Connect button if not Oura connected, OR if user isn't even app-logged-in */}
      {!appUserToken || !isOuraConnected ? (
        <button
          className="bg-purple-600 text-white px-4 py-2 rounded"
          onClick={connectOura}
          disabled={!appUserToken} // Disable if not logged into the app
        >
          {appUserToken ? "+ Connect Oura" : "Log in to Connect Oura"}
        </button>
      ) : (
        <>
          <button
            className="bg-green-600 text-white px-4 py-2 rounded mb-4"
            onClick={syncOuraData}
            disabled={syncMessage.startsWith("Syncing")}
          >
            {syncMessage.startsWith("Syncing") ? syncMessage : "Sync Today's Oura Data"}
          </button>

          {syncMessage && !syncMessage.startsWith("Syncing") && (
            <p className="text-sm text-gray-700 mb-2">{syncMessage}</p>
          )}

          {ouraData ? (
            <div className="mb-4">
              <p><strong>Oura Readiness:</strong> {ouraData.readiness ?? "N/A"}</p>
              <p><strong>Oura Sleep:</strong> {ouraData.sleepScore ?? "N/A"}</p>
              <p><strong>Oura Activity:</strong> {ouraData.activityScore ?? "N/A"}</p>
              <p><strong>Heart Rate:</strong> {ouraData.heartRate ?? "N/A"}</p>
              <p><strong>Stress:</strong> {ouraData.stress ?? "N/A"}</p>
            </div>
          ) : (
            <p className="text-sm text-gray-500">
              {isOuraConnected ? "No Oura data for today yet. Try syncing, or ensure your Oura ring has synced with the Oura app." : "Please connect your Oura account to see your stats."}
            </p>
          )}
        </>
      )}
    </div>
  );
};

export default TodayStats;