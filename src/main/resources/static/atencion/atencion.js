/* P-03 Registrar atencion y tratamiento (A-3; CU-05, CU-08; RF-05, RF-08; RN-02, RN-03, RN-10, RN-11).
   La pantalla solo muestra y envia. Las reglas las aplica el servidor: permisos por rol, autor de la atencion,
   hospital, catalogo CIE-10 y atencion cerrada inmutable. Los errores se muestran tal como llegan, en español. */
const $ = (id) => document.getElementById(id);
const params = new URLSearchParams(window.location.search);
let idAtencion = params.get('id');

function error(caja, r, porDefecto) {
  HCE.mostrar(caja, (r.cuerpo && r.cuerpo.mensaje) || porDefecto, 'error');
}

/* ---- Modo 1: elegir historia ---- */
function normalizar(t) { return String(t).normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase(); }

async function elegirHistoria() {
  $('elegir').hidden = false;
  const r = await HCE.api('/api/historias');
  if (!r.ok) { error($('error'), r, 'No se pudo cargar la lista de pacientes.'); return; }
  const dibujar = () => {
    const q = normalizar($('buscar').value.trim());
    const lista = r.cuerpo.filter(h => !q || normalizar([h.nroDocumento, h.nroHc, h.paciente].join(' ')).includes(q));
    const cuerpo = $('lista');
    cuerpo.replaceChildren();
    lista.slice(0, 20).forEach(h => {
      const tr = document.createElement('tr');
      [h.nroHc, h.tipoDocumento + ' ' + h.nroDocumento, h.paciente].forEach(t => tr.append(HCE.el('td', null, t)));
      const td = HCE.el('td');
      const enlace = HCE.el('a', 'hce-boton claro', 'Registrar atención');
      enlace.href = '/atencion/?historia=' + encodeURIComponent(h.idHistoriaClinica);
      td.append(enlace);
      tr.append(td);
      cuerpo.append(tr);
    });
    $('listaVacia').hidden = lista.length > 0;
  };
  $('buscar').addEventListener('input', dibujar);
  dibujar();
}

/* ---- Modo 2: abrir atencion en una historia ---- */
async function nuevaAtencion(idHistoria, idAdmision) {
  $('nueva').hidden = false;
  if (idAdmision) ['campoEnfermedad', 'campoFunciones', 'campoSistemas'].forEach(c => { $(c).hidden = false; });
  const h = await HCE.api('/api/historias/' + encodeURIComponent(idHistoria));
  if (!h.ok) { $('nueva').hidden = true; error($('error'), h, 'No se pudo abrir la historia clínica.'); return; }
  const p = h.cuerpo.paciente;
  $('paciente').textContent = p.apellidos + ', ' + p.nombres + ' · ' + p.tipoDocumento + ' ' + p.nroDocumento + ' · ' + h.cuerpo.nroHc;
  $('formNueva').addEventListener('submit', async (e) => {
    e.preventDefault();
    const cuerpo = { idHistoriaClinica: Number(idHistoria), motivo: $('motivo').value, anamnesis: $('anamnesis').value,
      examenFisico: $('examenFisico').value };
    if (idAdmision) {
      Object.assign(cuerpo, { idAdmision: Number(idAdmision), enfermedadActual: $('enfermedadActual').value,
        funcionesBiologicas: $('funcionesBiologicas').value, examenSistemas: $('examenSistemas').value });
    }
    const r = await HCE.api('/api/atenciones', { method: 'POST', cuerpo });
    if (r.status === 201) {
      window.location.href = '/atencion/?id=' + encodeURIComponent(r.cuerpo.id);
    } else {
      error($('mensajeNueva'), r, 'No se pudo abrir la atención.');
    }
  });
}

/* ---- Modo 3: atencion existente ---- */
async function cargarAtencion() {
  const r = await HCE.api('/api/atenciones/' + encodeURIComponent(idAtencion));
  if (!r.ok) { $('atencion').hidden = true; error($('error'), r, 'No se pudo abrir la atención.'); return; }
  const a = r.cuerpo;
  $('error').className = 'hce-mensaje';
  $('tituloAtencion').textContent = (a.cerrada ? '🔒 ' : '') + 'Atención del ' + HCE.fechaHora(a.fechaHora) + ' · ' + a.profesional;
  const estado = $('estadoAtencion');
  estado.textContent = a.cerrada ? 'Cerrada' : 'Abierta';
  estado.className = 'hce-chip ' + (a.cerrada ? 'info' : 'alerta');
  $('detalle').replaceChildren(HCE.detalleAtencion(a));
  // Una atencion cerrada no se edita (RN-03): no hay formularios de edicion, solo la enmienda.
  $('edicion').hidden = a.cerrada;
  $('enmienda').hidden = !a.cerrada;
  $('atencion').hidden = false;
}

async function enviar(e, ruta, cuerpo, caja, exito, alTerminar) {
  e.preventDefault();
  const r = await HCE.api(ruta, { method: 'POST', cuerpo });
  if (r.ok) {
    e.target.reset();
    await cargarAtencion();
    HCE.mostrar(caja, exito, 'ok');
    if (alTerminar) alTerminar();
  } else {
    error(caja, r, 'No se pudo guardar.');
  }
}

function conectarModo3() {
  const base = () => '/api/atenciones/' + encodeURIComponent(idAtencion);
  let temporizador;
  $('cie10').addEventListener('input', () => {
    clearTimeout(temporizador);
    const texto = $('cie10').value.trim();
    if (texto.length < 2) return;
    temporizador = setTimeout(async () => {
      const r = await HCE.api('/api/cie10?q=' + encodeURIComponent(texto));
      if (!r.ok) return;
      const opciones = $('opcionesCie10');
      opciones.replaceChildren();
      r.cuerpo.forEach(c => { const o = document.createElement('option'); o.value = c.codigo; o.label = c.descripcion; opciones.append(o); });
    }, 200);
  });
  $('formDiagnostico').addEventListener('submit', (e) => enviar(e, base() + '/diagnosticos',
    { codigoCie10: $('cie10').value.trim(), tipo: $('tipoDiagnostico').value, certeza: $('certezaDiagnostico').value }, $('mensajeDiagnostico'), 'Diagnóstico agregado.'));
  $('formTratamiento').addEventListener('submit', (e) => enviar(e, base() + '/tratamientos',
    { indicacion: $('indicacion').value, medicamento: $('medicamento').value, dosis: $('dosis').value, duracion: $('duracion').value },
    $('mensajeTratamiento'), 'Tratamiento agregado.'));
  $('formExamen').addEventListener('submit', (e) => enviar(e, base() + '/examenes',
    { tipoExamen: $('tipoExamen').value, indicaciones: $('indicacionesExamen').value }, $('mensajeExamen'), 'Examen solicitado: apoyo al diagnóstico lo verá en su bandeja.'));
  $('formEnmienda').addEventListener('submit', (e) => enviar(e, base() + '/enmiendas',
    { motivo: $('motivoEnmienda').value, contenidoCorregido: $('contenidoEnmienda').value }, $('mensajeEnmienda'), 'Enmienda registrada.'));
  $('cerrar').addEventListener('click', async () => {
    const acepta = await HCE.confirmar('¿Cerrar la atención?',
      'Después de cerrarla no se podrá modificar. Si hay un error, se corrige con una enmienda.', 'Sí, cerrar atención');
    if (!acepta) return;
    const r = await HCE.api(base() + '/cierre', { method: 'POST' });
    if (r.ok) { await cargarAtencion(); } else { error($('mensajeCierre'), r, 'No se pudo cerrar la atención.'); }
  });
}

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  const idAdmision = params.get('admision');
  if (idAdmision && !idAtencion) {
    // Desde la cola: si la atencion ya se inicio se continua; si no, se abre el formulario ampliado.
    const existente = await HCE.api('/api/admisiones/' + encodeURIComponent(idAdmision) + '/atencion');
    if (existente.ok) { idAtencion = String(existente.cuerpo.idAtencion); }
    else {
      const adm = await HCE.api('/api/admisiones/' + encodeURIComponent(idAdmision));
      if (!adm.ok) { error($('error'), adm, 'No se pudo abrir la admisión.'); return; }
      await nuevaAtencion(adm.cuerpo.idHistoriaClinica, idAdmision);
      return;
    }
  }
  if (idAtencion) { conectarModo3(); await cargarAtencion(); }
  else if (params.get('historia')) { await nuevaAtencion(params.get('historia')); }
  else { await elegirHistoria(); }
})();
