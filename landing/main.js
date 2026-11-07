// Finanzas App — Landing Interactions (Inspirado en ryoku.dev)

document.addEventListener('DOMContentLoaded', () => {
  // 1. Scroll-spy para el índice lateral numérico (00..06)
  const sections = document.querySelectorAll('.section');
  const railItems = document.querySelectorAll('.rail-item');

  const observerOptions = {
    root: null,
    rootMargin: '-20% 0px -60% 0px',
    threshold: 0
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const id = entry.target.getAttribute('id');
        railItems.forEach(item => {
          if (item.getAttribute('href') === `#${id}`) {
            item.classList.add('active');
          } else {
            item.classList.remove('active');
          }
        });
      }
    });
  }, observerOptions);

  sections.forEach(sec => observer.observe(sec));

  // 2. Botón de copiar comando del Hero
  const copyBtn = document.getElementById('copyCmd');
  if (copyBtn) {
    copyBtn.addEventListener('click', () => {
      const code = 'finanzas wearable sync --device=galaxy-watch --backend=mysql';
      navigator.clipboard.writeText(code).then(() => {
        copyBtn.textContent = '¡Copiado!';
        copyBtn.style.color = '#10b981';
        setTimeout(() => {
          copyBtn.textContent = 'Copiar';
          copyBtn.style.color = '';
        }, 2000);
      });
    });
  }

  // 3. Simulación interactiva del Smartwatch en el Showroom
  const watchQuickAdd = document.getElementById('watchQuickAdd');
  const watchSync = document.getElementById('watchSync');
  const watchToast = document.getElementById('watchToast');
  const watchBal = document.querySelector('.w-bal');
  const watchToday = document.querySelector('.w-today');

  let currentBal = 14850;
  let currentToday = 350;

  function showWatchToast(msg) {
    if (!watchToast) return;
    watchToast.textContent = msg;
    watchToast.classList.add('active');
    setTimeout(() => {
      watchToast.classList.remove('active');
    }, 1800);
  }

  if (watchQuickAdd) {
    watchQuickAdd.addEventListener('click', () => {
      const expenseAmt = 45; // Simula un café
      currentBal -= expenseAmt;
      currentToday += expenseAmt;

      if (watchBal) watchBal.textContent = `$${currentBal.toLocaleString()}`;
      if (watchToday) watchToday.textContent = `Hoy: -$${currentToday.toLocaleString()}`;

      showWatchToast('✓ -$45 Café (MySQL)');
    });
  }

  if (watchSync) {
    watchSync.addEventListener('click', () => {
      showWatchToast('⟳ Sincronizado');
    });
  }
});
