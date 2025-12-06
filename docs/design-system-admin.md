# Design System — Portal Admin

Este documento resume las reglas visuales y de maquetado aplicadas al portal administrador.

## Paleta y theming
- Variables declaradas en `static/css/admin/design-system.css`.
- Colores principales: `--color-primario`, `--color-acento`, fondos `--color-fondo` / `--color-superficie`.
- Texto y estado: `--color-texto`, `--color-texto-suave`, `--color-success`, `--color-warning`, `--color-danger`.
- Sombra y radios: `--shadow-sm|md|lg`, `--radius-sm|md|lg`.
- Espaciado base: `--space-1` a `--space-6` (4px a 24px).

## Tipografía
- Fuente: **Inter** (fallbacks del sistema).
- Jerarquía: `h1` 28px, `h2` 24px, `h3` 18px, `h4` 16px, cuerpo 14px–15px.
- Usar clases utilitarias `.muted` para texto secundario y `.badge` para resaltar estados.

## Componentes base
- **Botones**: aplica clase base `.btn` + variante (`.btn-primary`, `.btn-secondary`, `.btn-outline`, `.btn-danger`). Los enlaces ligeros usan `.btn-link`; acciones neutras usan `.clear-button`.
- **Cards**: contenedor `.card` con `.card__header` (títulos + acciones) y `.card__body`. Usa padding consistente (`var(--space-5)`).
- **Formularios**: estructura con `.form-grid` o `.form-grid--compact`; cada campo dentro de `.form-field`. Utiliza `.form-actions` para grupos de botones.
- **Tablas**: utiliza `.table-wrapper` + `.admin-table`; las versiones compactas añaden `.admin-table--compact`.
- **Badges**: `.badge` base + variantes `.primary`, `.success`, `.warning`, `.danger`. Contadores circulares: `.badge-count`.
- **Modales SweetAlert2**: estilizados automáticamente con los overrides del design system; basta con usar SweetAlert2 por defecto.
- **Grillas de resumen**: `.summary-grid` o `.grid-2` / `.grid-3` para layouts responsivos.

## Navegación y layout
- El shell principal usa `.admin-layout` (sidebar + contenido). La navegación activa se marca con `.admin-nav a.active`.
- Dentro de cada sección (`.admin-section`) mantener tarjetas alineadas en grillas descritas en `portal_admin.css` (ej. `.admin-panel`, `.secciones-grid`).

## Formularios y tablas estándar
- Inputs/ selects con borde `--color-borde` y fondo `--color-superficie-2`; siempre colocar `label` antes del control.
- Botón principal de acción: `.btn-primary`; botones secundarios/limpiar: `.btn-secondary` o `.clear-button`.
- Tablas con filas seleccionadas usan la utilidad `tools.markSelectedRow` (JS) y estilo `.selected` ya definido.

## Extensión y consistencia
- Reutiliza los mismos tokens (colores, radios, sombras) para nuevos componentes.
- Para nuevas cards o tablas, hereda las clases existentes en lugar de crear variaciones ad-hoc.
- Mantén los IDs que usa JS; agrega clases nuevas para estilos si es necesario.

## Ejemplos rápidos
- **Nueva tarjeta de métricas**
  ```html
  <article class="card">
    <header class="card__header">
      <h3>Métrica</h3>
      <span class="badge success">+12%</span>
    </header>
    <div class="summary-grid">
      <div class="summary-item">
        <p class="muted">Usuarios activos</p>
        <h2>1,240</h2>
      </div>
    </div>
  </article>
  ```
- **Formulario compacto**
  ```html
  <form class="form-grid form-grid--compact">
    <div class="form-field">
      <label>Nombre</label>
      <input type="text" />
    </div>
    <div class="form-field">
      <label>Tipo</label>
      <select></select>
    </div>
    <div class="form-actions">
      <button class="btn btn-primary">Guardar</button>
      <button class="btn btn-secondary" type="button">Cancelar</button>
    </div>
  </form>
  ```

## Reglas de HTML limpio
- Usa secciones semánticas (`section`, `article`, `header`, `aside`).
- Agrupa acciones relacionadas en `.card__actions` o `.form-actions`.
- Evita estilos inline salvo casos puntuales (métricas del header). Para nuevas vistas, preferir clases reutilizables.
- Alinea los textos auxiliares con `.muted`; no dupliques estilos en línea.

Siguiendo estas pautas se mantiene la consistencia visual y se facilita escalar nuevas pantallas del portal.
