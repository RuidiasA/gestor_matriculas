// Funciones compartidas: toasts, estados, selects, tablas, etc.

export function createTools() {
    const toastContainer = ensureToastContainer();

    function ensureToastContainer() {
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        return container;
    }

    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast toast--${type}`;
        toast.textContent = message;
        toastContainer.appendChild(toast);
        setTimeout(() => toast.remove(), 3500);
    }

    function showStatus(element, message, isError = false) {
        if (!element) return;
        element.textContent = message;
        element.hidden = false;
        element.style.color = isError ? '#c0392b' : 'var(--color-primario)';
    }

    function clearStatus(element) {
        if (!element) return;
        element.hidden = true;
        element.textContent = '';
    }

    function fillSelect(select, items, placeholder, valueGetter, labelGetter) {
        if (!select) return;
        select.innerHTML = '';
        const baseOption = document.createElement('option');
        baseOption.value = '';
        baseOption.textContent = placeholder;
        select.appendChild(baseOption);
        (items || []).forEach(item => {
            const option = document.createElement('option');
            option.value = valueGetter(item);
            option.textContent = labelGetter(item);
            select.appendChild(option);
        });
    }

    function renderEmptyRow(tbody, columns, message) {
        if (!tbody) return;
        tbody.innerHTML = `<tr><td colspan="${columns}" class="muted">${message}</td></tr>`;
    }

    function markSelectedRow(tbody, row) {
        if (!tbody) return;
        tbody.querySelectorAll('tr').forEach(r => r.classList.remove('selected'));
        row.classList.add('selected');
    }

    return { showToast, showStatus, clearStatus, fillSelect, renderEmptyRow, markSelectedRow };
}

// Navegación principal del panel
export function setupNavigation(onSeccionesFocus) {
    const navLinks = document.querySelectorAll('.admin-nav a');
    const sections = document.querySelectorAll('.admin-section');

    navLinks.forEach(link => {
        link.addEventListener('click', evt => {
            evt.preventDefault();
            const target = link.getAttribute('data-section');
            if (!target) return;

            sections.forEach(sec => sec.classList.remove('active'));
            document.getElementById(target)?.classList.add('active');

            navLinks.forEach(l => l.classList.remove('active'));
            link.classList.add('active');

            if (target === 'secciones' && typeof onSeccionesFocus === 'function') {
                onSeccionesFocus();
            }
        });
    });
}
