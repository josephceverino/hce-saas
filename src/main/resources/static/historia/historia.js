/* P-04 Historia clinica (CU-06, CU-21; RF-06, RF-07, RF-23; RN-03, RN-06, RN-07, RN-17). Referencia 02.
   Cabecera del paciente con cifras, filiacion, antecedentes por tipo, alergias, signos vitales y linea de tiempo.
   Si se llega desde la cola (?admision=), enfermeria registra aqui los signos vitales de la admision de hoy.
   El servidor decide quien ve y registra que: si la historia es de otro hospital responde 403 y solo se muestra el mensaje. */
const $ = (id) => document.getElementById(id);
const params = new URLSearchParams(window.location.search);
const idHistoria = params.get('id');
const idAdmision = params.get('admision');
const TIPOS = { PERSONAL: 'Personal', QUIRURGICO: 'Quirúrgico', VACUNA: 'Vacunas', HABITO: 'Hábitos', FAMILIAR: 'Familiar' };
const ESTADO_CIVIL = { SOLTERO: 'Soltero o soltera', CASADO: 'Casado o casada', CONVIVIENTE: 'Conviviente',
  DIVORCIADO: 'Divorciado o divorciada', VIUDO: 'Viudo o viuda' };
const INSTRUCCION = { SIN_INSTRUCCION: 'Sin instrucción', INICIAL: 'Inicial', PRIMARIA: 'Primaria', SECUNDARIA: 'Secundaria',
  SUPERIOR_TECNICA: 'Superior técnica', SUPERIOR_UNIVERSITARIA: 'Superior universitaria' };
let sesion = null;
let historia = null;

function dato(etiqueta, valor) {
  const d = HCE.el('div', 'hce-dato-caja');
  d.append(HCE.el('div', 'etiqueta', etiqueta), HCE.el('div', 'valor', valor || 'Sin registrar'));
  return d;
}

function mini(titulo, valor, nota) {
  const t = HCE.el('div', 'hce-mini-cifra');
  t.append(HCE.el('div', 'titulo', titulo), HCE.el('div', 'valor', valor), HCE.el('div', 'nota', nota || ''));
  return t;
}

function dibujarFiliacion(p) {
  $('filiacion').replaceChildren(
    dato('Documento', p.tipoDocumento + ' ' + p.nroDocumento),
    dato('Fecha y lugar de nacimiento', p.fechaNacimiento + (p.lugarNacimiento ? ' · ' + p.lugarNacimiento : '')),
    dato('Estado civil', ESTADO_CIVIL[p.estadoCivil] || p.estadoCivil),
    dato('Grado de instrucción', INSTRUCCION[p.gradoInstruccion] || p.gradoInstruccion),
    dato('Ocupación', p.ocupacion),
    dato('Religión', p.religion),
    dato('Domicilio', p.direccion),
    dato('Procedencia', p.procedencia),
    dato('Teléfono', p.telefono),
    dato('Contacto de emergencia', p.contactoEmergenciaNombre
      ? p.contactoEmergenciaNombre + ' (' + p.contactoEmergenciaParentesco + ') · ' + p.contactoEmergenciaTelefono : null)
  );
}

function dibujarAntecedentes(lista) {
  const cont = $('antecedentes');
  cont.replaceChildren();
  if (lista.length === 0) { cont.append(HCE.el('div', 'hce-vacio', 'Sin antecedentes registrados.')); return; }
  const grupos = {};
  lista.forEach(a => { const t = String(a.tipo).toUpperCase(); (grupos[t] = grupos[t] || []).push(a); });
  Object.keys(TIPOS).concat(Object.keys(grupos).filter(t => !TIPOS[t])).forEach(t => {
    if (!grupos[t]) return;
    const bloque = HCE.el('div', 'hce-grupo-antecedente');
    bloque.append(HCE.el('span', 'hce-chip', TIPOS[t] || t));
    const ul = HCE.el('ul', 'hce-lista');
    grupos[t].forEach(a => ul.append(HCE.el('li', null, a.descripcion + ' (' + a.fechaRegistro + ')')));
    bloque.append(ul);
    cont.append(bloque);
  });
}

function dibujarAlergias(lista) {
  const cont = $('listaAlergias');
  cont.replaceChildren();
  if (lista.length === 0) { cont.append(HCE.el('span', 'hce-chip ok', 'Sin alergias registradas')); return; }
  lista.forEach(a => {
    const fila = HCE.el('div', 'hce-alergia');
    fila.append(HCE.el('span', 'hce-chip error', a.agente), HCE.el('span', null, a.reaccion),
      HCE.el('span', 'hce-nota-celda', 'Gravedad ' + String(a.gravedad).toLowerCase()));
    cont.append(fila);
  });
}

function dibujarSignos(lista) {
  const ultimo = lista[0];
  const caja = $('signosUltimos');
  caja.replaceChildren();
  if (!ultimo) {
    caja.append(HCE.el('div', 'hce-vacio', 'Aún no hay signos vitales registrados.'));
    $('tablaSignos').hidden = true;
    return;
  }
  caja.append(
    mini('Presión arterial', ultimo.presionSistolica + '/' + ultimo.presionDiastolica, 'milímetros de mercurio'),
    mini('Frecuencia cardíaca', String(ultimo.frecuenciaCardiaca), 'latidos por minuto'),
    mini('Frecuencia respiratoria', String(ultimo.frecuenciaRespiratoria), 'respiraciones por minuto'),
    mini('Temperatura', String(ultimo.temperatura), 'grados Celsius'),
    mini('Saturación de oxígeno', ultimo.saturacionOxigeno + ' %', 'Tomado el ' + HCE.fechaHora(ultimo.fechaHora)));
  const cuerpo = $('signosHistorial');
  cuerpo.replaceChildren();
  lista.forEach(s => {
    const tr = document.createElement('tr');
    [HCE.fechaHora(s.fechaHora), s.presionSistolica + '/' + s.presionDiastolica, s.frecuenciaCardiaca,
      s.frecuenciaRespiratoria, s.temperatura, s.saturacionOxigeno + ' %'].forEach(v => tr.append(HCE.el('td', 'hce-nowrap', String(v))));
    cuerpo.append(tr);
  });
  $('tablaSignos').hidden = false;
}

function dibujarMiniCifras(h, signos) {
  const ultima = h.atenciones[0];
  const s = signos[0];
  $('miniCifras').replaceChildren(
    mini('Atenciones', String(h.atenciones.length), 'En esta historia'),
    mini('Última atención', ultima ? HCE.fechaHora(ultima.fechaHora) : 'Ninguna', ultima ? ultima.motivo : ''),
    mini('Alergias', String(h.alergias.length), h.alergias.length ? h.alergias.map(a => a.agente).join(', ') : 'Sin alergias'),
    mini('Última presión arterial', s ? s.presionSistolica + '/' + s.presionDiastolica : 'Sin registro',
      s ? HCE.fechaHora(s.fechaHora) : ''));
}

async function cargarSignos() {
  const r = await HCE.api('/api/historias/' + encodeURIComponent(idHistoria) + '/signos-vitales');
  return r.ok ? r.cuerpo : [];
}

async function cargarTodo() {
  const r = await HCE.api('/api/historias/' + encodeURIComponent(idHistoria));
  if (!r.ok) {
    HCE.mostrar($('error'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo abrir la historia clínica.', 'error');
    return false;
  }
  historia = r.cuerpo;
  const signos = await cargarSignos();
  const puedeAtender = HCE.tieneOpcion(sesion, 'atencion');
  VistaHistoria.cabecera(historia, puedeAtender ? '/atencion/?historia=' + encodeURIComponent(historia.idHistoriaClinica) : null);
  $('antetitulo').textContent = 'Historia clínica ' + historia.nroHc;
  dibujarMiniCifras(historia, signos);
  dibujarFiliacion(historia.paciente);
  dibujarAntecedentes(historia.antecedentes);
  dibujarAlergias(historia.alergias);
  dibujarSignos(signos);
  VistaHistoria.atenciones(historia, a => puedeAtender
    ? { texto: a.cerrada ? 'Agregar enmienda' : 'Continuar atención', href: '/atencion/?id=' + encodeURIComponent(a.id) }
    : null);
  $('cuerpoHistoria').hidden = false;
  return true;
}

/* ---- CU-21: enfermeria con la admision de hoy ---- */
async function dibujarEnfermeria() {
  if (!idAdmision || sesion.rolCodigo !== 'ENFERMERIA') return;
  const r = await HCE.api('/api/admisiones/' + encodeURIComponent(idAdmision));
  if (!r.ok) return;
  const a = r.cuerpo;
  const estado = $('enfermeriaEstado');
  estado.replaceChildren();
  if (a.tipoAtencion === 'EMERGENCIA') estado.append(HCE.el('span', 'hce-chip emergencia', 'Emergencia'));
  estado.append(HCE.el('span', 'hce-chip estado-' + a.estado, a.estadoDescripcion));
  $('enfermeriaTitulo').textContent = 'Atención de enfermería · llegó a las ' +
    new Date(a.fechaHoraAdmision).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' });
  $('tomarCaja').hidden = a.estado !== 'ADMITIDO';
  $('formSignos').hidden = a.estado !== 'EN_ENFERMERIA';
  if (a.estado === 'LISTO_PARA_ATENCION') HCE.mostrar($('mensajeEnfermeria'), 'El paciente está listo para atención: ya aparece en la cola del médico.', 'ok');
  $('enfermeria').hidden = false;
}

async function enviar(ruta, cuerpo, caja, exito) {
  const r = await HCE.api(ruta, { method: 'POST', cuerpo });
  if (!r.ok) { HCE.mostrar(caja, (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo guardar.', 'error'); return false; }
  await cargarTodo();
  HCE.mostrar(caja, exito, 'ok');
  return true;
}

$('tomar').addEventListener('click', async () => {
  const r = await HCE.api('/api/admisiones/' + idAdmision + '/toma', { method: 'POST' });
  if (!r.ok) { HCE.mostrar($('mensajeEnfermeria'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo tomar al paciente.', 'error'); return; }
  await dibujarEnfermeria();
});

$('formSignos').addEventListener('submit', async (e) => {
  e.preventDefault();
  const campos = ['presionSistolica', 'presionDiastolica', 'frecuenciaCardiaca', 'frecuenciaRespiratoria', 'temperatura', 'saturacionOxigeno'];
  const cuerpo = {};
  campos.forEach(c => cuerpo[c] = $(c).value === '' ? null : Number($(c).value));
  if (await enviar('/api/admisiones/' + idAdmision + '/signos-vitales', cuerpo, $('mensajeEnfermeria'), 'Signos vitales registrados.')) {
    e.target.reset();
  }
});

$('listo').addEventListener('click', async () => {
  const ok = await HCE.confirmar('¿El paciente está listo para atención?',
    'Pasará a la cola del médico. Revisa que los signos vitales, antecedentes y alergias estén registrados.', 'Sí, está listo');
  if (!ok) return;
  const r = await HCE.api('/api/admisiones/' + idAdmision + '/listo', { method: 'POST' });
  if (!r.ok) { HCE.mostrar($('mensajeEnfermeria'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo confirmar.', 'error'); return; }
  await dibujarEnfermeria();
});

$('formAntecedente').addEventListener('submit', async (e) => {
  e.preventDefault();
  if (await enviar('/api/historias/' + idHistoria + '/antecedentes',
    { tipo: $('tipoAntecedente').value, descripcion: $('descripcionAntecedente').value }, $('mensajeAntecedente'), 'Antecedente agregado.')) {
    $('descripcionAntecedente').value = '';
  }
});

$('formAlergia').addEventListener('submit', async (e) => {
  e.preventDefault();
  if (await enviar('/api/historias/' + idHistoria + '/alergias',
    { agente: $('agente').value, reaccion: $('reaccion').value, gravedad: $('gravedad').value }, $('mensajeAlergia'), 'Alergia agregada.')) {
    $('agente').value = ''; $('reaccion').value = '';
  }
});

(async () => {
  sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  if (!idHistoria) { $('sinHistoria').hidden = false; return; }
  if (!(await cargarTodo())) return;
  // RF-07: medico y enfermeria registran antecedentes y alergias (el servidor lo valida).
  const clinico = sesion.rolCodigo === 'MEDICO' || sesion.rolCodigo === 'ENFERMERIA';
  $('formAntecedente').hidden = !clinico;
  $('formAlergia').hidden = !clinico;
  await dibujarEnfermeria();
})();
