import React, { useState, useEffect } from 'react';
import { Activity, AlertTriangle, CheckCircle, ShieldCheck, Zap, Server } from 'lucide-react';

export default function Header({ metrics, onRefresh }) {
  const [timeStr, setTimeStr] = useState(new Date().toLocaleTimeString());

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeStr(new Date().toLocaleTimeString());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  return (
    <header className="bg-slate-900/90 backdrop-blur-md border-b border-slate-800 sticky top-0 z-30 px-6 py-3.5 shadow-xl">
      <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
        {/* Title / Branding */}
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400 shadow-inner">
            <Zap className="w-6 h-6 animate-pulse" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="font-['Outfit'] font-bold text-xl text-slate-100 tracking-tight">KSPDB Operator Console</h1>
              <span className="bg-amber-500/20 text-amber-300 text-xs font-semibold px-2 py-0.5 rounded-full border border-amber-500/40">
                South Division
              </span>
            </div>
            <p className="text-xs text-slate-400 font-medium">Karnataka State Power Distribution Board • Automated IoT Fault Localization</p>
          </div>
        </div>

        {/* Metrics Cards */}
        <div className="flex items-center gap-3 flex-wrap">
          <div className="bg-slate-800/80 border border-slate-700/60 rounded-lg px-3.5 py-1.5 flex items-center gap-2.5">
            <Server className="w-4 h-4 text-emerald-400" />
            <div>
              <div className="text-[10px] text-slate-400 uppercase font-bold">Total Poles</div>
              <div className="text-sm font-bold text-slate-100">{metrics?.totalPoles ?? 0} ({metrics?.totalDevices ?? 0} IoT)</div>
            </div>
          </div>

          <div className="bg-slate-800/80 border border-slate-700/60 rounded-lg px-3.5 py-1.5 flex items-center gap-2.5">
            <Activity className="w-4 h-4 text-rose-400" />
            <div>
              <div className="text-[10px] text-slate-400 uppercase font-bold">Dark Poles</div>
              <div className="text-sm font-bold text-rose-400">{metrics?.darkPoles ?? 0}</div>
            </div>
          </div>

          <div className="bg-slate-800/80 border border-slate-700/60 rounded-lg px-3.5 py-1.5 flex items-center gap-2.5">
            <AlertTriangle className="w-4 h-4 text-amber-400" />
            <div>
              <div className="text-[10px] text-slate-400 uppercase font-bold">Active Incidents</div>
              <div className="text-sm font-bold text-amber-300">{metrics?.activeIncidents ?? 0}</div>
            </div>
          </div>

          <div className="bg-slate-800/80 border border-slate-700/60 rounded-lg px-3.5 py-1.5 flex items-center gap-2.5">
            <ShieldCheck className="w-4 h-4 text-cyan-400" />
            <div>
              <div className="text-[10px] text-slate-400 uppercase font-bold">Auto-Verified</div>
              <div className="text-sm font-bold text-cyan-300">{metrics?.closedTickets ?? 0}</div>
            </div>
          </div>

          {/* Time & Live Indicator */}
          <div className="hidden lg:flex items-center gap-2 bg-slate-950 border border-slate-800 px-3 py-1.5 rounded-lg text-xs font-mono text-slate-300">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-ping"></span>
            <span>{timeStr}</span>
          </div>
        </div>
      </div>
    </header>
  );
}
