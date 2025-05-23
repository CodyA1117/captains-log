// src/components/LogEntryItem.js
import React from 'react';

const LogEntryItem = ({ entry, onSelectEntry, onDeleteEntry }) => {
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return new Date(dateString + 'T00:00:00').toLocaleDateString(undefined, options); // Add T00:00:00 to avoid timezone issues with just date
  };

  // Simple display for tags
  const displayTags = entry.tags && entry.tags.length > 0
    ? entry.tags.map(tag => `#${tag.name}`).join(' ')
    : 'No tags';

  return (
    <div className="bg-gray-100 p-3 rounded-md hover:bg-gray-200 mb-2">
      <div className="flex justify-between items-center">
        <button onClick={() => onSelectEntry(entry)} className="text-left flex-grow">
          <span className="font-semibold">{formatDate(entry.date)} - "{entry.title}"</span>
          <div className="text-xs text-gray-600">
            Mood: {entry.mood ?? 'N/A'} | Energy: {entry.energy ?? 'N/A'}
          </div>
          <div className="text-xs text-blue-500 mt-1">{displayTags}</div>
        </button>
        <button
          onClick={() => onDeleteEntry(entry.id)}
          className="ml-2 text-red-500 hover:text-red-700 font-bold"
          aria-label={`Delete entry titled ${entry.title}`}
        >
          × {/* A simple 'x' for delete */}
        </button>
      </div>
    </div>
  );
};

export default LogEntryItem;