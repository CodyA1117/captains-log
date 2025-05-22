const CaptainLogList = () => {
  return (
    <div className="bg-white p-4 shadow rounded-lg">
      <div className="flex justify-between items-center mb-2">
        <h2 className="text-xl font-bold">Captain’s Logs</h2>
        <button className="text-blue-600 font-bold text-2xl">+</button>
      </div>

      {/* Sample log buttons */}
      <div className="space-y-2">
        <button className="w-full text-left bg-gray-100 p-3 rounded-md hover:bg-gray-200">
          <div className="flex justify-between">
            <span>May 21 - "Feeling Strong"</span>
            <span>Mood: 80 | Energy: 75 | 🏃 💧 🍳</span>
          </div>
        </button>
      </div>
    </div>
  );
};

export default CaptainLogList;
