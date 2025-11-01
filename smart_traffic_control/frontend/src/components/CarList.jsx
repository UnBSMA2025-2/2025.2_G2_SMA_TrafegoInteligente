import "./CarList.css";

export default function CarList({ cars }) {
  return (
    <div className="car-list">
      <h2>🚗 Carros Ativos</h2>

      {cars.length === 0 ? (
        <p className="empty">Nenhum carro ativo.</p>
      ) : (
        <div className="car-grid">
          {cars.map((car) => (
            <div key={car} className="car-card">
              {car}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
