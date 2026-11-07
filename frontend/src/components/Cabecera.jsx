export default function Cabecera({ ancha = false, children }) {
  return (
    <header className={ancha ? 'cabecera cabecera-ancha' : 'cabecera'}>
      <a className="marca" href="#/">Control de gastos</a>
      {children}
    </header>
  );
}
