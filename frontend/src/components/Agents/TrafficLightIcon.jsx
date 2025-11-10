// frontend/src/components/Agents/TrafficLightIcon.jsx
import React from 'react';
import './Agents.css';

const TrafficLightIcon = ({ state }) => {
  const isRed = state === 'VERMELHO';
  const isGreen = state === 'VERDE';

  return (
    <div className="traffic-light-icon">
      <div className={`light red ${isRed ? 'active' : ''}`}></div>
      <div className={`light yellow ${!isRed && !isGreen ? 'active' : ''}`}></div>
      <div className={`light green ${isGreen ? 'active' : ''}`}></div>
    </div>
  );
};

export default TrafficLightIcon;