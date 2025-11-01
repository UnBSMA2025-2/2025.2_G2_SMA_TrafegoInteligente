import "./LogConsole.css";
import { useEffect, useRef } from "react";

export default function LogConsole({ logs }) {
  const consoleRef = useRef(null);

  // Auto-scroll para o final sempre que novos logs forem adicionados
  useEffect(() => {
    if (consoleRef.current) {
      consoleRef.current.scrollTop = consoleRef.current.scrollHeight;
    }
  }, [logs]);

  return (
    <div className="log-console">
      <h3>🧾 Log do Sistema</h3>
      <div className="log-container" ref={consoleRef}>
        {logs.length === 0 ? (
          <div className="empty">Nenhum log ainda.</div>
        ) : (
          logs.map((log, i) => (
            <div key={i} className="log-entry">
              • {log}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
