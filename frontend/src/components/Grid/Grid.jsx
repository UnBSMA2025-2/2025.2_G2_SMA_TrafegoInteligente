// frontend/src/components/Grid/Grid.jsx
import React from 'react';
import { gridConfig } from '../../services/mapConfig';
import Cell from './Cell';
import './Grid.css';

const Grid = () => {
  // Define o estilo CSS para o grid (baseado no config)
  const gridStyle = {
    display: 'grid',
    gridTemplateColumns: `repeat(${gridConfig.width}, 1fr)`,
  };

  return (
    <div className="grid-container" style={gridStyle}>
      {/*
       * Mapeia o layout 2D para renderizar as células.
       * 'flatMap' é uma forma limpa de achatar um array 2D.
      */}
      {gridConfig.layout.flatMap((row, y) => (
        row.map((cellType, x) => (
          <Cell 
            key={`${x}-${y}`} 
            type={cellType} 
          />
        ))
      ))}
    </div>
  );
};

export default Grid;