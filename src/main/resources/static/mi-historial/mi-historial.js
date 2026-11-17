/* P-05 Mi historial, solo lectura (A-6; CU-10; RF-14, RF-15, RF-16; RN-04, RN-07).
   Ningun endpoint recibe un id: la historia sale de la cuenta en sesion. No hay botones de edicion.
   Si el servidor niega el acceso (403/404) solo se muestra su mensaje. */
const $ = (id) => document.getElementById(id);

function dato(titulo, texto) {
  const bloque = HCE.el('div', 'hce-dato');
  bloque.append(HCE.el('div', 'etiqueta', titulo), HCE.el('div', 'valor', texto));
  return bloque;
}

function lista(titulo, items, texto, vacio) {
  const bloque = HCE.el('div', 'hce-dato');
  bloque.append(HCE.el('div', 'etiqueta', titulo));
  if (items.length === 0) {
    bloque.append(HCE.el('div', 'valor suave', vacio));
  } else {
    const ul = HCE.el('ul', 'hce-lista');
    items.forEach(i => ul.append(HCE.el('li', null, texto(i))));
    bloque.append(ul);
  }
  return bloque;
}

function dibujarResumen(r) {
  const c = $('resumenContenido');
  c.replaceChildren(
    dato('Paciente', r.paciente + ' · ' + r.tipoDocumento + ' ' + r.nroDocumento),
    dato('Nacimiento y sexo', r.fechaNacimiento + ' (' + HCE.edad(r.fechaNacimiento) + ' años) · ' + (r.sexo === 'F' ? 'Femenino' : 'Masculino')),
    dato('Historia clínica', r.nroHc + ' · ' + r.establecimiento),
    lista('Alergias', r.alergias, a => a.agente + ' · ' + a.reaccion + ' (gravedad ' + a.gravedad.toLowerCase() + ')', 'Sin alergias registradas.'),
    lista('Antecedentes', r.antecedentes, a => a.tipo + ': ' + a.descripcion + ' (' + a.fechaRegistro + ')', 'Sin antecedentes registrados.'),
    dato('Atenciones cerradas', r.totalAtenciones + (r.ultimaAtencion ? ' · última: ' + HCE.fechaHora(r.ultimaAtencion) : '')),
    lista('Diagnósticos', r.diagnosticos, d => d.codigoCie10 + ' · ' + d.descripcion, 'Sin diagnósticos.'),
    lista('Exámenes con resultado', r.examenes || [], x => x.tipoExamen + ' (' + x.fechaResultado + '): ' + x.resultado, 'Sin exámenes con resultado.'),
    HCE.el('p', 'hce-subtitulo', 'Generado el ' + HCE.fechaHora(r.generadoEn) + ' Datos ficticios de demostración.')
  );
}

async function verResumen() {
  const r = await HCE.api('/api/mi-historial/resumen');
  if (!r.ok) { HCE.mostrar($('error'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo generar el resumen.', 'error'); return; }
  dibujarResumen(r.cuerpo);
  $('vistaHistorial').hidden = true;
  $('resumen').hidden = false;
  window.scrollTo(0, 0);
}

$('verResumen').addEventListener('click', verResumen);
$('volver').addEventListener('click', () => { $('resumen').hidden = true; $('vistaHistorial').hidden = false; });
$('imprimir').addEventListener('click', () => window.print());

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  const r = await HCE.api('/api/mi-historial');
  if (!r.ok) {
    HCE.mostrar($('error'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo cargar tu historial.', 'error');
    return;
  }
  VistaHistoria.cabecera(r.cuerpo, null);
  VistaHistoria.antecedentes(r.cuerpo);
  VistaHistoria.atenciones(r.cuerpo, null); // solo lectura: sin botones
})();
