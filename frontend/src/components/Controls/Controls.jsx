// frontend/src/components/Controls/Controls.jsx
import React from 'react';
import './Controls.css';

const Controls = () => {
  return (
    <aside className="controls-panel">
      
      <div className="status-indicator connected">
        CONECTADO AO SERVIDOR
      </div>

      <div className="control-group">
        <h2>Simulação</h2>
        {/* --- ALTERAÇÃO AQUI --- */}
        {/* Adicionamos um 'div' para agrupar os botões lado a lado */}
        <div className="button-row">
          <button className="btn btn-success">▶️ Iniciar</button>
          <button className="btn btn-warning">⏹️ Resetar</button>
        </div>
      </div>

      <div className="control-group">
        <h2>Adicionar Veículo</h2>
        <form className="command-form">
          <label htmlFor="spawn-point">Spawn Point (listspawn)</label>
          <select id="spawn-point" name="spawn-point">
            <option value="spawn_0_3">Spawn (0, 3)</option>
            <option value="spawn_0_4">Spawn (0, 4)</option>
          </select>
          
          <label htmlFor="car-count">Quantidade (N)</label>
          <input type="number" id="car-count" defaultValue="1" min="1" />
          
          <button type="submit" className="btn btn-primary">Adicionar</button>
        </form>
      </div>

      <div className="control-group">
        <h2>Remover Veículo</h2>
        <form className="command-form">
          <label htmlFor="car-id">ID do Veículo (X)</label>
          <input type="text" id="car-id" placeholder="Ex: CarMock1" />
          <button type="submit" className="btn btn-danger">Remover</button>
        </form>
      </div>

      <div className="control-group">
        <h2>Veículos Ativos (list)</h2>
        <div className="list-container">
          <ul>
            <li>CarMock1 (X: 0, Y: 3)</li>
          </ul>
        </div>
      </div>
      
    </aside>
  );
};

export default Controls;