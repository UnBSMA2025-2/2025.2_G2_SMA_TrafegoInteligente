import React, { useEffect, useState } from "react";
import {
  getSpawns,
  getCars,
  addCars,
  removeCar,
  shutdownSystem,
  getSystemStatus,
} from "../../services/api";
import "./Controls.css";

const Controls = () => {
  const [spawns, setSpawns] = useState([]);
  const [cars, setCars] = useState([]);
  const [status, setStatus] = useState({});
  const [selectedSpawn, setSelectedSpawn] = useState("");
  const [carCount, setCarCount] = useState(1);
  const [carToRemove, setCarToRemove] = useState("");
  const [logs, setLogs] = useState([]); // 👈 novo estado para logs

  // Carrega spawns, status e carros ao iniciar
  useEffect(() => {
    loadInitialData();
    // Exemplo: log inicial
    addLog("Painel iniciado. Aguardando conexão com o servidor...");
  }, []);

  async function loadInitialData() {
    const [spawnData, carData, statusData] = await Promise.all([
      getSpawns(),
      getCars(),
      getSystemStatus(),
    ]);
    setSpawns(spawnData);
    setCars(carData);
    setStatus(statusData);

    addLog("Status atualizado com sucesso.");
  }

  async function handleAddCar(e) {
    e.preventDefault();
    const result = await addCars(carCount, selectedSpawn || null);
    addLog(`Adicionado ${carCount} carro(s).`);
    alert(result);
    await loadInitialData();
  }

  async function handleRemoveCar(e) {
    e.preventDefault();
    const result = await removeCar(carToRemove);
    addLog(`Removido veículo: ${carToRemove}`);
    alert(result);
    await loadInitialData();
  }

  async function handleShutdown() {
    const result = await shutdownSystem();
    addLog("Sistema JADE sendo encerrado...");
    alert(result.message || "Sistema encerrando...");
  }

  // Função auxiliar para adicionar logs
  function addLog(message) {
    const timestamp = new Date().toLocaleTimeString();
    setLogs((prev) => [...prev, `[${timestamp}] ${message}`]);
  }

  return (
    <aside className="controls-panel">
      <div
        className={`status-indicator ${
          status.active ? "connected" : "disconnected"
        }`}
      >
        {status.active ? "CONECTADO AO SERVIDOR" : "DESCONECTADO"}
      </div>

      <div className="control-group">
        <h2>Simulação</h2>
        <div className="button-row">
          <button className="btn btn-success" onClick={loadInitialData}>
            ▶️ Atualizar Status
          </button>
          <button className="btn btn-warning" onClick={handleShutdown}>
            ⏹️ Encerrar Sistema
          </button>
        </div>
      </div>

      <div className="control-group">
        <h2>Adicionar Veículo</h2>
        <form className="command-form" onSubmit={handleAddCar}>
          <label htmlFor="spawn-point">Spawn Point</label>
          <select
            id="spawn-point"
            value={selectedSpawn}
            onChange={(e) => setSelectedSpawn(e.target.value)}
          >
            <option value="">Aleatório</option>
            {spawns.map((s) => (
              <option key={s.name} value={s.name}>
                {s.name} (x: {s.x}, y: {s.y})
              </option>
            ))}
          </select>

          <label htmlFor="car-count">Quantidade</label>
          <input
            type="number"
            id="car-count"
            min="1"
            value={carCount}
            onChange={(e) => setCarCount(parseInt(e.target.value))}
          />

          <button type="submit" className="btn btn-primary">
            Adicionar
          </button>
        </form>
      </div>

      <div className="control-group">
        <h2>Remover Veículo</h2>
        <form className="command-form" onSubmit={handleRemoveCar}>
          <label htmlFor="car-id">ID do Veículo</label>
          <input
            type="text"
            id="car-id"
            value={carToRemove}
            onChange={(e) => setCarToRemove(e.target.value)}
            placeholder="Ex: Car1"
          />
          <button type="submit" className="btn btn-danger">
            Remover
          </button>
        </form>
      </div>

      <div className="control-group">
        <h2>Veículos Ativos</h2>
        <div className="list-container">
          <ul>
            {cars.length > 0 ? (
              cars.map((c) => <li key={c}>{c}</li>)
            ) : (
              <li>Nenhum carro ativo</li>
            )}
          </ul>
        </div>
      </div>

      {/* NOVO BLOCO DE LOGS */}
      <div className="control-group">
        <h2>Logs do Sistema</h2>
        <div className="logs-container">
          {logs.length > 0 ? (
            <pre>{logs.join("\n")}</pre>
          ) : (
            <pre>Nenhum log disponível.</pre>
          )}
        </div>
      </div>
    </aside>
  );
};

export default Controls;
