// frontend/src/App.jsx
import React from 'react';
import Header from './components/Layout/Header';
import Controls from './components/Controls/Controls';
import Grid from './components/Grid/Grid';
import AgentLayer from './components/Grid/AgentLayer';
import './App.css';

function App() {
  return (
    <div className="app-container">
      {/* 1. O Cabeçalho fica no topo */}
      <Header />

      {/* 2. O conteúdo principal com layout flexível */}
      <div className="main-content">
        
        {/* 3. O Painel de Controle na esquerda */}
        <Controls />
        
        {/* 4. A Simulação na direita */}
        <main className="simulation-area">
          <div className="simulation-wrapper">
            <Grid />
            <AgentLayer />
          </div>
        </main>

      </div>
    </div>
  );
}

export default App;