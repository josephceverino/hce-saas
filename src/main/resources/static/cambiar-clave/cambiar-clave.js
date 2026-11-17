/* P-13 Cambio de clave en el primer ingreso (CU-18; RF-28; RN-14, RN-12).
   La guia de requisitos es solo ayuda visual: la politica la valida el servidor y es quien rechaza. */
const $ = (id) => document.getElementById(id);

function revisar() {
  const actual = $('claveActual').value;
  const nueva = $('claveNueva').value;
  const marcar = (id, ok) => $(id).classList.toggle('cumple', ok);
  marcar('reqLargo', nueva.length >= 6);
  marcar('reqLetraNumero', /[A-Za-zÁÉÍÓÚáéíóúÑñ]/.test(nueva) && /\d/.test(nueva));
  marcar('reqDistinta', nueva.length > 0 && nueva !== actual);
  marcar('reqIguales', nueva.length > 0 && nueva === $('confirmacion').value);
}
['claveActual', 'claveNueva', 'confirmacion'].forEach(id => $(id).addEventListener('input', revisar));

$('formulario').addEventListener('submit', async (e) => {
  e.preventDefault();
  if ($('claveNueva').value !== $('confirmacion').value) {
    HCE.mostrar($('mensaje'), 'Las dos claves nuevas no coinciden.', 'error');
    return;
  }
  const r = await HCE.api('/api/sesion/clave', { method: 'POST',
    cuerpo: { claveActual: $('claveActual').value, claveNueva: $('claveNueva').value } });
  if (r.ok) {
    HCE.mostrar($('mensaje'), 'Clave actualizada. Entrando…', 'ok');
    window.location.replace(HCE.rutaInicial(r.cuerpo));
  } else {
    HCE.mostrar($('mensaje'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo cambiar la clave.', 'error');
  }
});

$('salir').addEventListener('click', async () => {
  await HCE.api('/api/logout', { method: 'POST', sinRedireccion: true });
  window.location.href = '/login/index.html';
});

(async () => {
  const r = await HCE.api('/api/sesion');
  if (!r.ok) return;
  if (!r.cuerpo.debeCambiarClave) {
    $('saludo').textContent = 'Elige una clave nueva para tu cuenta.';
    $('claveActual').closest('label').firstChild.textContent = 'Clave actual ';
  } else {
    $('saludo').textContent = 'Hola, ' + r.cuerpo.usuario + '. Es tu primer ingreso.';
  }
})();
