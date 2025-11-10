// src/pages/LogsPage.jsx
import React, { useEffect, useState } from "react";
import { connectWebSocket, disconnectWebSocket } from "../services/socket";
import { Link } from "react-router-dom";
import "./LogsPage.css";

const LogsPage = () => {
  const [logs, setLogs] = useState([]);

  useEffect(() => {
    const ws = connectWebSocket((msg) => {
      setLogs((prev) => [
        ...prev,
        `[${new Date().toLocaleTimeString()}] ${msg}`,
      ]);
    });

    return () => disconnectWebSocket();
  }, []);

  return (
    <div className="logs-page">
      <header className="logs-header">
        <h1>Logs em Tempo Real</h1>
        <Link to="/" className="btn-back">⬅ Voltar</Link>
      </header>

      <div className="logs-container">
        {logs.length > 0 ? (
          <pre>{logs.join("\n")}</pre>
        ) : (
          <pre>Aguardando mensagens do servidor...</pre>
        )}
      </div>
    </div>
  );
};

export default LogsPage;
