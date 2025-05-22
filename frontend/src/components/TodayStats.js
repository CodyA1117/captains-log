import React, { useEffect, useState } from "react";
import axios from "axios";

const TodayStats = () => {
  const [ouraData, setOuraData] = useState(null);
  const [nutritionData, setNutritionData] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId"); // Adjust if stored differently

    const fetchOura = async () => {
      try {
        const response = await axios.get("/api/oura/today", {
          headers: { Authorization: `Bearer ${token}` }
        });
        setOuraData(response.data);
      } catch (err) {
        console.error("Failed to fetch Oura data", err);
      }
    };

    const fetchNutrition = async () => {
      try {
        const response = await axios.get(`/api/nutrition/sync?userId=${userId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setNutritionData(response.data);
      } catch (err) {
        console.error("Failed to fetch nutrition data", err);
      }
    };

    fetchOura();
    fetchNutrition();
  }, []);

  return (
    <div className="bg-white p-4 shadow rounded-lg">
      <h2 className="text-xl font-bold mb-4">Today’s Metrics</h2>

      {ouraData ? (
        <div className="mb-4">
          <p><strong>Oura Readiness:</strong> {ouraData.readiness}</p>
          <p><strong>Oura Sleep:</strong> {ouraData.sleepScore}</p>
          <p><strong>Oura Activity:</strong> {ouraData.activityScore}</p>
        </div>
      ) : (
        <p className="text-sm text-gray-500">Loading Oura data...</p>
      )}

      {nutritionData ? (
        <div>
          <p><strong>Calories:</strong> {nutritionData.calories}</p>
          <p><strong>Protein:</strong> {nutritionData.protein}g</p>
          <p><strong>Carbs:</strong> {nutritionData.carbs}g</p>
          <p><strong>Fat:</strong> {nutritionData.fat}g</p>
          <p><strong>Water:</strong> {nutritionData.waterIntakeOz} oz</p>
          <p><strong>Weight:</strong> {nutritionData.bodyWeight} lbs</p>
        </div>
      ) : (
        <p className="text-sm text-gray-500">Loading Nutritionix data...</p>
      )}
    </div>
  );
};

export default TodayStats;
