// src/components/LogModal.js
import React, { useState, useEffect } from 'react';
import { createEntry, updateEntry as updateEntryAPI } from '../services/entryService';
// For tags, a more complex input might be needed later (e.g., react-select or react-tag-input)
// For now, a simple comma-separated string for tag names.

const LogModal = ({ isOpen, onClose, onSave, entryToEdit }) => {
  const [title, setTitle] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]); // Default to today
  const [note, setNote] = useState('');
  const [energy, setEnergy] = useState(50); // Default 0-100
  const [mood, setMood] = useState(50);   // Default 0-100
  const [tagsString, setTagsString] = useState(''); // Comma-separated tag names
  const [error, setError] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (entryToEdit) {
      setTitle(entryToEdit.title || '');
      setDate(entryToEdit.date || new Date().toISOString().split('T')[0]);
      setNote(entryToEdit.note || '');
      setEnergy(entryToEdit.energy ?? 50);
      setMood(entryToEdit.mood ?? 50);
      setTagsString(entryToEdit.tags ? entryToEdit.tags.map(t => t.name).join(', ') : '');
    } else {
      // Reset form for new entry
      setTitle('');
      setDate(new Date().toISOString().split('T')[0]);
      setNote('');
      setEnergy(50);
      setMood(50);
      setTagsString('');
    }
    setError(''); // Clear error when modal opens or entry changes
  }, [entryToEdit, isOpen]); // Re-populate form if entryToEdit changes or modal opens

  if (!isOpen) {
    return null;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setIsSaving(true);

    if (!title.trim()) {
        setError('Title is required.');
        setIsSaving(false);
        return;
    }

    const entryData = {
      title,
      date,
      note,
      energy: parseInt(energy, 10),
      mood: parseInt(mood, 10),
      tagNames: tagsString.split(',').map(tag => tag.trim()).filter(tag => tag !== ''),
    };

    try {
      if (entryToEdit) {
        await updateEntryAPI(entryToEdit.id, entryData);
      } else {
        await createEntry(entryData);
      }
      onSave(); // This prop will re-fetch entries in Dashboard/CaptainLogList
      handleClose();
    } catch (err) {
      console.error('Failed to save entry:', err);
      setError(err.message || 'Failed to save entry. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleClose = () => {
    // Reset form fields before closing if not editing, or keep for quick re-open
    // For simplicity, we reset via useEffect when isOpen changes or entryToEdit changes
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full flex justify-center items-center z-50">
      <div className="bg-white p-6 rounded-lg shadow-xl w-full max-w-lg mx-4">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-semibold">{entryToEdit ? 'Edit' : 'New'} Captain's Log</h2>
          <button onClick={handleClose} className="text-gray-600 hover:text-gray-800 text-2xl">×</button>
        </div>
        {error && <p className="text-red-500 text-sm mb-3">{error}</p>}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-700">Title*</label>
            <input
              type="text"
              id="title"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              required
            />
          </div>
          <div>
            <label htmlFor="date" className="block text-sm font-medium text-gray-700">Date</label>
            <input
              type="date"
              id="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="mood" className="block text-sm font-medium text-gray-700">Mood (0-100)</label>
              <input
                type="number"
                id="mood"
                min="0" max="100" step="1"
                value={mood}
                onChange={(e) => setMood(parseInt(e.target.value, 10))}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              />
            </div>
            <div>
              <label htmlFor="energy" className="block text-sm font-medium text-gray-700">Energy (0-100)</label>
              <input
                type="number"
                id="energy"
                min="0" max="100" step="1"
                value={energy}
                onChange={(e) => setEnergy(parseInt(e.target.value, 10))}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              />
            </div>
          </div>
          <div>
            <label htmlFor="tags" className="block text-sm font-medium text-gray-700">Tags (comma-separated)</label>
            <input
              type="text"
              id="tags"
              value={tagsString}
              onChange={(e) => setTagsString(e.target.value)}
              placeholder="e.g., productive, focused, learning"
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            />
          </div>
          <div>
            <label htmlFor="note" className="block text-sm font-medium text-gray-700">Note</label>
            <textarea
              id="note"
              rows="4"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
            ></textarea>
          </div>
          <div className="flex justify-end space-x-3">
            <button 
              type="button" 
              onClick={handleClose}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-md border border-gray-300"
            >
              Cancel
            </button>
            <button 
              type="submit"
              disabled={isSaving}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
            >
              {isSaving ? (entryToEdit ? 'Saving...' : 'Adding...') : (entryToEdit ? 'Save Changes' : 'Add Log')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LogModal;