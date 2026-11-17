/* P-04 Historia clinica (A-4; CU-06; RF-06; RN-03, RN-06, RN-07).
   El servidor decide quien ve que: si la historia es de otro hospital responde 403 y aqui solo se muestra el mensaje.
   El dibujo de la historia es comun con Mi historial (js/historia-vista.js). */
const $ = (id) => document.getElementById(id);

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
  const h = r.cuerpo;
  const puedeAtender = HCE.tieneOpcion(sesion, 'atencion');
  VistaHistoria.cabecera(h, puedeAtender ? '/atencion/?historia=' + encodeURIComponent(h.idHistoriaClinica) : null);
  VistaHistoria.antecedentes(h);
  VistaHistoria.atenciones(h, a => puedeAtender
    ? { texto: a.cerrada ? 'Agregar enmienda' : 'Continuar atención', href: '/atencion/?id=' + encodeURIComponent(a.id) }
    : null);
})();
