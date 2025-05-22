import React from "react";
import TodayStats from "../components/TodayStats";
import TrendChartStack from "../components/TrendChartStack";
import CaptainLogList from "../components/CaptainLogList";
import LogModal from "../components/LogModal"; // We'll build this soon

const Dashboard = () => {
  return (
    <div className="min-h-screen p-6 bg-gray-100 text-gray-800">
      {/* Header */}
      <h1 className="text-3xl font-bold mb-6">Captain’s Log Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* LEFT COLUMN */}
        <div className="col-span-1 space-y-6">
          {/* Today’s stats */}
          <TodayStats />

          {/* Log entries */}
          <CaptainLogList />

          {/* Add log button */}
          <LogModal />
        </div>

        {/* RIGHT COLUMN */}
        <div className="col-span-2">
          <TrendChartStack />
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
