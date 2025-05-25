// src/services/nutritionixService.js (or rename to nutritionLogService.js)
import axios from 'axios';

const API_URL = '/api/nutrition';

const getToken = () => localStorage.getItem('token');

const getAuthHeaders = () => ({
  Authorization: `Bearer ${getToken()}`,
  'Content-Type': 'application/json',
});

// Logs a full day's summary manually
export const logDailyNutritionSummary = async (summaryData) => {
  // summaryData: { description?, calories, protein, carbs, fat, waterIntakeOz, bodyWeight, loggedAt? }
  if (!getToken()) throw new Error('No token found');
  const response = await axios.post(`${API_URL}/log-daily-summary`, summaryData, { headers: getAuthHeaders() });
  return response.data; // Returns updated NutritionLogDTO
};

export const getTodayNutritionLog = async () => {
  if (!getToken()) throw new Error('No token found');
  try {
    const response = await axios.get(`${API_URL}/today`, { headers: getAuthHeaders() });
    if (response.status === 204) {
        return null;
    }
    return response.data;
  } catch (error) {
    if (error.response && error.response.status === 204) {
        return null;
    }
    console.error("Error fetching today's nutrition log:", error.response || error);
    throw error;
  }
};

export const getNutritionLogHistory = async () => {
    if (!getToken()) throw new Error('No token found');
    const response = await axios.get(`${API_URL}/history`, { headers: getAuthHeaders() });
    return response.data;
};