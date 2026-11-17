/* Graficos SVG propios, sin librerias ni CDN (DEC-10, docs/diseno_ui.md). Solo dibujan los datos que reciben:
   si todo es cero muestran un mensaje de estado vacio, nunca un grafico inventado. */
const Graficos = (() => {
  const NS = 'http://www.w3.org/2000/svg';

  function nodo(etiqueta, atributos, texto) {
    const n = document.createElementNS(NS, etiqueta);
    Object.entries(atributos || {}).forEach(([k, v]) => n.setAttribute(k, v));
    if (texto !== undefined) n.textContent = texto;
    return n;
  }

  function vacio(contenedor, mensaje) {
    contenedor.replaceChildren();
    const p = document.createElement('div');
    p.className = 'hce-vacio';
    p.textContent = mensaje;
    contenedor.append(p);
  }

  function hayDatos(datos) {
    return datos.some(d => d.valor > 0);
  }

  /** Barras verticales. datos: [{etiqueta, valor}]. */
  function barras(contenedor, datos, descripcion, mensajeVacio) {
    if (!hayDatos(datos)) { vacio(contenedor, mensajeVacio); return; }
    const ancho = 560, alto = 220, izq = 28, abajo = 28, arriba = 18;
    const maximo = Math.max(...datos.map(d => d.valor));
    const paso = (ancho - izq) / datos.length;
    const util = alto - abajo - arriba;
    const svg = nodo('svg', { viewBox: '0 0 ' + ancho + ' ' + alto, role: 'img', 'aria-label': descripcion, class: 'hce-grafico' });
    svg.append(nodo('line', { x1: izq, y1: alto - abajo, x2: ancho, y2: alto - abajo, stroke: 'var(--hce-borde)' }));
    const cadaCuantas = datos.length > 8 ? Math.ceil(datos.length / 7) : 1;
    datos.forEach((d, i) => {
      const h = maximo === 0 ? 0 : (d.valor / maximo) * util;
      const x = izq + i * paso + paso * 0.18;
      const w = paso * 0.64;
      const barra = nodo('rect', { x, y: alto - abajo - h, width: w, height: Math.max(h, d.valor > 0 ? 2 : 0), rx: 3, fill: 'var(--hce-primario)' });
      barra.append(nodo('title', {}, d.etiqueta + ': ' + d.valor));
      svg.append(barra);
      if (d.valor > 0) svg.append(nodo('text', { x: x + w / 2, y: alto - abajo - h - 4, 'text-anchor': 'middle', class: 'valor' }, String(d.valor)));
      if (i % cadaCuantas === 0) svg.append(nodo('text', { x: x + w / 2, y: alto - 8, 'text-anchor': 'middle', class: 'etiqueta' }, d.etiqueta));
    });
    contenedor.replaceChildren(svg);
  }

  /** Barras horizontales (rankings). datos: [{etiqueta, valor}], ya ordenados. */
  function barrasHorizontales(contenedor, datos, descripcion, mensajeVacio) {
    if (!hayDatos(datos)) { vacio(contenedor, mensajeVacio); return; }
    const maximo = Math.max(...datos.map(d => d.valor));
    const lista = document.createElement('div');
    lista.className = 'hce-ranking';
    lista.setAttribute('role', 'img');
    lista.setAttribute('aria-label', descripcion);
    datos.forEach(d => {
      const fila = document.createElement('div');
      fila.className = 'fila';
      const texto = document.createElement('div');
      texto.className = 'texto';
      texto.textContent = d.etiqueta;
      const pista = document.createElement('div');
      pista.className = 'pista';
      const barra = document.createElement('div');
      barra.className = 'barra';
      barra.style.width = Math.max((d.valor / maximo) * 100, 3) + '%';
      pista.append(barra);
      const valor = document.createElement('div');
      valor.className = 'cifra';
      valor.textContent = String(d.valor);
      fila.append(texto, pista, valor);
      lista.append(fila);
    });
    contenedor.replaceChildren(lista);
  }

  /** Dona de dos o mas partes. partes: [{etiqueta, valor, color}]. */
  function dona(contenedor, partes, descripcion, mensajeVacio) {
    if (!hayDatos(partes)) { vacio(contenedor, mensajeVacio); return; }
    const total = partes.reduce((s, p) => s + p.valor, 0);
    const r = 15.9155; // circunferencia 100
    const svg = nodo('svg', { viewBox: '0 0 42 42', role: 'img', 'aria-label': descripcion, class: 'hce-dona' });
    svg.append(nodo('circle', { cx: 21, cy: 21, r, fill: 'none', stroke: 'var(--hce-borde)', 'stroke-width': 6 }));
    let acumulado = 0;
    partes.forEach(p => {
      if (p.valor === 0) return;
      const porcentaje = (p.valor / total) * 100;
      const arco = nodo('circle', { cx: 21, cy: 21, r, fill: 'none', stroke: p.color, 'stroke-width': 6,
        'stroke-dasharray': porcentaje + ' ' + (100 - porcentaje), 'stroke-dashoffset': 25 - acumulado });
      arco.append(nodo('title', {}, p.etiqueta + ': ' + p.valor));
      svg.append(arco);
      acumulado += porcentaje;
    });
    svg.append(nodo('text', { x: 21, y: 22.5, 'text-anchor': 'middle', class: 'total' }, String(total)));
    const leyenda = document.createElement('ul');
    leyenda.className = 'hce-leyenda';
    partes.forEach(p => {
      const li = document.createElement('li');
      const punto = document.createElement('span');
      punto.className = 'punto';
      punto.style.background = p.color;
      li.append(punto, document.createTextNode(p.etiqueta + ': ' + p.valor));
      leyenda.append(li);
    });
    const caja = document.createElement('div');
    caja.className = 'hce-dona-caja';
    caja.append(svg, leyenda);
    contenedor.replaceChildren(caja);
  }

  return { barras, barrasHorizontales, dona };
})();
