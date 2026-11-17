/* Inicio por rol (A-5; RF-19, RN-06). Las cifras vienen de GET /api/indicadores/inicio (base de datos real,
   filtradas por el hospital del usuario). Si el endpoint falla, no se muestra ninguna tarjeta. */
(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  // El administrador del hospital ve ademas el tablero con graficos (A-10, DEC-10). El servidor valida el rol (403).
  if (sesion.rolCodigo === 'ADMIN_HOSPITAL') TableroAdmin.dibujar();
  const r = await HCE.api('/api/indicadores/inicio');
  const contenedor = document.getElementById('tarjetas');
  if (!r.ok || !r.cuerpo || !r.cuerpo.tarjetas || r.cuerpo.tarjetas.length === 0) {
    document.getElementById('vacio').hidden = false;
    return;
  }
  document.getElementById('subtitulo').textContent = 'Resumen de ' + r.cuerpo.establecimiento + '.';
  r.cuerpo.tarjetas.forEach(t => {
    const tarjeta = HCE.el('article', 'hce-indicador');
    tarjeta.append(HCE.el('div', 'titulo', t.titulo), HCE.el('div', 'valor', String(t.valor)), HCE.el('div', 'nota', t.nota));
    contenedor.append(tarjeta);
  });
})();
