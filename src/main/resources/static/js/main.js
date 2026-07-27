/**
 * main.js — LauTuQuy Management System
 * Chứa các tiện ích JavaScript chung dùng xuyên suốt ứng dụng.
 */

/* ----------------------------------------------------------------
   Auto-dismiss Flash Alerts sau 4 giây
   ---------------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', () => {
    const alerts = document.querySelectorAll('.alert[data-auto-dismiss]');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-10px)';
            alert.style.transition = 'all 0.3s ease';
            setTimeout(() => alert.remove(), 300);
        }, 4000);
    });
});

/* ----------------------------------------------------------------
   Sidebar Toggle (Mobile)
   ---------------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', () => {
    const toggleBtn = document.getElementById('sidebar-toggle');
    const sidebar   = document.querySelector('.sidebar');
    const overlay   = document.getElementById('sidebar-overlay');

    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', () => {
            sidebar.classList.toggle('open');
            if (overlay) overlay.classList.toggle('active');
        });

        if (overlay) {
            overlay.addEventListener('click', () => {
                sidebar.classList.remove('open');
                overlay.classList.remove('active');
            });
        }
    }
});

/* ----------------------------------------------------------------
   Confirm Dialog cho hành động nguy hiểm (Khóa tài khoản, Hủy đặt bàn, Xóa...)
   Cách hoạt động:
     1. Bắt click trên button[data-confirm]
     2. Hiện confirm dialog
     3. Nếu user bấm OK → gọi form.submit() trực tiếp (bypass event listener)
     4. Nếu user bấm Cancel → không làm gì, form không submit
   ---------------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('button[data-confirm]').forEach(btn => {
        btn.addEventListener('click', function (e) {
            // Chặn submit mặc định TRƯỚC
            e.preventDefault();
            e.stopPropagation();

            const msg = this.dataset.confirm || 'Bạn có chắc chắn muốn thực hiện hành động này?';

            if (confirm(msg)) {
                // User bấm OK → tìm form cha và submit bằng JS (vượt qua event listener)
                const form = this.closest('form');
                if (form) {
                    // Dùng HTMLFormElement.prototype.submit để bypass addEventListener
                    HTMLFormElement.prototype.submit.call(form);
                }
            }
            // Nếu Cancel → không làm gì, form không được submit
        });
    });
});

/* ----------------------------------------------------------------
   Form Submit Loading State
   ---------------------------------------------------------------- */
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('form[data-loading]').forEach(form => {
        form.addEventListener('submit', () => {
            const btn = form.querySelector('button[type="submit"]');
            if (btn) {
                btn.disabled = true;
                const original = btn.innerHTML;
                btn.innerHTML = '<span class="spinner"></span> Đang xử lý...';
                // Re-enable sau 10s để tránh stuck
                setTimeout(() => {
                    btn.disabled = false;
                    btn.innerHTML = original;
                }, 10000);
            }
        });
    });
});

/* ----------------------------------------------------------------
   Dynamic Toast Notification Helper
   ---------------------------------------------------------------- */
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.style.cssText = 'position:fixed; top:24px; right:24px; z-index:9999; display:flex; flex-direction:column; gap:10px; pointer-events:none;';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    const bg = type === 'success' ? '#2e7d52' : '#c0392b';
    const icon = type === 'success' ? 'bi-check-circle-fill' : 'bi-exclamation-triangle-fill';
    toast.style.cssText = `background:${bg}; color:#fff; padding:12px 18px; border-radius:8px; box-shadow:0 4px 12px rgba(0,0,0,0.25); display:flex; align-items:center; gap:10px; font-size:0.9rem; font-weight:500; opacity:0; transform:translateY(-15px); transition:all 0.3s ease; pointer-events:auto;`;
    toast.innerHTML = `<i class="bi ${icon}" style="font-size:1.1rem;"></i><span>${message}</span>`;
    container.appendChild(toast);

    requestAnimationFrame(() => {
        toast.style.opacity = '1';
        toast.style.transform = 'translateY(0)';
    });

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-15px)';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}
