import React from 'react';
import { AlertCircle, CheckCircle2, Clock, MapPin, Sparkles, UserCheck, Wrench, ShieldAlert } from 'lucide-react';

export default function TicketList({ tickets = [], onUpdateStatus, onOpenAiBrief, actionError }) {
  const safeTickets = Array.isArray(tickets) ? tickets : [];

  const getStatusBadge = (status) => {
    switch (status) {
      case 'DETECTED':
        return <span className="bg-rose-500/20 text-rose-300 border border-rose-500/30 text-xs px-2.5 py-0.5 rounded-full font-semibold flex items-center gap-1"><AlertCircle className="w-3.5 h-3.5" /> DETECTED</span>;
      case 'ACKNOWLEDGED':
        return <span className="bg-amber-500/20 text-amber-300 border border-amber-500/30 text-xs px-2.5 py-0.5 rounded-full font-semibold flex items-center gap-1"><Clock className="w-3.5 h-3.5" /> ACKNOWLEDGED</span>;
      case 'CREW_ASSIGNED':
        return <span className="bg-blue-500/20 text-blue-300 border border-blue-500/30 text-xs px-2.5 py-0.5 rounded-full font-semibold flex items-center gap-1"><UserCheck className="w-3.5 h-3.5" /> CREW ASSIGNED</span>;
      case 'RESOLVED':
        return <span className="bg-purple-500/20 text-purple-300 border border-purple-500/30 text-xs px-2.5 py-0.5 rounded-full font-semibold flex items-center gap-1"><Wrench className="w-3.5 h-3.5" /> RESOLVED</span>;
      case 'VERIFIED':
      case 'CLOSED':
        return <span className="bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-xs px-2.5 py-0.5 rounded-full font-semibold flex items-center gap-1"><CheckCircle2 className="w-3.5 h-3.5" /> AUTO-VERIFIED</span>;
      default:
        return <span className="bg-slate-700 text-slate-300 text-xs px-2.5 py-0.5 rounded-full font-semibold">{status}</span>;
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-4 flex flex-col h-full shadow-2xl">
      <div className="flex items-center justify-between border-b border-slate-800 pb-3 mb-4">
        <div>
          <h2 className="font-['Outfit'] font-bold text-lg text-slate-100 flex items-center gap-2">
            Control Room Incident Feed
          </h2>
          <p className="text-xs text-slate-400">Live ticket lifecycle & telemetry verification</p>
        </div>
        <span className="bg-slate-800 text-slate-300 text-xs font-mono px-2.5 py-1 rounded-md border border-slate-700">
          {safeTickets.length} Incidents
        </span>
      </div>

      {/* Action Rejection Error Alert Banner */}
      {actionError && (
        <div className="mb-4 bg-rose-950/80 border border-rose-500/50 rounded-lg p-3 text-xs text-rose-200 flex items-start gap-2.5 shadow-lg animate-bounce">
          <ShieldAlert className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
          <div>
            <div className="font-bold text-rose-300 text-sm">Restoration Unverified</div>
            <div className="text-slate-300 leading-relaxed mt-0.5">{actionError}</div>
          </div>
        </div>
      )}

      {/* Ticket Cards Feed */}
      <div className="flex-1 overflow-y-auto space-y-3.5 pr-1">
        {safeTickets.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-center py-12 text-slate-500">
            <CheckCircle2 className="w-12 h-12 text-emerald-500/40 mb-3" />
            <div className="font-semibold text-slate-300 text-sm">All Power Lines Energized</div>
            <div className="text-xs text-slate-500 mt-1 max-w-xs">No active faults detected across South Division LT network.</div>
          </div>
        ) : (
          safeTickets.map(ticket => {
            const inc = ticket.incident || {};
            const confidence = inc.confidence ?? 85.0;

            return (
              <div key={ticket.id} className="bg-slate-800/70 border border-slate-700/80 hover:border-slate-600 rounded-xl p-4 transition-all shadow-md">
                {/* Header */}
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs font-bold text-amber-400">{ticket.ticketNumber}</span>
                    <span className="text-[10px] bg-slate-700/80 text-slate-300 px-2 py-0.5 rounded font-mono">
                      {inc.faultType || 'SPAN_FAULT'}
                    </span>
                  </div>
                  {getStatusBadge(ticket.status)}
                </div>

                {/* Location & Span Details */}
                <div className="space-y-1.5 mb-3">
                  <div className="text-sm font-semibold text-slate-100 flex items-center gap-1.5">
                    <MapPin className="w-4 h-4 text-amber-400 shrink-0" />
                    <span>
                      {inc.fromPole ? inc.fromPole.poleCode : 'DT'} ➔ {inc.toPole ? inc.toPole.poleCode : 'DARK'}
                    </span>
                  </div>
                  <div className="text-xs text-slate-300 line-clamp-2 leading-relaxed bg-slate-900/60 p-2 rounded border border-slate-800">
                    {inc.reasoning}
                  </div>
                </div>

                {/* Metrics Grid */}
                <div className="grid grid-cols-3 gap-2 mb-3 bg-slate-900/40 p-2 rounded-lg text-center text-xs border border-slate-800">
                  <div>
                    <div className="text-[10px] text-slate-400 uppercase">PIN Code</div>
                    <div className="font-semibold text-slate-200">{inc.pincode || '560078'}</div>
                  </div>
                  <div>
                    <div className="text-[10px] text-slate-400 uppercase">Downstream</div>
                    <div className="font-semibold text-amber-300">{inc.affectedPoleCount || 1} Poles</div>
                  </div>
                  <div>
                    <div className="text-[10px] text-slate-400 uppercase">Confidence</div>
                    <div className="font-semibold text-emerald-400">{confidence}%</div>
                  </div>
                </div>

                {/* Confidence Bar */}
                <div className="w-full bg-slate-900 rounded-full h-1.5 mb-3 overflow-hidden">
                  <div
                    className={`h-1.5 rounded-full ${confidence >= 90 ? 'bg-emerald-500' : 'bg-amber-500'}`}
                    style={{ width: `${confidence}%` }}
                  ></div>
                </div>

                {/* Workflow Buttons */}
                <div className="flex items-center justify-between gap-2 pt-2 border-t border-slate-700/60">
                  <button
                    onClick={() => onOpenAiBrief(inc.id)}
                    className="bg-amber-500/10 hover:bg-amber-500/20 text-amber-300 border border-amber-500/30 text-xs px-3 py-1.5 rounded-lg flex items-center gap-1.5 font-medium transition-colors cursor-pointer"
                  >
                    <Sparkles className="w-3.5 h-3.5 text-amber-400" />
                    AI Dispatch Brief
                  </button>

                  <div className="flex items-center gap-2">
                    {ticket.status === 'DETECTED' && (
                      <button
                        onClick={() => onUpdateStatus(ticket.id, 'ACKNOWLEDGED')}
                        className="bg-amber-600 hover:bg-amber-500 text-slate-950 font-semibold text-xs px-3 py-1.5 rounded-lg transition-colors cursor-pointer"
                      >
                        Acknowledge
                      </button>
                    )}

                    {ticket.status === 'ACKNOWLEDGED' && (
                      <button
                        onClick={() => onUpdateStatus(ticket.id, 'CREW_ASSIGNED')}
                        className="bg-blue-600 hover:bg-blue-500 text-white font-semibold text-xs px-3 py-1.5 rounded-lg transition-colors cursor-pointer"
                      >
                        Assign Crew
                      </button>
                    )}

                    {(ticket.status === 'CREW_ASSIGNED' || ticket.status === 'ACKNOWLEDGED') && (
                      <button
                        onClick={() => onUpdateStatus(ticket.id, 'RESOLVED')}
                        className="bg-purple-600 hover:bg-purple-500 text-white font-semibold text-xs px-3 py-1.5 rounded-lg transition-colors cursor-pointer"
                      >
                        Mark Resolved
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
