/* Panel Hospitales del administrador de plataforma (A-11; DEC-11; RF-20). Solo totales por hospital de
   GET /api/indicadores/hospitales: el servidor no entrega datos clinicos ni identificadores de pacientes. */
const $ = (id) => document.getElementById(id);

function cifra(titulo, valor) {
  const c = HCE.el('div', 'cifra');
  c.append(HCE.el('div', 'valor', String(valor)), HCE.el('div', 'etiqueta', titulo));
  return c;
}

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  const r = await HCE.api('/api/indicadores/hospitales');
  if (!r.ok) { HCE.mostrar($('error'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo cargar el panel.', 'error'); return; }
  $('vacio').hidden = r.cuerpo.length > 0;
  r.cuerpo.forEach(h => {
    const tarjeta = HCE.el('article', 'hce-tarjeta hce-hospital-tarjeta');
    const cab = HCE.el('div', 'hce-evento-cabecera');
    const titulo = HCE.el('div');
    titulo.append(HCE.el('h2', null, h.nombre), HCE.el('div', 'hce-subtitulo', 'IPRESS ' + h.codigoIpress + ' · ' + h.region));
    cab.append(titulo, HCE.el('span', 'hce-chip ' + (h.estado === 'ACTIVO' ? 'ok' : 'error'), h.estado));
    const cifras = HCE.el('div', 'cifras');
    cifras.append(cifra('Usuarios', h.usuarios), cifra('Pacientes', h.pacientes), cifra('Atenciones', h.atenciones));
    tarjeta.append(cab, cifras);
    $('lista').append(tarjeta);
  });
})();
