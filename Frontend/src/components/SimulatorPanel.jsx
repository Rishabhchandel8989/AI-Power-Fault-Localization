import React, { useState } from 'react';
import { Play, RotateCcw, Zap, AlertOctagon, Radio, Calendar, CheckCircle2, ChevronRight, Sliders } from 'lucide-react';
import axios from 'axios';

export default function SimulatorPanel({ onActionComplete }) {
  const [selectedPole, setSelectedPole] = useState('P-0111-04');
  const [selectedDT, setSelectedDT] = useState('D-0112');
  const [selectedFeeder, setSelectedFeeder] = useState('F-07-01');
  const [statusMsg, setStatusMsg] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleRun = async (endpoint, data = null) => {
    setLoading(true);
    setStatusMsg(null);
    try {
      let res;
      if (data) {
        res = await axios.post(endpoint, data);
      } else {
        res = await axios.post(endpoint);
      }
      setStatusMsg({ type: 'success', text: res.data?.message || 'Simulation executed successfully.' });
      if (onActionComplete) onActionComplete();
    } catch (err) {
      setStatusMsg({ type: 'error', text: err.response?.data?.error || err.message || 'Simulation error.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 shadow-2xl space-y-4">
      <div className="flex items-center justify-between border-b border-slate-800 pb-3">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-amber-400">
            <Sliders className="w-4 h-4" />
          </div>
          <div>
            <h2 className="font-['Outfit'] font-bold text-base text-slate-100">Interactive Fault Simulator</h2>
            <p className="text-xs text-slate-400">Inject faults, noise, scheduled outages & restoration telemetry</p>
          </div>
        </div>
      </div>

      {statusMsg && (
        <div className={`p-2.5 rounded-lg text-xs font-medium flex items-center gap-2 ${statusMsg.type === 'success' ? 'bg-emerald-950/80 border border-emerald-500/40 text-emerald-300' : 'bg-rose-950/80 border border-rose-500/40 text-rose-300'}`}>
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{statusMsg.text}</span>
        </div>
      )}

      {/* Simulator Actions Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 text-xs">
        {/* 1. Span Fault */}
        <div className="bg-slate-800/60 p-3 rounded-lg border border-slate-700/70 space-y-2">
          <div className="font-bold text-slate-200 flex items-center gap-1.5">
            <Zap className="w-4 h-4 text-amber-400" /> Span Wire Snap
          </div>
          <select
            value={selectedPole}
            onChange={e => setSelectedPole(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 rounded px-2 py-1 text-slate-200 text-xs"
          >
            <option value="P-0111-04">P-0111-04 (Mapped DT)</option>
            <option value="P-0112-05">P-0112-05 (Mapped DT)</option>
            <option value="P-0113-03">P-0113-03 (60% Unmapped Case)</option>
          </select>
          <button
            disabled={loading}
            onClick={() => handleRun(`/api/v1/simulator/span/${selectedPole}`)}
            className="w-full bg-amber-600 hover:bg-amber-500 text-slate-950 font-bold py-1.5 px-3 rounded transition-colors cursor-pointer flex items-center justify-center gap-1"
          >
            <Play className="w-3.5 h-3.5 fill-current" /> Inject Span Fault
          </button>
        </div>

        {/* 2. DT Fault */}
        <div className="bg-slate-800/60 p-3 rounded-lg border border-slate-700/70 space-y-2">
          <div className="font-bold text-slate-200 flex items-center gap-1.5">
            <AlertOctagon className="w-4 h-4 text-rose-400" /> Transformer Fault
          </div>
          <select
            value={selectedDT}
            onChange={e => setSelectedDT(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 rounded px-2 py-1 text-slate-200 text-xs"
          >
            <option value="D-0111">D-0111 (Jayanagar East)</option>
            <option value="D-0112">D-0112 (Jayanagar Central)</option>
            <option value="D-0113">D-0113 (60% Unmapped DT)</option>
          </select>
          <button
            disabled={loading}
            onClick={() => handleRun(`/api/v1/simulator/transformer/${selectedDT}`)}
            className="w-full bg-rose-600 hover:bg-rose-500 text-white font-bold py-1.5 px-3 rounded transition-colors cursor-pointer flex items-center justify-center gap-1"
          >
            <Play className="w-3.5 h-3.5 fill-current" /> Inject DT Outage
          </button>
        </div>

        {/* 3. Sensor Noise & Outage */}
        <div className="bg-slate-800/60 p-3 rounded-lg border border-slate-700/70 space-y-2">
          <div className="font-bold text-slate-200 flex items-center gap-1.5">
            <Radio className="w-4 h-4 text-cyan-400" /> Noise / Scheduled Outage
          </div>
          <div className="flex gap-2">
            <button
              disabled={loading}
              onClick={() => handleRun(`/api/v1/simulator/device/P-0111-03`)}
              className="flex-1 bg-cyan-700 hover:bg-cyan-600 text-white font-semibold py-1.5 px-2 rounded text-[11px] transition-colors cursor-pointer"
            >
              Sensor Failure Noise
            </button>
            <button
              disabled={loading}
              onClick={() => handleRun(`/api/v1/simulator/outage`, { scope: 'feeder', targetId: 'F-07-01', reason: 'Planned Maintenance' })}
              className="flex-1 bg-purple-700 hover:bg-purple-600 text-white font-semibold py-1.5 px-2 rounded text-[11px] transition-colors cursor-pointer"
            >
              Scheduled Outage
            </button>
          </div>
        </div>

        {/* 4. Repair & Reset */}
        <div className="bg-slate-800/60 p-3 rounded-lg border border-slate-700/70 space-y-2">
          <div className="font-bold text-slate-200 flex items-center gap-1.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-400" /> Field Repair & Reset
          </div>
          <div className="flex gap-2">
            <button
              disabled={loading}
              onClick={() => handleRun(`/api/v1/simulator/repair/${selectedPole}`)}
              className="flex-1 bg-emerald-600 hover:bg-emerald-500 text-slate-950 font-bold py-1.5 px-2 rounded text-[11px] transition-colors cursor-pointer"
            >
              Repair (Auto-Verify)
            </button>
            <button
              disabled={loading}
              onClick={() => handleRun(`/api/v1/simulator/reset`)}
              className="bg-slate-700 hover:bg-slate-600 text-slate-200 font-semibold p-1.5 rounded transition-colors cursor-pointer"
              title="Reset Network"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
