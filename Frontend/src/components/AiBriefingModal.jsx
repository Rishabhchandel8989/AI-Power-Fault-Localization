import React, { useState, useEffect } from 'react';
import { Sparkles, X, Shield, Package, AlertTriangle, Users, MapPin, CheckCircle, Info } from 'lucide-react';
import axios from 'axios';

export default function AiBriefingModal({ incidentId, onClose }) {
  const [brief, setBrief] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (incidentId) {
      setLoading(true);
      axios.get(`/api/v1/incidents/${incidentId}/ai-brief`)
        .then(res => setBrief(res.data))
        .catch(err => console.error(err))
        .finally(() => setLoading(false));
    }
  }, [incidentId]);

  if (!incidentId) return null;

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-slate-900 border border-slate-700/80 rounded-2xl max-w-2xl w-full p-6 shadow-2xl space-y-5 relative max-h-[90vh] overflow-y-auto">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-slate-400 hover:text-slate-100 p-1.5 rounded-lg hover:bg-slate-800 transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Header */}
        <div className="flex items-center gap-3 border-b border-slate-800 pb-4">
          <div className="w-10 h-10 rounded-xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-amber-400">
            <Sparkles className="w-5 h-5 animate-pulse" />
          </div>
          <div>
            <h3 className="font-['Outfit'] font-bold text-xl text-slate-100 flex items-center gap-2">
              AI Lineman Dispatch Briefing
            </h3>
            <p className="text-xs text-slate-400">Synthesized incident analysis & crew field instructions</p>
          </div>
        </div>

        {loading ? (
          <div className="py-12 text-center text-slate-400 space-y-3">
            <Sparkles className="w-8 h-8 text-amber-400 animate-spin mx-auto" />
            <div className="text-sm font-medium">Generating AI Dispatch Briefing from Graph Topology...</div>
          </div>
        ) : brief ? (
          <div className="space-y-4 text-sm text-slate-200">
            {/* Severity & Incident Badge */}
            <div className="flex items-center justify-between bg-slate-800/80 p-3 rounded-xl border border-slate-700">
              <div>
                <div className="text-[10px] uppercase font-bold text-slate-400">Incident Reference</div>
                <div className="font-mono text-sm font-bold text-amber-400">{brief.incidentNumber}</div>
              </div>
              <span className="bg-rose-500/20 text-rose-300 border border-rose-500/40 px-3 py-1 rounded-full text-xs font-bold uppercase">
                {brief.severity}
              </span>
            </div>

            {/* Summary Box */}
            <div className="bg-slate-950 p-4 rounded-xl border border-slate-800 space-y-1.5">
              <div className="text-xs font-bold text-amber-400 uppercase tracking-wider flex items-center gap-1.5">
                <Info className="w-4 h-4" /> Root Cause Summary
              </div>
              <p className="text-sm text-slate-200 leading-relaxed">{brief.summary}</p>
            </div>

            {/* Location & Household Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div className="bg-slate-800/50 p-3 rounded-xl border border-slate-700/60 space-y-1">
                <div className="text-xs text-slate-400 flex items-center gap-1.5">
                  <MapPin className="w-4 h-4 text-amber-400" /> Precise Dispatch Location
                </div>
                <div className="text-xs font-mono font-medium text-slate-200">{brief.locationDetails}</div>
              </div>

              <div className="bg-slate-800/50 p-3 rounded-xl border border-slate-700/60 space-y-1">
                <div className="text-xs text-slate-400 flex items-center gap-1.5">
                  <Users className="w-4 h-4 text-blue-400" /> Impact & Recommended Crew
                </div>
                <div className="text-xs font-medium text-slate-200">
                  Est. <span className="text-amber-300 font-bold">{brief.estimatedHouseholdImpact}</span> Households Affected • Crew Size: <span className="text-blue-300 font-bold">{brief.recommendedCrewSize} Linemen</span>
                </div>
              </div>
            </div>

            {/* Materials Checklist */}
            <div className="bg-slate-800/50 p-4 rounded-xl border border-slate-700/60 space-y-2">
              <div className="text-xs font-bold text-slate-300 uppercase flex items-center gap-1.5">
                <Package className="w-4 h-4 text-emerald-400" /> Dispatch Repair Materials Checklist
              </div>
              <ul className="grid grid-cols-1 md:grid-cols-2 gap-2 text-xs">
                {brief.materialsChecklist?.map((item, idx) => (
                  <li key={idx} className="flex items-center gap-2 text-slate-200 bg-slate-900/60 px-2.5 py-1.5 rounded border border-slate-800">
                    <CheckCircle className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Safety Directives */}
            <div className="bg-rose-950/30 p-4 rounded-xl border border-rose-500/30 space-y-2">
              <div className="text-xs font-bold text-rose-300 uppercase flex items-center gap-1.5">
                <Shield className="w-4 h-4 text-rose-400" /> Lineman Safety & Isolation Directives
              </div>
              <ul className="space-y-1.5 text-xs text-rose-200">
                {brief.safetyDirectives?.map((dir, idx) => (
                  <li key={idx} className="flex items-start gap-2">
                    <AlertTriangle className="w-3.5 h-3.5 text-rose-400 shrink-0 mt-0.5" />
                    <span>{dir}</span>
                  </li>
                ))}
              </ul>
            </div>

            {/* Topology Note */}
            <div className="text-xs text-slate-400 italic bg-slate-950 p-3 rounded-lg border border-slate-800">
              💡 <span className="font-semibold text-slate-300">Topology Note:</span> {brief.topologyNotes}
            </div>
          </div>
        ) : (
          <div className="py-6 text-center text-rose-400">Failed to load briefing data.</div>
        )}
      </div>
    </div>
  );
}
