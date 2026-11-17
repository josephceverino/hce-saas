/* P-02 Pacientes con admision del dia (RF-02, RF-21; CU-20; RN-06, RN-13). Referencia 05.
   La lista ya viene limitada al hospital del usuario. Admitir (P-10) es un dialogo dentro de esta pantalla:
   el servidor decide si se puede (rol, hospital, una admision activa por dia). */
const $ = (id) => document.getElementById(id);
const POR_PAGINA = 10;
const TIPOS = { CONSULTA_EXTERNA: 'Consulta externa', EMERGENCIA: 'Emergencia' };
let todas = [];
let admisionHoy = {}; // idHistoria -> ultima admision de hoy
let pagina = 0;
let filtro = 'todos';
let puedeVerHistoria = false;
let puedeAdmitir = false;

const FILTROS = [
  { clave: 'todos', texto: 'Todos', prueba: () => true },
  { clave: 'hoy', texto: 'Admitidos hoy', prueba: h => !!admisionHoy[h.idHistoriaClinica] },
  { clave: 'sin', texto: 'Sin admitir hoy', prueba: h => !admisionHoy[h.idHistoriaClinica] }
];

function normalizar(t) { return String(t || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase(); }

function activa(a) { return a && a.estado !== 'ATENCION_CERRADA' && a.estado !== 'CANCELADO'; }

function dibujarPestanas() {
  const p = $('pestanas');
  p.replaceChildren();
  FILTROS.forEach(f => {
    const b = HCE.el('button', 'hce-pestana' + (f.clave === filtro ? ' activa' : ''));
    b.type = 'button';
    b.append(document.createTextNode(f.texto + ' '), HCE.el('span', 'cuenta', String(todas.filter(f.prueba).length)));
    b.addEventListener('click', () => { filtro = f.clave; pagina = 0; dibujarPestanas(); dibujar(); });
    p.append(b);
  });
}

async function admitir(h) {
  const tipo = await HCE.elegir('Admitir a ' + h.paciente, 'Elige el tipo de atención. El paciente pasará a la cola de enfermería.', [
    { valor: 'CONSULTA_EXTERNA', texto: 'Consulta externa', descripcion: 'Atención ambulatoria en orden de llegada' },
    { valor: 'EMERGENCIA', texto: 'Emergencia', descripcion: 'Se atiende primero en la cola', clase: 'emergencia' }
  ]);
  if (!tipo) return;
  const r = await HCE.api('/api/admisiones', { method: 'POST', cuerpo: { idHistoriaClinica: h.idHistoriaClinica, tipoAtencion: tipo } });
  if (r.status === 201) {
    HCE.mostrar($('mensaje'), h.paciente + ' fue admitido por ' + TIPOS[tipo].toLowerCase() + ' y está en la cola de enfermería.', 'ok');
    await cargarAdmisiones();
    dibujarPestanas();
    dibujar();
  } else {
    HCE.mostrar($('mensaje'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo admitir al paciente.', 'error');
  }
}

function dibujar() {
  const f = FILTROS.find(x => x.clave === filtro);
  const q = normalizar($('buscar').value.trim());
  const lista = todas.filter(f.prueba).filter(h => !q || normalizar([h.nroDocumento, h.nroHc, h.paciente].join(' ')).includes(q));
  const paginas = Math.max(1, Math.ceil(lista.length / POR_PAGINA));
  if (pagina >= paginas) pagina = paginas - 1;
  const visibles = lista.slice(pagina * POR_PAGINA, (pagina + 1) * POR_PAGINA);
  const cuerpo = $('cuerpo');
  cuerpo.replaceChildren();
  visibles.forEach(h => {
    const tr = document.createElement('tr');
    const persona = HCE.el('td');
    const caja = HCE.el('div', 'hce-persona');
    const partes = h.paciente.split(',');
    const texto = HCE.el('div');
    texto.append(HCE.el('div', 'nombre', h.paciente), HCE.el('div', 'nota', h.tipoDocumento + ' ' + h.nroDocumento));
    caja.append(HCE.el('span', 'hce-avatar-mini', HCE.iniciales(partes[1], partes[0])), texto);
    persona.append(caja);
    const hoy = HCE.el('td');
    const a = admisionHoy[h.idHistoriaClinica];
    if (a) {
      if (a.tipoAtencion === 'EMERGENCIA') hoy.append(HCE.el('span', 'hce-chip emergencia', 'Emergencia'), document.createTextNode(' '));
      hoy.append(HCE.el('span', 'hce-chip estado-' + a.estado, a.estadoDescripcion));
    } else {
      hoy.append(HCE.el('span', 'hce-nota-celda', puedeAdmitir ? 'Sin admitir' : 'Ver en la cola'));
    }
    const acciones = HCE.el('td', 'derecha');
    const grupo = HCE.el('div', 'hce-acciones-fila');
    if (puedeAdmitir && !activa(a)) {
      const b = HCE.el('button', 'hce-boton primario pequeno', 'Admitir');
      b.type = 'button';
      b.addEventListener('click', () => admitir(h));
      grupo.append(b);
    }
    if (puedeVerHistoria) {
      const enlace = HCE.el('a', 'hce-boton claro pequeno', 'Ver historia');
      enlace.href = '/historia/?id=' + encodeURIComponent(h.idHistoriaClinica);
      grupo.append(enlace);
    }
    acciones.append(grupo);
    tr.append(persona, HCE.el('td', 'hce-monoespacio-celda', h.nroHc), HCE.el('td', null, h.fechaApertura), hoy, acciones);
    cuerpo.append(tr);
  });
  const vacio = $('vacio');
  vacio.hidden = lista.length > 0;
  vacio.textContent = todas.length === 0 ? 'Aún no hay pacientes registrados en tu hospital.' : 'No hay pacientes que coincidan.';
  $('rango').textContent = lista.length ? 'Mostrando ' + (pagina * POR_PAGINA + 1) + ' a ' + (pagina * POR_PAGINA + visibles.length) + ' de ' + lista.length : '';
  $('anterior').disabled = pagina === 0;
  $('siguiente').disabled = pagina >= paginas - 1;
}

async function cargarAdmisiones() {
  admisionHoy = {};
  const r = await HCE.api('/api/admisiones/cola');
  if (!r.ok) return;
  // la mas reciente de hoy por historia
  r.cuerpo.slice().sort((x, y) => x.fechaHoraAdmision.localeCompare(y.fechaHoraAdmision))
    .forEach(a => { admisionHoy[a.idHistoriaClinica] = a; });
}

$('buscar').addEventListener('input', () => { pagina = 0; dibujar(); });
$('anterior').addEventListener('click', () => { pagina--; dibujar(); });
$('siguiente').addEventListener('click', () => { pagina++; dibujar(); });

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  puedeVerHistoria = HCE.tieneOpcion(sesion, 'historia');
  puedeAdmitir = HCE.tieneOpcion(sesion, 'registrar-paciente'); // admision: el servidor igual valida el permiso
  $('nuevoPaciente').hidden = !puedeAdmitir;
  const r = await HCE.api('/api/historias');
  if (r.ok) todas = r.cuerpo;
  if (puedeAdmitir) await cargarAdmisiones();
  if (puedeAdmitir) dibujarPestanas(); else $('pestanas').hidden = true;
  dibujar();
})();
