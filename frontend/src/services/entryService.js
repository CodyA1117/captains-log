// src/services/entryService.js
import axios from 'axios';

const API_URL = '/api/entries'; // Matches your EntryController @RequestMapping

const getToken = () => localStorage.getItem('token');

const getAuthHeaders = () => ({
  Authorization: `Bearer ${getToken()}`,
  'Content-Type': 'application/json',
});

export const getUserEntries = async () => {
  if (!getToken()) throw new Error('No token found');
  const response = await axios.get(API_URL, { headers: getAuthHeaders() });
  return response.data; // Expects a list of EntryResponseDTO
};

export const getEntryById = async (id) => {
  if (!getToken()) throw new Error('No token found');
  const response = await axios.get(`${API_URL}/${id}`, { headers: getAuthHeaders() });
  return response.data;
};

export const createEntry = async (entryData) => {
  // entryData should be an EntryRequestDTO: { title, date, note, energy, mood, tagNames }
  if (!getToken()) throw new Error('No token found');
  const response = await axios.post(API_URL, entryData, { headers: getAuthHeaders() });
  return response.data;
};

export const updateEntry = async (id, entryData) => {
  // entryData should be an EntryRequestDTO
  if (!getToken()) throw new Error('No token found');
  const response = await axios.put(`${API_URL}/${id}`, entryData, { headers: getAuthHeaders() });
  return response.data;
};

export const deleteEntry = async (id) => {
  if (!getToken()) throw new Error('No token found');
  await axios.delete(`${API_URL}/${id}`, { headers: getAuthHeaders() });
  // Delete usually returns 204 No Content, so no response.data to return directly
};

// You might also want a service for tags if you plan to fetch them separately for a tag cloud/selector
// export const getUserTags = async () => { ... }