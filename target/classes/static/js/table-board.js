/**
 * table-board.js — Polling AJAX trạng thái bàn NFR-03
 */
document.addEventListener('DOMContentLoaded', () => {
    const POLL_INTERVAL = 10000; // 10 giây

    function fetchTableStatuses() {
        fetch('/api/tables/status')
            .then(res => {
                if (!res.ok) throw new Error('API Table status error');
                return res.json();
            })
            .then(tables => {
                tables.forEach(table => {
                    const card = document.getElementById('table-card-' + table.id);
                    if (card) {
                        const badge = card.querySelector('.badge');
                        if (badge) {
                            badge.textContent = table.status;
                            badge.className = 'badge ' + getBadgeClass(table.status);
                        }
                    }
                });
            })
            .catch(err => console.warn('Poller warning:', err.message));
    }

    function getBadgeClass(status) {
        switch (status) {
            case 'EMPTY': return 'badge-active';
            case 'RESERVED': return 'badge-pending';
            case 'SERVING': return 'badge-seated';
            case 'DIRTY': return 'badge-locked';
            default: return 'badge-secondary';
        }
    }

    // Polling tự động
    setInterval(fetchTableStatuses, POLL_INTERVAL);
});
