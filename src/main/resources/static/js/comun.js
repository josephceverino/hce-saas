/* Utilidades comunes del frontend HCE. Sin librerias ni CDN. */
const HCE = (() => {
  const ICONO_PACIENTES = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M17 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2"/><circle cx="10" cy="7" r="4"/><path d="M21 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>';

  /** Llama a la API y devuelve {ok, status, cuerpo}. Un 401 manda al login. */
  async function api(ruta, opciones = {}) {
    const config = { method: opciones.method || 'GET', headers: {} };
    if (opciones.cuerpo !== undefined) {
      config.headers['Content-Type'] = 'application/json';
      config.body = JSON.stringify(opciones.cuerpo);
    }
    const respuesta = await fetch(ruta, config);
    let cuerpo = null;
    try { cuerpo = await respuesta.json(); } catch (e) { /* sin cuerpo */ }
    if (respuesta.status === 401 && !opciones.sinRedireccion) {
      window.location.href = '/login/index.html';
    }
    return { ok: respuesta.ok, status: respuesta.status, cuerpo };
  }

  function mostrar(caja, texto, tipo) {
    caja.textContent = texto;
    caja.className = 'hce-mensaje ' + tipo;
  }

  function el(etiqueta, clase, texto) {
    const nodo = document.createElement(etiqueta);
    if (clase) nodo.className = clase;
    if (texto !== undefined) nodo.textContent = texto;
    return nodo;
  }

  /** Exige sesion, dibuja sidebar y barra superior y devuelve los datos de la sesion. */
  async function iniciarPantalla(activo) {
    const r = await api('/api/sesion');
    if (!r.ok) return null;
    const sesion = r.cuerpo;
    const app = document.getElementById('app');

    const sidebar = el('aside', 'hce-sidebar');
    const marca = el('div', 'hce-marca');
    const logo = el('img'); logo.src = '/logo.svg'; logo.alt = '';
    marca.append(logo, el('span', null, 'Plataforma HCE'));
    const menu = el('nav', 'hce-menu');
    const enlace = el('a', activo === 'pacientes' ? 'activo' : '');
    enlace.href = '/pacientes/index.html';
    enlace.insertAdjacentHTML('afterbegin', ICONO_PACIENTES); // SVG propio, sin datos del usuario
    enlace.append(document.createTextNode('Pacientes'));
    menu.append(enlace);
    const perfil = el('div', 'hce-perfil');
    perfil.append(el('div', 'nombre', sesion.usuario), el('div', 'rol', sesion.rol));
    const salir = el('button', 'hce-boton secundario', 'Cerrar sesión');
    salir.type = 'button';
    salir.addEventListener('click', async () => {
      await api('/api/logout', { method: 'POST', sinRedireccion: true });
      window.location.href = '/login/index.html';
    });
    perfil.append(salir);
    sidebar.append(marca, menu, perfil);

    const barra = el('header', 'hce-barra');
    barra.append(el('span', 'hce-chip', 'Hospital activo'), el('span', 'hce-hospital', sesion.establecimiento));

    app.prepend(sidebar);
    app.insertBefore(barra, document.getElementById('contenido'));
    return sesion;
  }

  return { api, mostrar, el, iniciarPantalla };
})();
