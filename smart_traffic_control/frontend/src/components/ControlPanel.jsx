import { api } from "../services/api";
import "./ControlPanel.css";

export default function ControlPanel({ fetchCars, setLogs }) {
  // ➕ Adiciona carros
  const addCars = async () => {
    const n = prompt("Quantos carros deseja adicionar?");
    if (!n) return;
    try {
      const res = await api.post(`/add/${n}`);
      setLogs((prev) => [...prev, res.data]);
      fetchCars();
    } catch (err) {
      setLogs((prev) => [...prev, "Erro ao adicionar carros"]);
    }
  };

  // 🗑️ Remove carro
  const removeCar = async () => {
    const name = prompt("Nome do carro a remover (ex: Car1)");
    if (!name) return;
    try {
      const res = await api.delete(`/remove/${name}`);
      setLogs((prev) => [...prev, res.data]);
      fetchCars();
    } catch (err) {
      setLogs((prev) => [...prev, "Erro ao remover carro"]);
    }
  };

  return (
    <div className="control-panel">
      <button onClick={addCars} className="btn btn-add">
        ➕ Adicionar Carros
      </button>
      <button onClick={removeCar} className="btn btn-remove">
        🗑️ Remover Carro
      </button>
      <button onClick={fetchCars} className="btn btn-refresh">
        🔄 Atualizar Lista
      </button>
    </div>
  );
}
