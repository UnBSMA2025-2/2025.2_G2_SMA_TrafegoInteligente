// src/pages/LogsPage.jsx
import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { connectWebSocket, disconnectWebSocket } from "../services/socket";
import "./LogsPage.css";

const LogsPage = () => {
  const [logs, setLogs] = useState({
    sistema: [],
    semaforo: [],
    carro: [],
    pardal: [],
  });

  const [summary, setSummary] = useState({
    semaforosVerdes: 0,
    carrosAtivos: 0,
    pardaisAtivos: 0,
    statusSistema: "Conectando...",
  });

  // Interpreta mensagens e organiza por categoria
  function interpretMessage(msg) {
    try {
      const data = JSON.parse(msg);
      const { agent } = data;

      if (agent?.startsWith("SEMAFORO")) {
        atualizarResumo("semaforo", data.state);

        // Atualiza o estado visual do semáforo
        setEstadosSemaforos((prev) => ({ ...prev, [agent]: data.state }));

        return {
          categoria: "semaforo",
          texto: `🚦 ${agent} ficou ${data.state}`,
          cor: data.state === "VERDE" ? "#3fb950" : "#f85149",
        };
      }

      if (agent?.startsWith("Car")) {
        const { x, y } = data.position || {};

        // Atualiza posição do carro ou remove ao sair
        if (data.event === "move" || data.event === "spawn") {
          setPosicoesCarros((prev) => ({
            ...prev,
            [agent]: { x, y, direction: data.direction },
          }));
        } else if (data.event === "exit") {
          setPosicoesCarros((prev) => {
            const novo = { ...prev };
            delete novo[agent];
            return novo;
          });
        }

        // Retorna mensagem de log conforme o evento
        if (data.event === "move") {
          return {
            categoria: "carro",
            texto: `🚗 ${agent} moveu-se para ${data.direction} (x=${x.toFixed(1)}, y=${y.toFixed(1)})`,
            cor: "#79c0ff",
          };
        }

        if (data.event === "exit") {
          atualizarResumo("carro", "exit");
          return {
            categoria: "carro",
            texto: `🚗 ${agent} saiu da simulação`,
            cor: "#79c0ff",
          };
        }

        if (data.event === "spawn") {
          atualizarResumo("carro", "spawn");
          return {
            categoria: "carro",
            texto: `🚗 ${agent} entrou na simulação (x=${x.toFixed(1)}, y=${y.toFixed(1)})`,
            cor: "#79c0ff",
          };
        }
      }

      if (agent?.startsWith("PARDAL")) {
        atualizarResumo("pardal");
        return {
          categoria: "pardal",
          texto: `📸 ${agent} detectou ${data.carCount} carro(s) (${data.event})`,
          cor: "#e3b341",
        };
      }

      return { categoria: "sistema", texto: msg, cor: "#8b949e" };
    } catch {
      let cor = "#8b949e";
      if (msg.includes("Erro")) cor = "#f85149";
      if (msg.includes("Conectado")) cor = "#3fb950";
      if (msg.includes("encerrada")) cor = "#e3b341";

      return { categoria: "sistema", texto: msg, cor };
    }
  }

  function atualizarResumo(tipo, valor) {
    setSummary((prev) => {
      const novo = { ...prev };
      if (tipo === "carro") {
        if (valor === "spawn") novo.carrosAtivos++;
        if (valor === "exit" && novo.carrosAtivos > 0) novo.carrosAtivos--;
      }
      if (tipo === "semaforo") {
        if (valor === "VERDE") novo.semaforosVerdes++;
        if (valor === "VERMELHO" && novo.semaforosVerdes > 0) novo.semaforosVerdes--;
      }
      if (tipo === "pardal") novo.pardaisAtivos++;
      return novo;
    });
  }

  // Estado adicional para monitorar semáforos fixos
  const [estadosSemaforos, setEstadosSemaforos] = useState({});

  const [posicoesCarros, setPosicoesCarros] = useState({});

  // WebSocket
  useEffect(() => {
    const ws = connectWebSocket((msg) => {
      const item = interpretMessage(msg);
      const timestamp = new Date().toLocaleTimeString();
      const novo = { ...item, timestamp };

      setLogs((prev) => ({
        ...prev,
        [item.categoria]: [...prev[item.categoria], novo],
      }));
    });

    return () => disconnectWebSocket();
  }, []);

  return (
    <div className="logs-dashboard">
      <header className="logs-header">
        <h1>Logs em Tempo Real</h1>
        <Link to="/" className="btn-back">⬅ Voltar</Link>
      </header>

      <section className="summary-bar">
        <div>📶 Sistema: {summary.statusSistema}</div>
        <div>🚦 Verdes: {summary.semaforosVerdes}</div>
        <div>🚗 Carros ativos: {summary.carrosAtivos}</div>
        <div>📸 Pardais ativos: {summary.pardaisAtivos}</div>
      </section>

      <main className="logs-grid">
        <div className="log-column">
          <h2>📶 Sistema</h2>
          {logs.sistema.map((l, i) => (
            <div key={i} style={{ color: l.cor }}>
              [{l.timestamp}] {l.texto}
            </div>
          ))}
        </div>

        <div className="log-column">
          <h2>🚦 Semáforos</h2>
          {logs.semaforo.map((l, i) => (
            <div key={i} style={{ color: l.cor }}>
              [{l.timestamp}] {l.texto}
            </div>
          ))}
        </div>

        <div className="log-column">
          <h2>🚗 Carros</h2>
          {logs.carro.map((l, i) => (
            <div key={i} style={{ color: l.cor }}>
              [{l.timestamp}] {l.texto}
            </div>
          ))}
        </div>

        <div className="log-column">
          <h2>📸 Pardais</h2>
          {logs.pardal.map((l, i) => (
            <div key={i} style={{ color: l.cor }}>
              [{l.timestamp}] {l.texto}
            </div>
          ))}
        </div>
      </main>
      {/* ============================================== */}
      {/* 🗺️ MAPA VIÁRIO DETALHADO 3x3 COM ENTRADAS/SAÍDAS */}
      {/* ============================================== */}
      <section className="traffic-map">
        <h2>🗺️ Mapa da Malha Viária (3x3)</h2>

        <div className="road-grid">
          {[
            // Y = 2
            ["", "", "SPAWN_1_2_S", "SPAWN_2_2_S", ""],
            // Y = 1
            ["", "→", "↓→", "↓", ""],
            // Y = 0
            ["SPAWN_-1_0_L", "→↑↔", "↔↓↔", "↔↓", ""],
            // Y = -1
            ["SAIDA_0_-2_O", "←↑←", "←", "←↓", ""],
            // Y = -2
            ["", "", "", "SAIDA_2_-2_S", ""],
          ].map((row, yIndex) => (
            <div key={yIndex} className="row-line">
              {row.map((cell, xIndex) => {
                const coordX = xIndex - 1; // eixo X (ajuste visual)
                const coordY = 2 - yIndex; // eixo Y (de cima pra baixo)

                // Define se tem semáforo nessa posição
                const semaforosPos = {
                  "(0,0)": ["SEMAFORO_0_0_N", "SEMAFORO_0_0_L", "SEMAFORO_0_0_O"],
                  "(1,0)": ["SEMAFORO_1_0_S", "SEMAFORO_1_0_L", "SEMAFORO_1_0_O"],
                  "(2,0)": ["SEMAFORO_2_0_S", "SEMAFORO_2_0_L"],
                };
                const key = `(${coordX},${coordY})`;
                const semaforos = semaforosPos[key] || [];

                // Conta quantos carros únicos estão nessa coordenada
                const carrosNestaCelula = Object.values(posicoesCarros).filter(
                  (car) => Math.round(car.x) === coordX && Math.round(car.y) === coordY
                );
                const quantidadeCarros = carrosNestaCelula.length;

                return (
                  <div key={xIndex} className="cell">
                    {/* Coordenadas no canto */}
                    <span className="coord">{key}</span>

                    {/* Caso seja entrada ou saída */}
                    {cell.startsWith("SPAWN") && (
                      <span className="spawn">ENTRADA</span>
                    )}
                    {cell.startsWith("SAIDA") && (
                      <span className="exit">SAÍDA</span>
                    )}

                    {/* Caso tenha semáforos */}
                    {semaforos.length > 0 && (
                      <div className="semaforos">
                        {semaforos.map((id) => {
                          // Define seta com base na direção do nome
                          let arrow = "";
                          if (id.endsWith("_N")) arrow = "↑";
                          else if (id.endsWith("_S")) arrow = "↓";
                          else if (id.endsWith("_L")) arrow = "→";
                          else if (id.endsWith("_O")) arrow = "←";

                          return (
                            <div
                              key={id}
                              className="light-dot"
                              title={`${id} (${estadosSemaforos[id] || "..."})`}
                              style={{
                                backgroundColor:
                                  estadosSemaforos[id] === "VERDE"
                                    ? "#3fb950"
                                    : estadosSemaforos[id] === "VERMELHO"
                                    ? "#f85149"
                                    : "#6e7681",
                              }}
                            >
                              {arrow}
                            </div>
                          );
                        })}
                      </div>
                    )}

                    {/* Carros presentes nesta célula */}
                    {quantidadeCarros > 0 && (
                      <div
                        className="car-dot"
                        title={`Carros nesta célula: ${carrosNestaCelula
                          .map((c) => `${c.x.toFixed(1)},${c.y.toFixed(1)} [${c.direction}]`)
                          .join(" | ")}`}
                      >
                        🚗<span className="car-count">{quantidadeCarros}</span>
                      </div>
                    )}

                    {/* Ruas com setas */}
                    {!cell.startsWith("SPAWN") &&
                      !cell.startsWith("SAIDA") &&
                      semaforos.length === 0 && (
                        <span className="road">{cell}</span>
                      )}
                  </div>
                );
              })}
            </div>
          ))}
        </div>

        <p className="map-legend">
          🟢 Entradas 🔴 Saídas 🚦 Semáforos dinâmicos 🚗 Carros ↔ Ruas com sentido
        </p>
      </section>
    </div>
  );
};

export default LogsPage;
