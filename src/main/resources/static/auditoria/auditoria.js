/* Auditoria del hospital (A-5; RF-13; RN-06, RN-07, RNF-08). Solo ADMIN_HOSPITAL: el servidor responde 403 a otros roles
   y solo devuelve registros de su hospital. Filtros y paginacion se aplican en el servidor (GET /api/auditoria). */
const $ = (id) => document.getElementById(id);
const TAMANO = 15;
let pagina = 0;

function consulta() {
  const p = new URLSearchParams({ pagina: String(pagina), tamano: String(TAMANO) });
  ['usuario', 'accion', 'nroHc', 'desde', 'hasta'].forEach(c => { const v = $(c).value.trim(); if (v) p.set(c, v); });
  return p.toString();
}

async function cargar() {
  const r = await HCE.api('/api/auditoria?' + consulta());
  if (!r.ok) { HCE.mostrar($('error'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo cargar la auditoría.', 'error'); return; }
  $('error').className = 'hce-mensaje';
  const d = r.cuerpo;
  const cuerpo = $('cuerpo');
  cuerpo.replaceChildren();
  d.registros.forEach(x => {
    const tr = document.createElement('tr');
    [HCE.fechaHora(x.fechaHora), x.usuario, x.rol, x.accion, x.nroHc || '', x.ip].forEach(t => tr.append(HCE.el('td', null, t)));
    cuerpo.append(tr);
  });
  $('total').textContent = d.total;
  $('vacio').hidden = d.total > 0;
  $('rango').textContent = d.total === 0 ? '' : 'Página ' + (d.pagina + 1) + ' de ' + d.totalPaginas;
  $('anterior').disabled = d.pagina === 0;
  $('siguiente').disabled = d.pagina + 1 >= d.totalPaginas;
}

$('filtros').addEventListener('submit', (e) => { e.preventDefault(); pagina = 0; cargar(); });
$('limpiar').addEventListener('click', () => { $('filtros').reset(); pagina = 0; cargar(); });
$('anterior').addEventListener('click', () => { pagina--; cargar(); });
$('siguiente').addEventListener('click', () => { pagina++; cargar(); });

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  const acciones = await HCE.api('/api/auditoria/acciones');
  if (acciones.ok) {
    acciones.cuerpo.forEach(a => { const o = document.createElement('option'); o.value = a; o.textContent = a; $('accion').append(o); });
  }
  await cargar();
})();
