// frontend/src/components/Agents/CarIcon.jsx
import React from 'react';
import './Agents.css';

// 1. Correção: Importa da pasta 'images'
import carIconUrl from '../../assets/images/car_black_1.png';

// Mapeia a direção do backend (SUL, NORTE) para graus CSS
const getRotation = (direction) => {
  switch (direction) {
    case 'NORTE':
      return '-90deg';
    case 'SUL':
      return '90deg';
    case 'LESTE':
      return '0deg';
    case 'OESTE':
      return '180deg';
    default:
      return '0deg';
  }
};

const CarIcon = ({ direction }) => {
  const style = {
    transform: `rotate(${getRotation(direction)})`,
  };

  return (
    <div className="car-icon" style={style}>
      {/* 2. Usa a tag <img> normal */}
      <img 
        src={carIconUrl} 
        alt="Carro" 
        className="car-svg-img" 
      />
    </div>
  );
};

export default CarIcon;