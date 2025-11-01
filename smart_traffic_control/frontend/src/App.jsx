import { useEffect, useState } from "react";
import { api } from "./services/api";
import CarList from "./components/CarList";
import ControlPanel from "./components/ControlPanel";
import LogConsole from "./components/LogConsole";

import "./App.css";

export default function App() {
  const [cars, setCars] = useState([]);
  const [logs, setLogs] = useState([]);

  // 🔄 Buscar lista de carros
  const fetchCars = async () => {
    try {
      const response = await api.get("/list");
      const text = response.data;

      const cleanList = text
        .replace(/\[|\]|\s/g, "")
        .split(",")
        .filter(Boolean);

      setCars(cleanList);
      setLogs((prev) => [...prev, "Lista atualizada com sucesso"]);
    } catch (error) {
      console.error("Erro ao buscar carros:", error);
      setLogs((prev) => [...prev, "Erro ao buscar carros"]);
    }
  };

  useEffect(() => {
    fetchCars();
  }, []);

  return (
    <div className="app-container">
      <h1>Smart Traffic Control</h1>
      <ControlPanel fetchCars={fetchCars} setLogs={setLogs} />
      <CarList cars={cars} />
      <LogConsole logs={logs} />
    </div>
  );
}
