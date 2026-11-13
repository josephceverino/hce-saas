/* P-01 Inicio de sesión (CU-01, RF-10). */
const $ = (id) => document.getElementById(id);

$('formLogin').addEventListener('submit', async (e) => {
  e.preventDefault();
  const r = await HCE.api('/api/login', {
    method: 'POST', sinRedireccion: true,
    cuerpo: { usuario: $('usuario').value, clave: $('clave').value }
  });
  if (r.ok) {
    window.location.href = '/pacientes/index.html';
  } else {
    HCE.mostrar($('mensajeLogin'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo iniciar sesión.', 'error');
  }
});

document.querySelectorAll('.hce-demo button').forEach(b => b.addEventListener('click', () => {
  $('usuario').value = b.dataset.u;
  $('clave').value = 'demo123';
}));

// Si ya hay sesión, va directo a la aplicación.
HCE.api('/api/sesion', { sinRedireccion: true }).then(r => { if (r.ok) window.location.replace('/pacientes/index.html'); });
