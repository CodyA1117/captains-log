// src/components/ManualNutritionLogger.js (New or Renamed from NutritionixLogger.js)
import React, { useState } from 'react';
import * as nutritionLogService from '../services/nutritionixService'; // Or your renamed service file

const ManualNutritionLogger = ({ onDataLogged }) => {
  const [calories, setCalories] = useState('');
  const [protein, setProtein] = useState('');
  const [carbs, setCarbs] = useState('');
  const [fat, setFat] = useState('');
  const [waterAmount, setWaterAmount] = useState('');
  const [weightAmount, setWeightAmount] = useState('');
  const [logDate, setLogDate] = useState(new Date().toISOString().split('T')[0]);
  const [description, setDescription] = useState('');

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const resetForm = () => {
    setCalories(''); setProtein(''); setCarbs(''); setFat('');
    setWaterAmount(''); setWeightAmount(''); setDescription('');
    // setLogDate(new Date().toISOString().split('T')[0]); // Optionally reset date
    setMessage(''); setError('');
  };

  const handleSubmitLog = async (e) => {
    e.preventDefault();
    setMessage(''); setError('');

    // Basic validation - make more robust as needed
    if (!calories && !protein && !carbs && !fat && !waterAmount && !weightAmount) {
        setError('Please enter at least one value to log.');
        return;
    }

    const summaryData = {
      description,
      calories: calories ? parseFloat(calories) : null,
      protein: protein ? parseFloat(protein) : null,
      carbs: carbs ? parseFloat(carbs) : null,
      fat: fat ? parseFloat(fat) : null,
      waterIntakeOz: waterAmount ? parseFloat(waterAmount) : null,
      bodyWeight: weightAmount ? parseFloat(weightAmount) : null,
      loggedAt: logDate ? new Date(logDate).toISOString() : new Date().toISOString(), // Send as ISO string with time
    };

    try {
      await nutritionLogService.logDailyNutritionSummary(summaryData);
      setMessage('Nutrition data logged successfully for ' + logDate + '!');
      onDataLogged(); // Callback to refresh data in parent (TodayStats)
      resetForm();
    } catch (err) {
      setError(err.message || 'Failed to log nutrition data.');
    }
  };

  return (
    <div className="bg-white p-4 shadow rounded-lg mt-6">
      <h3 className="text-lg font-semibold mb-3">Log Daily Nutrition & Biometrics</h3>

      {message && <p className="text-green-600 text-sm mb-2">{message}</p>}
      {error && <p className="text-red-600 text-sm mb-2">{error}</p>}

      <form onSubmit={handleSubmitLog} className="space-y-3">
        <div>
          <label htmlFor="logDate" className="block text-sm font-medium text-gray-700">Date for Log</label>
          <input
            type="date" id="logDate" value={logDate} onChange={(e) => setLogDate(e.target.value)}
            className="mt-1 block w-full p-2 border rounded-md" required
          />
        </div>
        <div>
          <label htmlFor="description" className="block text-sm font-medium text-gray-700">Description (Optional)</label>
          <input
            type="text" id="description" value={description} onChange={(e) => setDescription(e.target.value)}
            placeholder="e.g., Overall day's intake"
            className="mt-1 block w-full p-2 border rounded-md"
          />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div><label className="text-xs">Calories (kcal)</label><input type="number" step="any" value={calories} onChange={e => setCalories(e.target.value)} className="w-full p-1 border rounded-md text-sm"/></div>
          <div><label className="text-xs">Protein (g)</label><input type="number" step="any" value={protein} onChange={e => setProtein(e.target.value)} className="w-full p-1 border rounded-md text-sm"/></div>
          <div><label className="text-xs">Carbs (g)</label><input type="number" step="any" value={carbs} onChange={e => setCarbs(e.target.value)} className="w-full p-1 border rounded-md text-sm"/></div>
          <div><label className="text-xs">Fat (g)</label><input type="number" step="any" value={fat} onChange={e => setFat(e.target.value)} className="w-full p-1 border rounded-md text-sm"/></div>
          <div><label className="text-xs">Water (oz)</label><input type="number" step="any" value={waterAmount} onChange={e => setWaterAmount(e.target.value)} className="w-full p-1 border rounded-md text-sm"/></div>
          <div><label className="text-xs">Weight (lbs/kg)</label><input type="number" step="any" value={weightAmount} onChange={e => setWeightAmount(e.target.value)} className="w-full p-1 border rounded-md text-sm"/></div>
        </div>
        <button type="submit" className="w-full bg-indigo-600 text-white p-2 rounded hover:bg-indigo-700">
          Log Daily Summary
        </button>
      </form>
    </div>
  );
};

export default ManualNutritionLogger;