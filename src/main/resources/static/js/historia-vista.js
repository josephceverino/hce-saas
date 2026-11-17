/* Dibujo comun de una historia clinica (HistoriaDetalleResponse). Lo usan P-04 (historia/) y P-05 (mi-historial/).
   Solo presentacion: ninguna regla de negocio vive aqui. Requiere comun.js y los ids de las paginas que lo usan. */
const VistaHistoria = (() => {
  const $ = (id) => document.getElementById(id);

  /** Cabecera del paciente. hrefNuevaAtencion: si no es null, muestra el boton "Registrar atencion". */
  function cabecera(h, hrefNuevaAtencion) {
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
      h.alergias.forEach(a => alergias.append(HCE.el('span', 'hce-chip error',
        a.agente + ' · ' + a.reaccion + ' (' + a.gravedad.toLowerCase() + ')')));
    }
    const boton = $('nuevaAtencion');
    if (boton) {
      boton.hidden = !hrefNuevaAtencion;
      if (hrefNuevaAtencion) boton.href = hrefNuevaAtencion;
    }
    $('cabecera').hidden = false;
  }

  function antecedentes(h) {
    if (h.antecedentes.length === 0) return;
    const ul = $('antecedentes');
    ul.replaceChildren();
    h.antecedentes.forEach(a => ul.append(HCE.el('li', null, a.tipo + ': ' + a.descripcion + ' (' + a.fechaRegistro + ')')));
    $('antecedentesTarjeta').hidden = false;
  }

  /** Linea de tiempo. accion(a) devuelve {texto, href} para el boton de cada atencion, o null si no hay boton. */
  function atenciones(h, accion) {
    const contenedor = $('atenciones');
    contenedor.replaceChildren();
    h.atenciones.forEach(a => {
      const evento = HCE.el('article', 'hce-evento' + (a.cerrada ? ' cerrada' : ''));
      const cab = HCE.el('div', 'hce-evento-cabecera');
      const titulo = HCE.el('div');
      titulo.append(HCE.el('div', 'titulo', a.motivo), HCE.el('div', 'meta', HCE.fechaHora(a.fechaHora) + ' · ' + a.profesional));
      const estado = HCE.el('span', 'hce-chip ' + (a.cerrada ? 'info hce-candado' : 'alerta'), a.cerrada ? '🔒 Cerrada' : 'Abierta');
      cab.append(titulo, estado);
      evento.append(cab, HCE.detalleAtencion(a));
      const datos = accion ? accion(a) : null;
      if (datos) {
        const acciones = HCE.el('div', 'hce-acciones');
        const enlace = HCE.el('a', 'hce-boton claro', datos.texto);
        enlace.href = datos.href;
        acciones.append(enlace);
        evento.append(acciones);
      }
      contenedor.append(evento);
    });
    $('cuenta').textContent = h.atenciones.length;
    $('sinAtenciones').hidden = h.atenciones.length > 0;
    $('atencionesTarjeta').hidden = false;
  }

  return { cabecera, antecedentes, atenciones };
})();
