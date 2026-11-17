/* P-02 Pacientes: lista del hospital con buscador y paginacion (A-2; RF-02, RN-06).
   El filtro se hace en el navegador sobre la lista que el servidor ya limito al hospital del usuario. */
const $ = (id) => document.getElementById(id);
const POR_PAGINA = 10;
let todas = [];
let pagina = 0;
let puedeVerHistoria = false;

function normalizar(texto) {
  return String(texto).normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
}

function filtradas() {
  const q = normalizar($('buscar').value.trim());
  if (!q) return todas;
  return todas.filter(h => normalizar([h.nroDocumento, h.nroHc, h.paciente].join(' ')).includes(q));
}

function dibujar() {
  const lista = filtradas();
  const paginas = Math.max(1, Math.ceil(lista.length / POR_PAGINA));
  if (pagina >= paginas) pagina = paginas - 1;
  const visibles = lista.slice(pagina * POR_PAGINA, (pagina + 1) * POR_PAGINA);
  const cuerpo = $('cuerpo');
  cuerpo.replaceChildren();
  visibles.forEach(h => {
    const tr = document.createElement('tr');
    [h.nroHc, h.tipoDocumento + ' ' + h.nroDocumento, h.paciente, h.fechaApertura]
      .forEach(t => tr.appendChild(HCE.el('td', null, t)));
    const estado = HCE.el('td');
    estado.append(HCE.el('span', 'hce-chip ' + (h.estado === 'ABIERTA' ? 'ok' : 'alerta'), h.estado));
    tr.appendChild(estado);
    const accion = HCE.el('td');
    if (puedeVerHistoria) {
      const enlace = HCE.el('a', null, 'Ver historia');
      enlace.href = '/historia/?id=' + encodeURIComponent(h.idHistoriaClinica);
      accion.append(enlace);
    }
    tr.appendChild(accion);
    cuerpo.appendChild(tr);
  });
  $('cuenta').textContent = lista.length;
  const vacio = $('vacio');
  vacio.hidden = lista.length > 0;
  vacio.textContent = todas.length === 0
    ? 'Aún no hay historias abiertas en tu hospital.'
    : 'No hay pacientes que coincidan con la búsqueda.';
  $('rango').textContent = lista.length === 0 ? '' :
    'Mostrando ' + (pagina * POR_PAGINA + 1) + ' a ' + (pagina * POR_PAGINA + visibles.length) + ' de ' + lista.length;
  $('anterior').disabled = pagina === 0;
  $('siguiente').disabled = pagina >= paginas - 1;
}

$('buscar').addEventListener('input', () => { pagina = 0; dibujar(); });
$('anterior').addEventListener('click', () => { pagina--; dibujar(); });
$('siguiente').addEventListener('click', () => { pagina++; dibujar(); });

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  puedeVerHistoria = HCE.tieneOpcion(sesion, 'historia');
  $('nuevoPaciente').hidden = !HCE.tieneOpcion(sesion, 'registrar-paciente');
  const r = await HCE.api('/api/historias');
  if (r.ok) todas = r.cuerpo;
  dibujar();
})();
