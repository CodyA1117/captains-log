// src/components/TodayStats.js
import React, { useState, useEffect, useCallback } from "react";
import axios from "axios"; // For Oura direct calls
import * as nutritionLogService from '../services/nutritionixService'; // Or your renamed service file

const TodayStats = () => {
  const [ouraData, setOuraData] = useState(null);
  const [nutritionixData, setNutritionixData] = useState(null); // From your DB

  const [isOuraConnected, setIsOuraConnected] = useState(false);
  // isNutritionixConnected state is removed as connection is implicit if user is logged in

  const [isLoadingOura, setIsLoadingOura] = useState(true);
  const [isLoadingNutritionix, setIsLoadingNutritionix] = useState(true);

  const [syncMessageOura, setSyncMessageOura] = useState("");
  const [syncMessageNutritionix, setSyncMessageNutritionix] = useState("");

  const appUserToken = localStorage.getItem("token");

  const fetchTodayOuraData = useCallback(async () => {
     // ... (Oura fetching logic - unchanged from last working version)
     if (!appUserToken) { setIsLoadingOura(false); setIsOuraConnected(false); setOuraData(null); return; }
     setIsLoadingOura(true);
     try {
       const res = await axios.get("/api/oura/today", { headers: { Authorization: `Bearer ${appUserToken}` } });
       if (res.status === 200 && res.data) {
         setOuraData({
             readiness: res.data.readinessScore || null,
             sleepScore: res.data.sleepScore || null,
             activityScore: res.data.activityScore || null,
             heartRate: res.data.heartRate || null,
             stress: res.data.stressScore || null,
         });
         setIsOuraConnected(true);
       } else if (res.status === 204) {
         setOuraData(null);
         setIsOuraConnected(true);
       } else {
         setOuraData(null);
         setIsOuraConnected(false);
       }
     } catch (err) { setOuraData(null); setIsOuraConnected(false); console.error("🔴 TodayStats: Failed to fetch Oura data:", err.response || err); }
     finally { setIsLoadingOura(false); }
   }, [appUserToken]);

  const fetchTodayNutritionixData = useCallback(async () => {
    if (!appUserToken) {
      setNutritionixData(null);
      setIsLoadingNutritionix(false);
      return;
    }
    setIsLoadingNutritionix(true);
    try {
      const data = await nutritionLogService.getTodayNutritionLog();
      console.log("🟢 TodayStats: Backend responded for /api/nutrition/today (local DB):", data);
      setNutritionixData(data); // Will be null if 204 or error
    } catch (err) {
      console.error("🔴 TodayStats: Failed to fetch Nutritionix data from local DB:", err.response || err);
      setNutritionixData(null);
    } finally {
      setIsLoadingNutritionix(false);
    }
  }, [appUserToken]);

  useEffect(() => {
    if (appUserToken) {
      fetchTodayOuraData();
      fetchTodayNutritionixData();
    } else {
      setIsLoadingOura(false); setIsOuraConnected(false);
      setIsLoadingNutritionix(false);
    }
  }, [appUserToken, fetchTodayOuraData, fetchTodayNutritionixData]);

  const connectOura = () => window.location.href = "http://localhost:8080/api/oura/start";
  const syncOuraData = async () => { /* ... existing Oura sync logic (unchanged) ... */
     if (!appUserToken) { setSyncMessageOura("Please log in."); return; }
     setSyncMessageOura("Syncing Oura data...");
     try {
         await axios.get("/api/oura/sync-today", { headers: { Authorization: `Bearer ${appUserToken}` } });
         await axios.get("/api/oura/sync-sleep", { headers: { Authorization: `Bearer ${appUserToken}` } });
         await axios.get("/api/oura/sync-activity", { headers: { Authorization: `Bearer ${appUserToken}` } });
         await axios.get("/api/oura/sync-heart-rate", { headers: { Authorization: `Bearer ${appUserToken}` } });
         await axios.get("/api/oura/sync-stress", { headers: { Authorization: `Bearer ${appUserToken}` } });
         await fetchTodayOuraData();
         setSyncMessageOura("Oura data sync complete!");
     } catch (err) {
         console.error("Failed to sync Oura data:", err.response || err);
         const errorMsg = err.response?.data?.message || err.response?.data || err.message || "Unknown error";
         setSyncMessageOura(`Oura sync failed: ${errorMsg}.`);
     }
 };

  const refreshLocalNutritionData = async () => {
    if (!appUserToken) { setSyncMessageNutritionix("Please log in."); return; }
    setSyncMessageNutritionix("Refreshing nutrition data...");
    await fetchTodayNutritionixData();
    setSyncMessageNutritionix("Nutrition data refreshed!");
  };

  if (isLoadingOura || isLoadingNutritionix) {
    return (
      <div className="bg-white p-4 shadow rounded-lg">
        <h2 className="text-xl font-bold mb-4">Today’s Metrics</h2>
        <p>Loading stats...</p>
      </div>
    );
  }

  return (
    <div className="bg-white p-4 shadow rounded-lg">
      <h2 className="text-xl font-bold mb-4">Today’s Metrics</h2>
      {/* Oura Section */}
      <div className="mb-6 pb-4 border-b">
        {/* ... Oura JSX (unchanged) ... */}
         <h3 className="text-lg font-semibold mb-2">Oura Ring</h3>
         {!appUserToken || !isOuraConnected ? (
         <button className="bg-purple-600 text-white px-4 py-2 rounded" onClick={connectOura} disabled={!appUserToken}>
             {appUserToken ? "+ Connect Oura" : "Log in to Connect Oura"}
         </button>
         ) : (
         <>
             <button className="bg-green-600 text-white px-4 py-2 rounded mb-2" onClick={syncOuraData} disabled={syncMessageOura.startsWith("Syncing")}>
             {syncMessageOura.startsWith("Syncing") ? syncMessageOura : "Sync Oura Data"}
             </button>
             {syncMessageOura && !syncMessageOura.startsWith("Syncing") && <p className="text-sm text-gray-700 mb-1">{syncMessageOura}</p>}
             {ouraData ? (
             <div className="text-sm">
                 <p><strong>Readiness:</strong> {ouraData.readiness ?? "N/A"}</p>
                 <p><strong>Sleep:</strong> {ouraData.sleepScore ?? "N/A"}</p>
                 <p><strong>Activity:</strong> {ouraData.activityScore ?? "N/A"}</p>
                 <p><strong>Heart Rate:</strong> {ouraData.heartRate ?? "N/A"}</p>
                 <p><strong>Stress:</strong> {ouraData.stress ?? "N/A"}</p>
             </div>
             ) : (
             <p className="text-sm text-gray-500">No Oura data for today. Try syncing.</p>
             )}
         </>
         )}
      </div>

      {/* Nutritionix Section - Simplified */}
      {appUserToken && (
        <div>
          <h3 className="text-lg font-semibold mb-2">Nutrition Log</h3>
          <button
             className="bg-teal-500 text-white px-4 py-2 rounded mb-2"
             onClick={refreshLocalNutritionData}
             disabled={syncMessageNutritionix.startsWith("Refreshing")}
           >
            {syncMessageNutritionix.startsWith("Refreshing") ? syncMessageNutritionix : "Refresh Nutrition Data"}
          </button>
          {syncMessageNutritionix && !syncMessageNutritionix.startsWith("Refreshing") &&
             <p className="text-sm text-gray-700 mb-1">{syncMessageNutritionix}</p>
          }
          {nutritionixData ? (
            <div className="text-sm">
              <p><strong>Calories:</strong> {nutritionixData.calories?.toFixed(0) ?? "N/A"} kcal</p>
              <p><strong>Protein:</strong> {nutritionixData.protein?.toFixed(1) ?? "N/A"} g</p>
              <p><strong>Carbs:</strong> {nutritionixData.carbs?.toFixed(1) ?? "N/A"} g</p>
              <p><strong>Fat:</strong> {nutritionixData.fat?.toFixed(1) ?? "N/A"} g</p>
              <p><strong>Weight:</strong> {nutritionixData.bodyWeight ?? "N/A"} {nutritionixData.bodyWeight ? "lbs" : ""}</p> {/* Assuming lbs */}
              <p><strong>Water:</strong> {nutritionixData.waterIntakeOz ?? "N/A"} oz</p>
            </div>
          ) : (
            <p className="text-sm text-gray-500">No nutrition data logged for today in Captain's Log.</p>
          )}
        </div>
      )}
    </div>
  );
};

export default TodayStats;