/* Mi perfil, para todos los roles (A-9; DEC-10; RN-06). Solo datos de la cuenta, sin datos clinicos.
   El hospital y el rol salen de la sesion del servidor, no de algo que el usuario pueda cambiar. */
(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (!sesion) return;
  const $ = (id) => document.getElementById(id);
  $('avatar').textContent = sesion.usuario.slice(0, 2).toUpperCase();
  $('usuario').textContent = sesion.usuario;
  $('rol').textContent = sesion.rol;
  // El administrador de plataforma no pertenece a ningun hospital.
  $('hospital').textContent = sesion.establecimiento || 'Plataforma';
  $('ultimoAcceso').textContent = sesion.ultimoAcceso ? HCE.fechaHora(sesion.ultimoAcceso) : 'Primer acceso';
})();
