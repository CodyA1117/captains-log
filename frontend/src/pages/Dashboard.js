// src/pages/Dashboard.js
import React, { useState, useCallback } from 'react';
import TodayStats from "../components/TodayStats";
import TrendChartStack from "../components/TrendChartStack";
import CaptainLogList from "../components/CaptainLogList";
import LogModal from "../components/LogModal";
import ManualNutritionLogger from '../components/ManualNutritionLogger'; // Updated import

const Dashboard = () => {
  // ... (existing state for LogModal: isLogModalOpen, entryToEdit, refreshLogListKey)
  const [isLogModalOpen, setIsLogModalOpen] = useState(false);
  const [entryToEdit, setEntryToEdit] = useState(null);
  const [refreshLogListKey, setRefreshLogListKey] = useState(0);

  const [refreshAllDataKey, setRefreshAllDataKey] = useState(0); // Single key to refresh TodayStats

  const handleOpenModalForNew = () => { setEntryToEdit(null); setIsLogModalOpen(true); };
  const handleOpenModalForEdit = (entry) => { setEntryToEdit(entry); setIsLogModalOpen(true); };
  const handleCloseModal = () => { setIsLogModalOpen(false); setEntryToEdit(null); };

  const handleSaveEntry = useCallback(() => {
    handleCloseModal();
    setRefreshLogListKey(prevKey => prevKey + 1);
  }, []);

  // This will be called by ManualNutritionLogger after it successfully saves data
  const handleNutritionDataLogged = useCallback(() => {
    setRefreshAllDataKey(prevKey => prevKey + 1); // Trigger re-render of TodayStats
  }, []);

  return (
    <div className="min-h-screen p-6 bg-gray-100 text-gray-800">
      <h1 className="text-3xl font-bold mb-6">Captain’s Log Dashboard</h1>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="col-span-1 space-y-6">
          <TodayStats key={refreshAllDataKey} /> {/* Use the new key here */}
          <CaptainLogList
            key={refreshLogListKey}
            onOpenModal={handleOpenModalForNew}
            onSelectEntryForEdit={handleOpenModalForEdit}
          />
          <LogModal
            isOpen={isLogModalOpen}
            onClose={handleCloseModal}
            onSave={handleSaveEntry}
            entryToEdit={entryToEdit}
          />
          <ManualNutritionLogger onDataLogged={handleNutritionDataLogged} /> {/* Use new logger */}
        </div>
        <div className="col-span-2">
          <TrendChartStack />
        </div>
      </div>
    </div>
  );
};

export default Dashboard;