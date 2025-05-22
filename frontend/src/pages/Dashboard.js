import React from "react";
import TodayStats from "../components/TodayStats";
import TrendChartStack from "../components/TrendChartStack";
import CaptainLogList from "../components/CaptainLogList";
import LogModal from "../components/LogModal";

const Dashboard = () => {
  return (
    <div className="grid grid-cols-5 gap-4 p-6 h-screen overflow-y-auto bg-gray-50">
      {/* LEFT SIDE */}
      <div className="col-span-2 flex flex-col space-y-4">
        <TodayStats />
        <CaptainLogList />
      </div>

      {/* RIGHT SIDE */}
      <div className="col-span-3">
        <TrendChartStack />
      </div>

      <LogModal />
    </div>
  );
};

export default Dashboard;
