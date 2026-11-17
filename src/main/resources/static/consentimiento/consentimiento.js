/* P-06 Consentimiento y privacidad (M-5; CU-11; RF-17; RN-04, RN-07, RN-09).
   Lista con interruptores: el interruptor encendido es un consentimiento ACTIVO; al apagarlo se revoca (con confirmacion).
   Un consentimiento revocado no se reactiva: se otorga uno nuevo. El servidor solo trabaja con mis consentimientos. */
const $ = (id) => document.getElementById(id);

function interruptor(c) {
  const etiqueta = HCE.el('label', 'hce-interruptor');
  const caja = document.createElement('input');
  caja.type = 'checkbox';
  caja.checked = c.activo;
  caja.disabled = !c.activo;
  caja.setAttribute('aria-label', c.activo ? 'Revocar el acceso de ' + c.otorgadoA : 'Acceso revocado');
  etiqueta.append(caja, HCE.el('span', 'pista'));
  if (c.activo) {
    caja.addEventListener('change', async () => {
      caja.checked = true; // solo cambia si el servidor confirma la revocacion
      const acepta = await HCE.confirmar('¿Revocar el acceso?',
        c.otorgadoA + ' dejará de tener acceso de inmediato. Esta decisión queda registrada con la fecha.', 'Sí, revocar');
      if (!acepta) return;
      const r = await HCE.api('/api/consentimientos/' + encodeURIComponent(c.id) + '/revocacion', { method: 'POST' });
      if (r.ok) { await cargar(); $('mensaje').className = 'hce-mensaje'; HCE.mostrar($('mensajeLista'), 'Acceso revocado.', 'ok'); }
      else HCE.mostrar($('mensajeLista'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo revocar.', 'error');
    });
  }
  return etiqueta;
}

async function cargar() {
  const r = await HCE.api('/api/consentimientos');
  if (!r.ok) { HCE.mostrar($('error'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudieron cargar tus consentimientos.', 'error'); return; }
  const lista = $('lista');
  lista.replaceChildren();
  r.cuerpo.forEach(c => {
    const fila = HCE.el('div', 'hce-autorizacion' + (c.activo ? '' : ' revocada'));
    const texto = HCE.el('div', 'texto');
    texto.append(HCE.el('div', 'titulo', c.otorgadoA), HCE.el('div', null, c.alcance));
    const fechas = 'Otorgado el ' + HCE.fechaHora(c.fechaOtorgado) + (c.fechaRevocado ? ' · Revocado el ' + HCE.fechaHora(c.fechaRevocado) : '');
    texto.append(HCE.el('div', 'meta', fechas));
    fila.append(texto, HCE.el('span', 'hce-chip ' + (c.activo ? 'ok' : 'error'), c.activo ? 'Activo' : 'Revocado'), interruptor(c));
    lista.append(fila);
  });
  $('cuenta').textContent = r.cuerpo.length;
  $('vacio').hidden = r.cuerpo.length > 0;
}

$('formulario').addEventListener('submit', async (e) => {
  e.preventDefault();
  const r = await HCE.api('/api/consentimientos', { method: 'POST', cuerpo: { otorgadoA: $('otorgadoA').value, alcance: $('alcance').value } });
  if (r.status === 201) {
    e.target.reset();
    await cargar();
    $('mensajeLista').className = 'hce-mensaje';
    HCE.mostrar($('mensaje'), 'Consentimiento otorgado a ' + r.cuerpo.otorgadoA + '.', 'ok');
  } else {
    HCE.mostrar($('mensaje'), (r.cuerpo && r.cuerpo.mensaje) || 'No se pudo otorgar el consentimiento.', 'error');
  }
});

(async () => {
  const sesion = await HCE.iniciarPantalla();
  if (sesion) await cargar();
})();
