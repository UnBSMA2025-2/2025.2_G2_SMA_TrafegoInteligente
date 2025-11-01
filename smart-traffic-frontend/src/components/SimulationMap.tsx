import React from 'react';
import { CarState, PardalState, TrafficLightState } from '../App';

interface SimulationMapProps {
  trafficLights: TrafficLightState[];
  cars: CarState[];
  pardal: PardalState;
}

export const SimulationMap: React.FC<SimulationMapProps> = ({
  trafficLights,
  cars,
  pardal,
}) => {
  return (
    <main className="map-container">
      {/* --- 1. RUAS --- */}
      {/* Rua Horizontal (Y=380, Altura=40) */}
      <div className="road horizontal"></div>
      {/* Rua Vertical (X=480, Largura=40) */}
      <div className="road vertical"></div>

      {/* --- 2. SEMÁFOROS --- */}
      {trafficLights.map((light) => (
        <div
          key={light.id}
          className="traffic-light"
          style={{
            top: `${light.y}px`,
            left: `${light.x}px`,
          }}
        >
          <div
            className={`light red ${light.state === 'RED' ? 'active' : ''}`}
          ></div>
          <div
            className={`light green ${light.state === 'GREEN' ? 'active' : ''}`}
          ></div>
        </div>
      ))}

      {/* --- 3. PARDAL (CÂMERA) --- */}
      <div
        className={`pardal ${pardal.triggered ? 'triggered' : ''}`}
        style={{
          top: `${pardal.y}px`,
          left: `${pardal.x}px`,
        }}
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M2 8V6a2 2 0 0 1 2-2h3" />
          <path d="M10 4h4" />
          <path d="M22 8V6a2 2 0 0 0-2-2h-3" />
          <path d="M2 16v2a2 2 0 0 0 2 2h3" />
          <path d="M22 16v2a2 2 0 0 1-2 2h-3" />
          <path d="M10 20h4" />
          <circle cx="12" cy="12" r="3" />
          <path d="M12 1v2" />
          <path d="M12 21v2" />
        </svg>
      </div>

      {/* --- 4. CARROS --- */}
      {cars.map((car) => (
        <div
          key={car.id}
          className="car"
          style={{
            top: `${car.y}px`,
            left: `${car.x}px`,
            // Aplica a rotação do carro
            transform: `rotate(${car.rotation}deg)`,
          }}
        >
          {/* SVG do Carro */}
          <svg
            viewBox="0 0 240 400"
            xmlns="http://www.w3.org/2000/svg"
            fill="#3b82f6" // Cor azul
          >
            <path d="M60 20 L180 20 Q220 20 220 60 L220 340 Q220 380 180 380 L60 380 Q20 380 20 340 L20 60 Q20 20 60 20 Z" />
            <path
              d="M40 80 L200 80 L200 120 L40 120 Z"
              fill="#c0c0c0"
              stroke="#000"
              strokeWidth="5"
            />
            <path
              d="M40 240 L200 240 L200 280 L40 280 Z"
              fill="#c0c0c0"
              stroke="#000"
              strokeWidth="5"
            />
            <rect
              x="10"
              y="40"
              width="220"
              height="30"
              fill="#ffff00"
              opacity="0.8"
            />
            <rect
              x="10"
              y="330"
              width="220"
              height="30"
              fill="#ff0000"
              opacity="0.8"
            />
          </svg>
        </div>
      ))}
    </main>
  );
};

