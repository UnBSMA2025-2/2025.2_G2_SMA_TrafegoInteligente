// frontend/src/components/Agents/TrafficLightIcon.jsx
import React from 'react';
import './Agents.css';

/**
 * Componente que renderiza um semáforo.
 * Ele recebe 'state' que pode ser "VERDE" ou "VERMELHO".
 */
const TrafficLightIcon = ({ state }) => {
  const isRed = state === 'VERMELHO';
  const isGreen = state === 'VERDE';

  return (
    <div className="traffic-light-icon">
      <div 
        className={`light red ${isRed ? 'active' : ''}`}
      ></div>
      <div 
        className={`light yellow ${!isRed && !isGreen ? 'active' : ''}`}
      ></div>
      <div 
        className={`light green ${isGreen ? 'active' : ''}`}
      ></div>
    </div>
  );
};

export default TrafficLightIcon;