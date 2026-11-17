/* Registrar paciente y abrir historia (A-2; CU-03; RF-01, RF-04; RN-01, RN-06; DEC-02).
   Tres respuestas distintas: 201 historia abierta, 200 ya existia en tu hospital, 409 esta en otro establecimiento. */
const $ = (id) => document.getElementById(id);
const campos = ['tipoDocumento', 'nroDocumento', 'nombres', 'apellidos', 'fechaNacimiento', 'sexo', 'telefono', 'correo', 'direccion'];
let puedeVerHistoria = false;

function mostrarResultado(texto, tipo, idHistoria) {
  HCE.mostrar($('mensaje'), texto, tipo);
  const enlace = $('verHistoria');
  const conHistoria = !!idHistoria && puedeVerHistoria;
  enlace.hidden = !conHistoria;
  if (conHistoria) enlace.href = '/historia/?id=' + encodeURIComponent(idHistoria);
  $('acciones').hidden = !(tipo === 'ok' || tipo === 'info');
}

$('formulario').addEventListener('submit', async (e) => {
  e.preventDefault();
  const datos = {};
  campos.forEach(c => datos[c] = $(c).value);
  const r = await HCE.api('/api/pacientes', { method: 'POST', cuerpo: datos });
  const c = r.cuerpo || {};
  if (r.status === 201) {
    mostrarResultado('Historia clínica ' + c.nroHc + ' abierta para ' + c.paciente + '.', 'ok', c.idHistoriaClinica);
    e.target.reset();
  } else if (r.status === 200) {
    // Flujo alternativo de CU-03: el paciente ya tiene historia en este hospital (RN-01). No se crea otra.
    mostrarResultado('El paciente ya tiene la historia clínica ' + c.nroHc + ' en tu hospital (' + c.paciente + '). No se creó otra.', 'info', c.idHistoriaClinica);
  } else if (r.status === 409) {
    // DEC-02: el documento esta registrado en otro establecimiento; no se muestran sus datos.
    mostrarResultado(c.mensaje || 'Ese documento ya está registrado en otro establecimiento.', 'error');
  } else {
    mostrarResultado(c.mensaje || 'No se pudo registrar.', 'error');
  }
});

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (sesion) puedeVerHistoria = HCE.tieneOpcion(sesion, 'historia');
})();
