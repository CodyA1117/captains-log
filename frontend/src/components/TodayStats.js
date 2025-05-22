import React, { useState, useEffect } from "react";
import axios from "axios";

const TodayStats = () => {
  const [ouraData, setOuraData] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const [syncMessage, setSyncMessage] = useState("");
  const token = localStorage.getItem("token");

  // Check if the user has previously connected Oura
  const checkConnection = async () => {
    try {
      const res = await axios.get("/api/oura/sync-today", {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (res.status === 200) {
        setIsConnected(true);
      }
    } catch (err) {
      setIsConnected(false);
    }
  };

  useEffect(() => {
    checkConnection();
  }, []);

  // Redirect to Oura login to authorize the user
  const connectOura = () => {
    window.location.href = "/api/oura/start";
  };

  // Sync all available Oura data
  const syncOuraData = async () => {
    try {
      setSyncMessage("Syncing data...");

      await axios.get("/api/oura/sync-today", {
        headers: { Authorization: `Bearer ${token}` },
      });
      await axios.get("/api/oura/sync-sleep", {
        headers: { Authorization: `Bearer ${token}` },
      });
      await axios.get("/api/oura/sync-activity", {
        headers: { Authorization: `Bearer ${token}` },
      });
      await axios.get("/api/oura/sync-heart-rate", {
        headers: { Authorization: `Bearer ${token}` },
      });

      // For now, simulate updated values since no /oura/today endpoint exists yet
      setOuraData({
        readiness: "Synced",
        sleepScore: "Synced",
        activityScore: "Synced",
        heartRate: "Synced",
      });

      setSyncMessage("Oura data synced!");
    } catch (err) {
      console.error("Failed to sync Oura data:", err);
      setSyncMessage("Failed to sync Oura data.");
    }
  };

  return (
    <div className="bg-white p-4 shadow rounded-lg">
      <h2 className="text-xl font-bold mb-4">Today’s Metrics</h2>

      {!isConnected ? (
        <button
          className="bg-purple-600 text-white px-4 py-2 rounded"
          onClick={connectOura}
        >
          + Connect Oura
        </button>
      ) : (
        <>
          <button
            className="bg-green-600 text-white px-4 py-2 rounded mb-4"
            onClick={syncOuraData}
          >
            Sync Today's Oura Data
          </button>

          {syncMessage && (
            <p className="text-sm text-gray-700 mb-2">{syncMessage}</p>
          )}

          {ouraData ? (
            <div className="mb-4">
              <p><strong>Oura Readiness:</strong> {ouraData.readiness}</p>
              <p><strong>Oura Sleep:</strong> {ouraData.sleepScore}</p>
              <p><strong>Oura Activity:</strong> {ouraData.activityScore}</p>
              <p><strong>Heart Rate:</strong> {ouraData.heartRate}</p>
            </div>
          ) : (
            <p className="text-sm text-gray-500">
              No Oura data yet. Click sync above.
            </p>
          )}
        </>
      )}
    </div>
  );
};

export default TodayStats;
