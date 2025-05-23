// src/pages/Dashboard.js
import React, { useState, useCallback } from 'react'; // Added useState, useCallback
import TodayStats from "../components/TodayStats";
import TrendChartStack from "../components/TrendChartStack";
import CaptainLogList from "../components/CaptainLogList";
import LogModal from "../components/LogModal";

const Dashboard = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [entryToEdit, setEntryToEdit] = useState(null); // null for new, entry object for edit
  const [refreshLogListKey, setRefreshLogListKey] = useState(0); // To trigger re-fetch in CaptainLogList

  const handleOpenModalForNew = () => {
    setEntryToEdit(null); // Ensure it's for a new entry
    setIsModalOpen(true);
  };

  const handleOpenModalForEdit = (entry) => {
    setEntryToEdit(entry);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEntryToEdit(null); // Clear edit state
  };

  // This function will be called by LogModal after a successful save
  // It will then trigger CaptainLogList to re-fetch its data
  const handleSaveEntry = useCallback(() => {
    handleCloseModal();
    // Increment key to force CaptainLogList to re-fetch.
    // A more robust solution might involve a shared state/context or passing a refresh function.
    setRefreshLogListKey(prevKey => prevKey + 1); 
  }, []);


  return (
    <div className="min-h-screen p-6 bg-gray-100 text-gray-800">
      <h1 className="text-3xl font-bold mb-6">Captain’s Log Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="col-span-1 space-y-6">
          <TodayStats />
          <CaptainLogList 
            key={refreshLogListKey} // Add key here
            onOpenModal={handleOpenModalForNew} 
            onSelectEntryForEdit={handleOpenModalForEdit} 
          />
          {/* LogModal is now conditionally rendered here */}
        </div>
        <div className="col-span-2">
          <TrendChartStack />
        </div>
      </div>

      {/* Render LogModal conditionally based on isModalOpen */}
      <LogModal 
        isOpen={isModalOpen} 
        onClose={handleCloseModal} 
        onSave={handleSaveEntry}
        entryToEdit={entryToEdit} 
      />
    </div>
  );
};

export default Dashboard;