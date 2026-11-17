/* P-09 Cola de pacientes del dia (RF-22; CU-20, CU-21; RN-13, RN-06). Referencia 05.
   El servidor decide que estados ve cada rol y el orden (emergencia primero, luego por llegada).
   Las acciones solo llaman al servidor; el orden de los estados lo impone el dominio (RN-13). */
const $ = (id) => document.getElementById(id);
const ESTADOS = {
  ADMITIDO: 'Admitidos', EN_ENFERMERIA: 'En enfermería', LISTO_PARA_ATENCION: 'Listos para atención',
  EN_ATENCION: 'En atención', ATENCION_CERRADA: 'Atendidos', CANCELADO: 'Cancelados'
};
const POR_ROL = {
  ADMISION: { titulo: 'Pacientes del día', subtitulo: 'Todos los admitidos hoy y en qué paso van.',
    estados: ['ADMITIDO', 'EN_ENFERMERIA', 'LISTO_PARA_ATENCION', 'EN_ATENCION', 'ATENCION_CERRADA'] },
  ENFERMERIA: { titulo: 'Cola de enfermería', subtitulo: 'Toma al siguiente paciente y registra sus signos vitales. La emergencia va primero.',
    estados: ['ADMITIDO', 'EN_ENFERMERIA'] },
  MEDICO: { titulo: 'Cola de atención', subtitulo: 'Pacientes listos para ser atendidos. La emergencia va primero.',
    estados: ['LISTO_PARA_ATENCION', 'EN_ATENCION'] }
};
let sesion = null;
let cola = [];
let filtro = 'todos';

function normalizar(t) { return String(t || '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase(); }

function minutosDesde(fecha) {
  const m = Math.max(0, Math.round((Date.now() - new Date(fecha).getTime()) / 60000));
  if (m < 60) return m + ' min';
  return Math.floor(m / 60) + ' h ' + (m % 60) + ' min';
}

function hora(fecha) {
  return new Date(fecha).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' });
}

async function accion(ruta, mensajeOk, despues) {
  const r = await HCE.api(ruta, { method: 'POST' });
  if (!r.ok) { HCE.mostrar($('mensaje'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo completar la acción.', 'error'); return; }
  HCE.mostrar($('mensaje'), mensajeOk, 'ok');
  if (despues) { despues(r.cuerpo); return; }
  await cargar();
}

function boton(texto, clase, alHacerClic) {
  const b = HCE.el('button', 'hce-boton pequeno ' + clase, texto);
  b.type = 'button';
  b.addEventListener('click', alHacerClic);
  return b;
}

function enlace(texto, clase, href) {
  const a = HCE.el('a', 'hce-boton pequeno ' + clase, texto);
  a.href = href;
  return a;
}

function accionesDe(a) {
  const g = HCE.el('div', 'hce-acciones-fila');
  const historia = '/historia/?id=' + encodeURIComponent(a.idHistoriaClinica) + '&admision=' + encodeURIComponent(a.id);
  const rol = sesion.rolCodigo;
  if (rol === 'ENFERMERIA' && a.estado === 'ADMITIDO') {
    g.append(boton('Tomar paciente', 'primario', () => accion('/api/admisiones/' + a.id + '/toma',
      a.paciente + ' pasó a enfermería.', () => { window.location.href = historia; })));
  } else if (rol === 'ENFERMERIA' && a.estado === 'EN_ENFERMERIA') {
    g.append(enlace('Signos vitales', 'primario', historia));
  } else if (rol === 'MEDICO' && a.estado === 'LISTO_PARA_ATENCION') {
    g.append(enlace('Iniciar atención', 'primario', '/atencion/?admision=' + encodeURIComponent(a.id)),
      enlace('Ver historia', 'claro', historia));
  } else if (rol === 'MEDICO' && a.estado === 'EN_ATENCION') {
    g.append(enlace('Continuar atención', 'primario', '/atencion/?admision=' + encodeURIComponent(a.id)),
      enlace('Ver historia', 'claro', historia));
  } else if (rol === 'ADMISION' && a.estado === 'ADMITIDO') {
    g.append(boton('Cancelar', 'claro', async () => {
      const ok = await HCE.confirmar('¿Cancelar la admisión de ' + a.paciente + '?',
        'Solo se puede cancelar antes de que enfermería lo tome. Queda registrado.', 'Sí, cancelar');
      if (ok) accion('/api/admisiones/' + a.id + '/cancelacion', 'Se canceló la admisión de ' + a.paciente + '.');
    }));
  }
  return g;
}

function dibujarCifras(config) {
  const cont = $('cifras');
  cont.replaceChildren();
  const clases = { ADMITIDO: '', EN_ENFERMERIA: 'alerta', LISTO_PARA_ATENCION: 'ok', EN_ATENCION: '', ATENCION_CERRADA: '' };
  config.estados.forEach(e => {
    const t = HCE.el('article', 'hce-cifra-estado ' + (clases[e] || ''));
    t.append(HCE.el('div', 'titulo', ESTADOS[e]), HCE.el('div', 'valor', String(cola.filter(a => a.estado === e).length)),
      HCE.el('div', 'nota', e === 'ADMITIDO' ? 'Esperan a enfermería' : e === 'LISTO_PARA_ATENCION' ? 'Esperan al médico' : 'Hoy'));
    cont.append(t);
  });
  const emergencias = cola.filter(a => a.tipoAtencion === 'EMERGENCIA' && a.estado !== 'ATENCION_CERRADA' && a.estado !== 'CANCELADO').length;
  const t = HCE.el('article', 'hce-cifra-estado error');
  t.append(HCE.el('div', 'titulo', 'Emergencias'), HCE.el('div', 'valor', String(emergencias)), HCE.el('div', 'nota', 'En espera o en atención'));
  cont.append(t);
  const p = $('pestanas');
  p.replaceChildren();
  [{ clave: 'todos', texto: 'Todos' }].concat(config.estados.map(e => ({ clave: e, texto: ESTADOS[e] }))).forEach(f => {
    const b = HCE.el('button', 'hce-pestana' + (f.clave === filtro ? ' activa' : ''));
    b.type = 'button';
    const n = f.clave === 'todos' ? cola.length : cola.filter(a => a.estado === f.clave).length;
    b.append(document.createTextNode(f.texto + ' '), HCE.el('span', 'cuenta', String(n)));
    b.addEventListener('click', () => { filtro = f.clave; dibujarCifras(config); dibujarTabla(); });
    p.append(b);
  });
}

function dibujarTabla() {
  const q = normalizar($('buscar').value.trim());
  const lista = cola.filter(a => filtro === 'todos' || a.estado === filtro)
    .filter(a => !q || normalizar([a.paciente, a.nroDocumento, a.nroHc].join(' ')).includes(q));
  const cuerpo = $('cuerpo');
  cuerpo.replaceChildren();
  lista.forEach((a, i) => {
    const tr = document.createElement('tr');
    if (a.tipoAtencion === 'EMERGENCIA') tr.className = 'hce-fila-emergencia';
    const persona = HCE.el('td');
    const caja = HCE.el('div', 'hce-persona');
    const partes = a.paciente.split(',');
    const texto = HCE.el('div');
    const edad = HCE.edad(a.fechaNacimiento);
    texto.append(HCE.el('div', 'nombre', a.paciente),
      HCE.el('div', 'nota', a.nroHc + ' · ' + edad + ' años · ' + (a.sexo === 'F' ? 'Femenino' : 'Masculino')));
    caja.append(HCE.el('span', 'hce-avatar-mini', HCE.iniciales(partes[1], partes[0])), texto);
    persona.append(caja);
    const tipo = HCE.el('td');
    tipo.append(HCE.el('span', 'hce-chip ' + (a.tipoAtencion === 'EMERGENCIA' ? 'emergencia' : 'info'),
      a.tipoAtencion === 'EMERGENCIA' ? 'Emergencia' : 'Consulta externa'));
    const estado = HCE.el('td');
    estado.append(HCE.el('span', 'hce-chip estado-' + a.estado, a.estadoDescripcion));
    const acciones = HCE.el('td', 'derecha');
    acciones.append(accionesDe(a));
    const activo = a.estado !== 'ATENCION_CERRADA' && a.estado !== 'CANCELADO';
    tr.append(HCE.el('td', 'hce-orden', String(i + 1)), persona, tipo, estado, HCE.el('td', 'hce-nowrap', hora(a.fechaHoraAdmision)),
      HCE.el('td', 'hce-nowrap', activo ? minutosDesde(a.fechaHoraEstado) : '·'), acciones);
    cuerpo.append(tr);
  });
  const vacio = $('vacio');
  vacio.hidden = lista.length > 0;
  vacio.textContent = cola.length === 0
    ? (sesion.rolCodigo === 'ADMISION' ? 'Aún no hay pacientes admitidos hoy. Admítelos desde Pacientes.'
      : 'No hay pacientes esperando. Cuando admisión admita a alguien, aparecerá aquí.')
    : 'No hay pacientes que coincidan.';
}

async function cargar() {
  const r = await HCE.api('/api/admisiones/cola');
  if (!r.ok) { HCE.mostrar($('mensaje'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo cargar la cola.', 'error'); return; }
  cola = r.cuerpo;
  const config = POR_ROL[sesion.rolCodigo] || POR_ROL.ADMISION;
  dibujarCifras(config);
  dibujarTabla();
  $('actualizado').textContent = 'Actualizado ' + hora(new Date());
}

$('buscar').addEventListener('input', dibujarTabla);
$('actualizar').addEventListener('click', cargar);

(async () => {
  sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  const config = POR_ROL[sesion.rolCodigo] || POR_ROL.ADMISION;
  $('titulo').textContent = config.titulo;
  $('subtitulo').textContent = config.subtitulo;
  await cargar();
  setInterval(cargar, 30000); // la cola cambia sola: se refresca cada 30 segundos
})();
