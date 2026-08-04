import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import Header from './components/Header';
import NetworkMap from './components/NetworkMap';
import TicketList from './components/TicketList';
import SimulatorPanel from './components/SimulatorPanel';
import AiBriefingModal from './components/AiBriefingModal';

export default function App() {
  const [metrics, setMetrics] = useState(null);
  const [poles, setPoles] = useState([]);
  const [tickets, setTickets] = useState([]);
  const [selectedIncidentAiId, setSelectedIncidentAiId] = useState(null);
  const [actionError, setActionError] = useState(null);

  const fetchData = useCallback(async () => {
    try {
      const [mRes, pRes, tRes] = await Promise.all([
        axios.get('/api/v1/dashboard'),
        axios.get('/api/v1/poles'),
        axios.get('/api/v1/tickets')
      ]);
      setMetrics(mRes.data);
      setPoles(pRes.data);
      setTickets(tRes.data);
    } catch (err) {
      console.error('Failed to fetch dashboard data:', err);
    }
  }, []);

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 3000);
    return () => clearInterval(interval);
  }, [fetchData]);

  const handleUpdateStatus = async (ticketId, newStatus) => {
    setActionError(null);
    try {
      await axios.put(`/api/v1/tickets/${ticketId}/status`, { status: newStatus });
      fetchData();
    } catch (err) {
      const errorMsg = err.response?.data?.error || err.message || 'Failed to update ticket status.';
      setActionError(errorMsg);
      // Auto-clear error after 6 seconds
      setTimeout(() => setActionError(null), 6000);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-['Inter',sans-serif]">
      {/* Top Header */}
      <Header metrics={metrics} onRefresh={fetchData} />

      {/* Main Content Layout */}
      <main className="flex-1 p-4 lg:p-6 max-w-7xl mx-auto w-full space-y-4">
        {/* Simulator Control Panel */}
        <SimulatorPanel onActionComplete={fetchData} />

        {/* Map & Ticket Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 h-[620px]">
          {/* Leaflet Network Map (8 Cols) */}
          <div className="lg:col-span-8 h-full">
            <NetworkMap poles={poles} tickets={tickets} />
          </div>

          {/* Ticket Feed (4 Cols) */}
          <div className="lg:col-span-4 h-full">
            <TicketList
              tickets={tickets}
              onUpdateStatus={handleUpdateStatus}
              onOpenAiBrief={id => setSelectedIncidentAiId(id)}
              actionError={actionError}
            />
          </div>
        </div>
      </main>

      {/* AI Briefing Modal */}
      {selectedIncidentAiId && (
        <AiBriefingModal
          incidentId={selectedIncidentAiId}
          onClose={() => setSelectedIncidentAiId(null)}
        />
      )}

      {/* Footer */}
      <footer className="border-t border-slate-900 bg-slate-950 py-3 text-center text-xs text-slate-500">
        Karnataka State Power Distribution Board (KSPDB) • Autonomous IoT Fault Localization & Auto-Verified Ticket Lifecycle
      </footer>
    </div>
  );
}
