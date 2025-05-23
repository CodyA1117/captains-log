// src/components/CaptainLogList.js
import React, { useState, useEffect } from 'react';
import { getUserEntries, deleteEntry as deleteEntryAPI } from '../services/entryService'; // Import the API functions
import LogEntryItem from './LogEntryItem'; // We'll create this next
// We'll pass functions to open modal from Dashboard down to here
// For now, let's assume onOpenModal is passed as a prop

const CaptainLogList = ({ onOpenModal, onSelectEntryForEdit }) => {
  const [entries, setEntries] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const fetchEntries = async () => {
    setIsLoading(true);
    setError('');
    try {
      const data = await getUserEntries();
      setEntries(data);
    } catch (err) {
      console.error("Failed to fetch entries:", err);
      setError(err.message || 'Failed to load log entries. Please try again.');
      setEntries([]); // Clear entries on error
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchEntries();
  }, []); // Fetch on component mount

  const handleDeleteEntry = async (entryId) => {
    if (window.confirm('Are you sure you want to delete this log entry?')) {
      try {
        await deleteEntryAPI(entryId);
        setEntries(prevEntries => prevEntries.filter(entry => entry.id !== entryId));
        // Optionally show a success message
      } catch (err) {
        console.error("Failed to delete entry:", err);
        setError(err.message || 'Failed to delete entry.');
      }
    }
  };

  // This function will be called by LogEntryItem to signal that an entry should be edited
  // It will then call the prop passed from Dashboard
  const handleSelectEntry = (entry) => {
    console.log("Selected entry for edit:", entry);
    onSelectEntryForEdit(entry); // Call the prop function
  };


  return (
    <div className="bg-white p-4 shadow rounded-lg">
      <div className="flex justify-between items-center mb-4"> {/* Increased mb */}
        <h2 className="text-xl font-bold">Captain’s Logs</h2>
        <button
          onClick={() => onOpenModal()} // Call prop to open modal for new entry
          className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-3 rounded-full text-lg"
          aria-label="Add new log entry"
        >
          +
        </button>
      </div>

      {isLoading && <p>Loading logs...</p>}
      {error && <p className="text-red-500">{error}</p>}

      {!isLoading && !error && entries.length === 0 && (
        <p className="text-gray-500">No log entries yet. Click '+' to add one!</p>
      )}

      <div className="space-y-2 max-h-96 overflow-y-auto"> {/* Added max-h and overflow */}
        {entries.map(entry => (
          <LogEntryItem
            key={entry.id}
            entry={entry}
            onSelectEntry={handleSelectEntry} // Pass down for editing
            onDeleteEntry={handleDeleteEntry} // Pass down for deleting
          />
        ))}
      </div>
    </div>
  );
};

export default CaptainLogList;