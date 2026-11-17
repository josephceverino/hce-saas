/* P-04 Historia clinica (A-4; CU-06; RF-06; RN-03, RN-06, RN-07).
   El servidor decide quien ve que: si la historia es de otro hospital responde 403 y aqui solo se muestra el mensaje. */
const $ = (id) => document.getElementById(id);

function dibujarCabecera(h, puedeAtender) {
  const p = h.paciente;
  $('avatar').textContent = HCE.iniciales(p.nombres, p.apellidos);
  $('nombre').textContent = p.apellidos + ', ' + p.nombres;
  $('datosPaciente').textContent = p.tipoDocumento + ' ' + p.nroDocumento + ' · ' + HCE.edad(p.fechaNacimiento) +
    ' años · ' + (p.sexo === 'F' ? 'Femenino' : 'Masculino') + ' · ' + h.nroHc + ' · ' + h.establecimiento;
  const alergias = $('alergias');
  alergias.replaceChildren();
  if (h.alergias.length === 0) {
    alergias.append(HCE.el('span', 'hce-chip ok', 'Sin alergias registradas'));
  } else {
    alergias.append(HCE.el('strong', null, 'Alergias:'));
    h.alergias.forEach(a => alergias.append(HCE.el('span', 'hce-chip error', a.agente + ' · ' + a.reaccion + ' (' + a.gravedad.toLowerCase() + ')')));
  }
  const boton = $('nuevaAtencion');
  boton.hidden = !puedeAtender;
  boton.href = '/atencion/?historia=' + encodeURIComponent(h.idHistoriaClinica);
  $('cabecera').hidden = false;
}

function dibujarAntecedentes(h) {
  if (h.antecedentes.length === 0) return;
  const ul = $('antecedentes');
  ul.replaceChildren();
  h.antecedentes.forEach(a => ul.append(HCE.el('li', null, a.tipo + ': ' + a.descripcion + ' (' + a.fechaRegistro + ')')));
  $('antecedentesTarjeta').hidden = false;
}

function dibujarAtenciones(h, puedeAtender) {
  const contenedor = $('atenciones');
  contenedor.replaceChildren();
  h.atenciones.forEach(a => {
    const evento = HCE.el('article', 'hce-evento' + (a.cerrada ? ' cerrada' : ''));
    const cabecera = HCE.el('div', 'hce-evento-cabecera');
    const titulo = HCE.el('div');
    titulo.append(HCE.el('div', 'titulo', a.motivo), HCE.el('div', 'meta', HCE.fechaHora(a.fechaHora) + ' · ' + a.profesional));
    const estado = HCE.el('span', 'hce-chip ' + (a.cerrada ? 'info hce-candado' : 'alerta'), a.cerrada ? '🔒 Cerrada' : 'Abierta');
    cabecera.append(titulo, estado);
    evento.append(cabecera, HCE.detalleAtencion(a));
    if (puedeAtender) {
      const acciones = HCE.el('div', 'hce-acciones');
      const enlace = HCE.el('a', 'hce-boton claro', a.cerrada ? 'Agregar enmienda' : 'Continuar atención');
      enlace.href = '/atencion/?id=' + encodeURIComponent(a.id);
      acciones.append(enlace);
      evento.append(acciones);
    }
    contenedor.append(evento);
  });
  $('cuenta').textContent = h.atenciones.length;
  $('sinAtenciones').hidden = h.atenciones.length > 0;
  $('atencionesTarjeta').hidden = false;
}

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  const id = new URLSearchParams(window.location.search).get('id');
  if (!id) { $('sinHistoria').hidden = false; return; }
  const r = await HCE.api('/api/historias/' + encodeURIComponent(id));
  if (!r.ok) {
    HCE.mostrar($('error'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo abrir la historia clínica.', 'error');
    return;
  }
  const puedeAtender = HCE.tieneOpcion(sesion, 'atencion');
  dibujarCabecera(r.cuerpo, puedeAtender);
  dibujarAntecedentes(r.cuerpo);
  dibujarAtenciones(r.cuerpo, puedeAtender);
})();
