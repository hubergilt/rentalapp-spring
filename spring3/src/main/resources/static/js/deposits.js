// Powers the inline, AJAX-refreshed security deposit installments panel on
// a tenancy's show page (fragments/deposit-panel.html). No framework - a
// couple of small fetch() calls that swap #deposit-panel's innerHTML with
// the server-rendered fragment.
window.depositPanel = (function () {
    function swap(html) {
        const wrapper = document.createElement('div');
        wrapper.innerHTML = html;
        const fresh = wrapper.querySelector('#deposit-panel');
        const current = document.getElementById('deposit-panel');
        if (fresh && current) {
            current.replaceWith(fresh);
        }
    }

    function add() {
        const form = document.getElementById('deposit-add-form');
        const url = form.dataset.addUrl;
        const body = new URLSearchParams(new FormData(form));
        fetch(url, { method: 'POST', body })
            .then(r => r.text())
            .then(swap)
            .catch(() => alert('Could not add the installment. Please try again.'));
    }

    function remove(url) {
        if (!confirm('Remove this installment?')) return;
        fetch(url, { method: 'POST' })
            .then(r => r.text())
            .then(swap)
            .catch(() => alert('Could not remove the installment. Please try again.'));
    }

    return { add, remove };
})();
