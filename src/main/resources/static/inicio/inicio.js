/* Inicio propio de cada rol (plan v3, 0.4; docs/recorrido.md; A-5; RF-19, RN-06).
   Las cifras vienen de GET /api/indicadores/inicio (base de datos real, filtradas por el hospital del usuario).
   Los textos por rol son solo presentacion: que puede hacer cada rol lo decide el servidor (menu y permisos). */
const $ = (id) => document.getElementById(id);

const TEXTOS = {
  ADMISION: 'Recibes a los pacientes: búscalos, regístralos con su filiación y ábreles su historia clínica.',
  ENFERMERIA: 'Atiendes a los pacientes admitidos: tomas sus signos vitales y registras antecedentes y alergias.',
  MEDICO: 'Atiendes a los pacientes listos: revisas su historia, registras la atención, el diagnóstico y el tratamiento.',
  APOYO_DIAGNOSTICO: 'Registras los resultados de los exámenes que solicitan los médicos de tu hospital.',
  ADMIN_HOSPITAL: 'Administras el personal de tu hospital y revisas la actividad y los indicadores.'
};
const VACIOS = {
  APOYO_DIAGNOSTICO: 'Aún no hay exámenes solicitados. Cuando un médico pida un examen, aparecerá aquí.'
};
const ACCIONES = { // accion principal de cada rol, solo si el menu del servidor la incluye
  ADMISION: { clave: 'registrar-paciente', texto: 'Registrar paciente' },
  MEDICO: { clave: 'atencion', texto: 'Registrar atención' }
};
const DESCRIPCION_MENU = {
  cola: 'Pacientes admitidos hoy y su estado',
  pacientes: 'Buscar pacientes y ver su estado',
  'registrar-paciente': 'Registrar un paciente y abrir su historia',
  historia: 'Consultar la historia clínica',
  atencion: 'Registrar una atención médica',
  usuarios: 'Crear al personal y controlar su acceso',
  auditoria: 'Quién accedió a qué y cuándo',
  hospitales: 'Hospitales suscritos',
  'mi-historial': 'Tu historia clínica',
  consentimiento: 'Quién puede ver tu historia',
  examenes: 'Solicitudes pendientes y resultados',
  perfil: 'Tu cuenta y tu último acceso'
};

function saludo() {
  const h = new Date().getHours();
  return h < 12 ? 'Buenos días' : h < 19 ? 'Buenas tardes' : 'Buenas noches';
}

function dibujarAccesos(sesion) {
  const lista = $('accesos');
  (sesion.menu || []).filter(o => o.clave !== 'inicio').forEach(o => {
    const a = HCE.el('a', 'hce-acceso');
    a.href = o.ruta;
    const texto = HCE.el('div');
    texto.append(HCE.el('div', 'titulo', o.titulo), HCE.el('div', 'nota', DESCRIPCION_MENU[o.clave] || ''));
    a.append(texto, HCE.el('span', 'flecha', '→'));
    lista.append(a);
  });
  const accion = ACCIONES[sesion.rolCodigo];
  if (accion && HCE.tieneOpcion(sesion, accion.clave)) {
    const opcion = sesion.menu.find(o => o.clave === accion.clave);
    const boton = HCE.el('a', 'hce-boton primario', '+ ' + accion.texto);
    boton.href = opcion.ruta;
    $('accionesRol').append(boton);
  }
}

/** RF-22: los primeros 5 de la cola del rol, con la emergencia primero (orden del servidor). */
async function dibujarCola(sesion) {
  if (!HCE.tieneOpcion(sesion, 'cola')) return;
  const r = await HCE.api('/api/admisiones/cola');
  if (!r.ok) return;
  const activos = r.cuerpo.filter(a => a.estado !== 'ATENCION_CERRADA' && a.estado !== 'CANCELADO');
  $('colaTitulo').textContent = (sesion.rolCodigo === 'ADMISION' ? 'Pacientes del día' : 'Tu cola de hoy') + ' · ' + activos.length + ' en espera';
  const lista = $('colaLista');
  activos.slice(0, 5).forEach((a, i) => {
    const fila = HCE.el('a', 'hce-cola-fila' + (a.tipoAtencion === 'EMERGENCIA' ? ' emergencia' : ''));
    fila.href = '/cola/';
    const texto = HCE.el('div', 'texto');
    texto.append(HCE.el('div', 'nombre', a.paciente), HCE.el('div', 'nota', a.nroHc + ' · llegó a las ' +
      new Date(a.fechaHoraAdmision).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' })));
    const chips = HCE.el('div', 'hce-fila-chips');
    if (a.tipoAtencion === 'EMERGENCIA') chips.append(HCE.el('span', 'hce-chip emergencia', 'Emergencia'));
    chips.append(HCE.el('span', 'hce-chip estado-' + a.estado, a.estadoDescripcion));
    fila.append(HCE.el('span', 'hce-orden', String(i + 1)), texto, chips);
    lista.append(fila);
  });
  $('colaVacia').hidden = activos.length > 0;
  $('colaTarjeta').hidden = false;
}

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  $('saludo').textContent = saludo() + ', ' + sesion.usuario;
  $('descripcionRol').textContent = TEXTOS[sesion.rolCodigo] || '';
  dibujarAccesos(sesion);
  dibujarCola(sesion);
  // El administrador del hospital ve ademas el tablero con graficos (A-10, DEC-10). El servidor valida el rol (403).
  if (sesion.rolCodigo === 'ADMIN_HOSPITAL') TableroAdmin.dibujar();
  const r = await HCE.api('/api/indicadores/inicio');
  if (r.ok && r.cuerpo) {
    $('subtitulo').textContent = sesion.rol + ' · ' + r.cuerpo.establecimiento;
    $('antetitulo').textContent = 'Resumen de hoy · ' + r.cuerpo.establecimiento;
  }
  const tarjetas = (r.ok && r.cuerpo && r.cuerpo.tarjetas) || [];
  if (tarjetas.length === 0) {
    const vacio = $('vacio');
    vacio.textContent = VACIOS[sesion.rolCodigo] || 'No hay indicadores para mostrar.';
    vacio.hidden = false;
    return;
  }
  tarjetas.forEach(t => {
    const tarjeta = HCE.el('article', 'hce-indicador');
    tarjeta.append(HCE.el('div', 'titulo', t.titulo), HCE.el('div', 'valor', String(t.valor)), HCE.el('div', 'nota', t.nota));
    $('tarjetas').append(tarjeta);
  });
})();
