// frontend/src/components/Agents/CarIcon.jsx
import React from 'react';
import './Agents.css';

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
    // Aplicamos a rotação no wrapper, não no ícone
    transform: `rotate(${getRotation(direction)})`,
  };

  return (
    <div className="car-icon-wrapper" style={style}>
      {/* Este é o novo "corpo" do carro feito em CSS */}
      <div className="car-body-css"></div>
    </div>
  );
};

export default CarIcon;