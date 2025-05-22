import React, { useState, useEffect } from "react";
import axios from "axios";

const TodayStats = () => {
  const [ouraData, setOuraData] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const [syncMessage, setSyncMessage] = useState("");
  const token = localStorage.getItem("token");

  const fetchTodayData = async () => {
    try {
      const res = await axios.get("/api/oura/today", {
        headers: { Authorization: `Bearer ${token}` },
      });

      console.log("🟢 Backend responded:", res);

      if (res.data) {
        setOuraData({
          readiness: res.data.readinessScore,
          sleepScore: res.data.sleepScore,
          activityScore: res.data.activityScore,
          heartRate: res.data.heartRate,
        });
        setIsConnected(true);
      } else {
        console.log("⚠️ No data in response (res.data is null or undefined)");
      }
    } catch (err) {
      console.error("🔴 Failed to fetch today's Oura data:", err);
      setIsConnected(false);
    }
  };


  useEffect(() => {
    fetchTodayData();
  }, []);

  const connectOura = () => {
    window.location.href = "http://localhost:8080/api/oura/start";

  };

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

      await fetchTodayData();
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
              <p><strong>Oura Readiness:</strong> {ouraData.readiness ?? "N/A"}</p>
              <p><strong>Oura Sleep:</strong> {ouraData.sleepScore ?? "N/A"}</p>
              <p><strong>Oura Activity:</strong> {ouraData.activityScore ?? "N/A"}</p>
              <p><strong>Heart Rate:</strong> {ouraData.heartRate ?? "N/A"}</p>
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
