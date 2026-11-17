/* P-16 Registrar paciente con filiacion completa (CU-03, CU-19; RF-01, RF-04, RF-27; RN-01, RN-06, RN-15; DEC-02).
   Respuestas: 201 historia abierta (y cuenta del portal nueva), 200 ya existia en tu hospital, 409 esta en otro hospital.
   Las validaciones las hace el servidor; aqui solo se muestran sus mensajes. */
const $ = (id) => document.getElementById(id);
const CAMPOS = ['tipoDocumento', 'nroDocumento', 'nombres', 'apellidos', 'fechaNacimiento', 'sexo', 'telefono', 'correo',
  'direccion', 'lugarNacimiento', 'procedencia', 'estadoCivil', 'gradoInstruccion', 'ocupacion', 'religion',
  'contactoEmergenciaNombre', 'contactoEmergenciaParentesco', 'contactoEmergenciaTelefono'];
let puedeVerHistoria = false;

$('fechaNacimiento').addEventListener('input', () => {
  const edad = HCE.edad($('fechaNacimiento').value);
  $('edad').value = edad === '' || edad < 0 ? '' : edad + ' años';
});

function mostrarResultado(c, existia) {
  $('formulario').hidden = true;
  $('resultado').hidden = false;
  $('resultadoAntetitulo').textContent = existia ? 'El paciente ya estaba registrado' : 'Historia clínica abierta';
  $('resultadoTitulo').textContent = c.paciente + ' · ' + c.nroHc;
  $('resultadoTexto').textContent = existia
    ? 'Ya tiene historia clínica en tu hospital. No se creó otra.'
    : 'Se registró al paciente y se abrió su historia clínica en tu hospital.';
  const cuenta = c.cuentaPortal;
  $('constanciaPaciente').hidden = !cuenta;
  $('avisoConstancia').hidden = !cuenta;
  $('imprimir').hidden = !cuenta;
  if (cuenta) {
    $('cUsuario').textContent = cuenta.usuario;
    $('cClave').textContent = cuenta.claveTemporal;
  }
  const enlace = $('verHistoria');
  enlace.hidden = !puedeVerHistoria;
  enlace.href = '/historia/?id=' + encodeURIComponent(c.idHistoriaClinica);
  window.scrollTo(0, 0);
}

$('formulario').addEventListener('submit', async (e) => {
  e.preventDefault();
  const datos = {};
  CAMPOS.forEach(c => datos[c] = $(c).value);
  const r = await HCE.api('/api/pacientes', { method: 'POST', cuerpo: datos });
  const c = r.cuerpo || {};
  if (r.status === 201) mostrarResultado(c, false);
  else if (r.status === 200) mostrarResultado(c, true);
  else HCE.mostrar($('mensaje'), c.mensaje || 'No se pudo registrar al paciente.', 'error');
});

$('imprimir').addEventListener('click', () => HCE.imprimir());
$('otro').addEventListener('click', () => {
  $('cClave').textContent = ''; // la clave temporal no se vuelve a mostrar
  $('resultado').hidden = true;
  $('formulario').reset();
  $('edad').value = '';
  $('mensaje').className = 'hce-mensaje';
  $('formulario').hidden = false;
});

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (sesion) puedeVerHistoria = HCE.tieneOpcion(sesion, 'historia');
})();
