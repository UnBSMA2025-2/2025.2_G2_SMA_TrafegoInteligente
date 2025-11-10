// frontend/src/components/Grid/Cell.jsx
import React from 'react';
import { CELL_STYLES } from '../../services/mapConfig';
import './Grid.css';

const Cell = ({ type }) => {
  // Pega o estilo (apenas cor) do mapConfig
  const style = CELL_STYLES[type] || CELL_STYLES.empty;
  
  const cellStyle = {
    backgroundColor: style.backgroundColor,
  };

  return (
    <div className="grid-cell" style={cellStyle}>
      {/* O conteúdo virá na camada de Agentes */}
    </div>
  );
};

export default Cell;