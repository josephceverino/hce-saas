/* Pacientes: registrar paciente y abrir historia (CU-03; RF-01, RF-04; RN-01, RN-06; DEC-02). */
const $ = (id) => document.getElementById(id);
const campos = ['tipoDocumento', 'nroDocumento', 'nombres', 'apellidos', 'fechaNacimiento', 'sexo', 'telefono', 'correo', 'direccion'];

async function cargar() {
  const r = await HCE.api('/api/historias');
  if (!r.ok) return;
  const cuerpo = $('cuerpo');
  cuerpo.replaceChildren();
  r.cuerpo.forEach(h => {
    const tr = document.createElement('tr');
    [h.nroHc, h.tipoDocumento + ' ' + h.nroDocumento, h.paciente, h.fechaApertura, h.establecimiento, h.estado]
      .forEach(t => tr.appendChild(HCE.el('td', null, t)));
    cuerpo.appendChild(tr);
  });
  $('cuenta').textContent = r.cuerpo.length;
  $('vacio').hidden = r.cuerpo.length > 0;
}

$('formulario').addEventListener('submit', async (e) => {
  e.preventDefault();
  const datos = {};
  campos.forEach(c => datos[c] = $(c).value);
  const r = await HCE.api('/api/pacientes', { method: 'POST', cuerpo: datos });
  if (r.status === 201) {
    HCE.mostrar($('mensaje'), 'Historia clínica ' + r.cuerpo.nroHc + ' abierta para ' + r.cuerpo.paciente + '.', 'ok');
    e.target.reset();
    cargar();
  } else if (r.status === 200) {
    // Flujo alternativo de CU-03: el paciente ya tiene historia en este hospital (RN-01).
    HCE.mostrar($('mensaje'), 'El paciente ya tiene la historia clínica ' + r.cuerpo.nroHc + ' en tu hospital (' + r.cuerpo.paciente + '). No se creó otra.', 'info');
    cargar();
  } else if (r.cuerpo) {
    HCE.mostrar($('mensaje'), r.cuerpo.mensaje || 'No se pudo registrar.', 'error');
  }
});

(async () => {
  const sesion = await HCE.iniciarPantalla('pacientes');
  if (sesion) cargar();
})();
