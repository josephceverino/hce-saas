/* P-12 Exámenes de apoyo al diagnóstico (CU-23; RF-09; RN-18, RN-06). Solo muestra y envía:
   el servidor decide el hospital (sesión), el permiso y que solo se registre sobre solicitudes pendientes. */
const $ = (id) => document.getElementById(id);
const PESTANAS = [
  { clave: 'SOLICITADO', texto: 'Pendientes', vacio: 'No hay exámenes pendientes en tu hospital.' },
  { clave: 'RESULTADO_REGISTRADO', texto: 'Con resultado', vacio: 'Todavía no registraste resultados.' }
];
let estado = 'SOLICITADO';
let cantidades = { SOLICITADO: 0, RESULTADO_REGISTRADO: 0 };

async function registrar(fila) {
  const texto = await pedirResultado(fila.examen.tipoExamen, fila.paciente);
  if (!texto) return;
  const r = await HCE.api('/api/examenes/' + fila.examen.id + '/resultado', { method: 'POST', cuerpo: { resultado: texto } });
  if (r.ok) {
    HCE.mostrar($('mensaje'), 'Resultado de ' + fila.examen.tipoExamen + ' registrado para ' + fila.paciente + '.', 'ok');
    await cargar();
  } else {
    HCE.mostrar($('mensaje'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo registrar el resultado.', 'error');
  }
}

/** Dialogo propio con un campo de texto. Devuelve el texto o null si se cancela. */
function pedirResultado(examen, paciente) {
  return new Promise(resolver => {
    const fondo = HCE.el('div', 'hce-modal-fondo');
    const caja = HCE.el('div', 'hce-modal');
    caja.setAttribute('role', 'dialog');
    caja.setAttribute('aria-modal', 'true');
    const campo = HCE.el('textarea');
    campo.setAttribute('aria-label', 'Resultado del examen');
    campo.rows = 5;
    const error = HCE.el('div', 'hce-mensaje');
    const cancelar = HCE.el('button', 'hce-boton claro', 'Cancelar');
    const guardar = HCE.el('button', 'hce-boton primario', 'Guardar resultado');
    cancelar.type = guardar.type = 'button';
    const cerrar = (v) => { fondo.remove(); resolver(v); };
    cancelar.addEventListener('click', () => cerrar(null));
    guardar.addEventListener('click', () => {
      if (!campo.value.trim()) { HCE.mostrar(error, 'Ingresa el resultado del examen.', 'error'); return; }
      cerrar(campo.value.trim());
    });
    const botones = HCE.el('div', 'hce-acciones');
    botones.append(cancelar, guardar);
    caja.append(HCE.el('h2', null, 'Resultado de ' + examen), HCE.el('p', null, 'Paciente: ' + paciente), campo, error, botones);
    fondo.append(caja);
    document.body.append(fondo);
    campo.focus();
  });
}

function dibujarPestanas() {
  const p = $('pestanas');
  p.replaceChildren();
  PESTANAS.forEach(f => {
    const b = HCE.el('button', 'hce-pestana' + (f.clave === estado ? ' activa' : ''));
    b.type = 'button';
    b.append(document.createTextNode(f.texto + ' '), HCE.el('span', 'cuenta', String(cantidades[f.clave])));
    b.addEventListener('click', () => { estado = f.clave; cargar(); });
    p.append(b);
  });
}

async function cargar() {
  const [actual, otro] = await Promise.all([
    HCE.api('/api/examenes?estado=' + estado),
    HCE.api('/api/examenes?estado=' + (estado === 'SOLICITADO' ? 'RESULTADO_REGISTRADO' : 'SOLICITADO'))
  ]);
  if (!actual.ok) { HCE.mostrar($('mensaje'), (actual.cuerpo && actual.cuerpo.mensaje) || 'No se pudo cargar la lista de exámenes.', 'error'); return; }
  cantidades[estado] = actual.cuerpo.length;
  if (otro.ok) cantidades[estado === 'SOLICITADO' ? 'RESULTADO_REGISTRADO' : 'SOLICITADO'] = otro.cuerpo.length;
  dibujarPestanas();
  const cuerpo = $('cuerpo');
  cuerpo.replaceChildren();
  actual.cuerpo.forEach(fila => {
    const x = fila.examen;
    const tr = document.createElement('tr');
    const paciente = HCE.el('td');
    paciente.append(HCE.el('div', 'nombre', fila.paciente), HCE.el('div', 'nota', fila.documento + ' · ' + fila.nroHc));
    const examen = HCE.el('td');
    examen.append(HCE.el('div', 'nombre', x.tipoExamen));
    if (x.indicaciones) examen.append(HCE.el('div', 'nota', x.indicaciones));
    if (x.resultado) examen.append(HCE.el('div', 'nota', 'Resultado: ' + x.resultado));
    const pedido = HCE.el('td', null, HCE.fechaHora(x.fechaHora) + ' · ' + x.solicitadoPor);
    const chip = HCE.el('td');
    chip.append(HCE.el('span', 'hce-chip ' + (x.resultado ? 'ok' : 'alerta'), x.resultado ? 'Con resultado' : 'Pendiente'));
    const acc = HCE.el('td', 'derecha');
    if (!x.resultado) {
      const b = HCE.el('button', 'hce-boton primario pequeno', 'Registrar resultado');
      b.type = 'button';
      b.addEventListener('click', () => registrar(fila));
      acc.append(b);
    }
    tr.append(paciente, examen, pedido, chip, acc);
    cuerpo.append(tr);
  });
  const vacio = $('vacio');
  vacio.hidden = actual.cuerpo.length > 0;
  vacio.textContent = PESTANAS.find(f => f.clave === estado).vacio;
}

$('actualizar').addEventListener('click', cargar);

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (sesion) await cargar();
})();
