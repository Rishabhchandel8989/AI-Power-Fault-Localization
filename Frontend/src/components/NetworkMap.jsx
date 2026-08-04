import React from 'react';
import { MapContainer, TileLayer, CircleMarker, Polyline, Popup } from 'react-leaflet';

export default function NetworkMap({ poles, tickets, onSelectPole }) {
  const center = [12.9675, 77.5925];

  // Group poles by DT for polyline connections
  const dtGroups = {};
  poles.forEach(p => {
    if (!dtGroups[p.transformerCode]) {
      dtGroups[p.transformerCode] = [];
    }
    dtGroups[p.transformerCode].push(p);
  });

  // Calculate connections between parent & child poles
  const lines = [];
  poles.forEach(pole => {
    if (pole.parentPoleCode) {
      const parent = poles.find(p => p.poleCode === pole.parentPoleCode);
      if (parent) {
        const isBroken = !pole.energized && parent.energized;
        lines.push({
          id: `${parent.poleCode}-${pole.poleCode}`,
          positions: [
            [parent.latitude, parent.longitude],
            [pole.latitude, pole.longitude]
          ],
          isBroken,
          dtCode: pole.transformerCode
        });
      }
    }
  });

  return (
    <div className="w-full h-full relative rounded-xl overflow-hidden border border-slate-800 shadow-2xl bg-slate-900">
      <MapContainer center={center} zoom={16} scrollWheelZoom={true} className="w-full h-full">
        {/* OpenStreetMap Dark Carto Tile Layer */}
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        />

        {/* Polylines for LT lines */}
        {lines.map(line => (
          <Polyline
            key={line.id}
            positions={line.positions}
            pathOptions={{
              color: line.isBroken ? '#ef4444' : '#3b82f6',
              weight: line.isBroken ? 4 : 2,
              dashArray: line.isBroken ? '8, 8' : undefined,
              opacity: 0.8
            }}
          />
        ))}

        {/* Pole Markers */}
        {poles.map(pole => {
          let fillColor = '#10b981'; // Green (Energized)
          if (!pole.hasDevice) {
            fillColor = '#64748b'; // Slate (No Device)
          } else if (!pole.energized) {
            fillColor = '#ef4444'; // Red (Dark / Fault)
          }

          return (
            <CircleMarker
              key={pole.id}
              center={[pole.latitude, pole.longitude]}
              radius={pole.energized ? 6 : 9}
              pathOptions={{
                fillColor: fillColor,
                fillOpacity: 0.9,
                color: pole.energized ? '#065f46' : '#991b1b',
                weight: 2
              }}
              eventHandlers={{
                click: () => onSelectPole && onSelectPole(pole)
              }}
            >
              <Popup>
                <div className="p-1 space-y-1.5 text-slate-100">
                  <div className="flex items-center justify-between gap-2 border-b border-slate-700 pb-1">
                    <span className="font-bold text-amber-400 text-sm">{pole.poleCode}</span>
                    <span className={`text-[10px] px-2 py-0.5 rounded font-bold uppercase ${pole.energized ? 'bg-emerald-500/20 text-emerald-300' : 'bg-rose-500/20 text-rose-300'}`}>
                      {pole.energized ? 'ENERGIZED' : 'DARK'}
                    </span>
                  </div>
                  <div className="text-xs space-y-1 text-slate-300">
                    <div><span className="text-slate-400">DT:</span> {pole.transformerCode}</div>
                    <div><span className="text-slate-400">Feeder:</span> {pole.feederCode}</div>
                    <div><span className="text-slate-400">Parent:</span> {pole.parentPoleCode || 'N/A (Substation/DT)'}</div>
                    <div><span className="text-slate-400">GPS:</span> {pole.latitude.toFixed(6)}, {pole.longitude.toFixed(6)}</div>
                    <div><span className="text-slate-400">PIN Code:</span> {pole.pincode}</div>
                    <div><span className="text-slate-400">Firmware:</span> {pole.firmwareVersion}</div>
                    <div>
                      <span className="text-slate-400">Topology:</span>{' '}
                      <span className={pole.topologyKnown ? 'text-emerald-400 font-semibold' : 'text-amber-400 font-semibold'}>
                        {pole.topologyKnown ? 'Mapped (1:1)' : '60% Unmapped Case'}
                      </span>
                    </div>
                  </div>
                </div>
              </Popup>
            </CircleMarker>
          );
        })}
      </MapContainer>

      {/* Legend */}
      <div className="absolute bottom-4 left-4 bg-slate-900/90 backdrop-blur-md border border-slate-800 rounded-lg p-3 z-[1000] text-xs space-y-2 shadow-xl">
        <div className="font-bold text-slate-200 text-xs border-b border-slate-800 pb-1">Network Legend</div>
        <div className="flex items-center gap-2">
          <span className="w-3 h-3 rounded-full bg-emerald-500"></span>
          <span className="text-slate-300">Energized Pole</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="w-3 h-3 rounded-full bg-rose-500 animate-pulse"></span>
          <span className="text-slate-300">De-Energized / Fault</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="w-3 h-3 rounded-full bg-slate-500"></span>
          <span className="text-slate-400">Unequipped Pole (No Device)</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="w-6 h-0.5 bg-rose-500 border-t border-dashed border-rose-500"></span>
          <span className="text-slate-300">Faulted Span Line</span>
        </div>
      </div>
    </div>
  );
}
