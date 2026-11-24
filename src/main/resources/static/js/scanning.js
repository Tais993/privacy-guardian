document.addEventListener("DOMContentLoaded", () => {
    const percent = document.getElementById("scan-percent");
    const entries = [...document.querySelectorAll(".scan-log p")];
    const startedAt = Date.now();
    const timer = setInterval(() => {
        const elapsed = Date.now() - startedAt;
        const value = Math.min(100, Math.floor(elapsed / 100));
        percent.textContent = value;
        const activeIndex = Math.min(entries.length - 1, Math.floor(elapsed / 2500));
        entries.forEach((entry, index) => {
            entry.classList.toggle("is-current", index === activeIndex);
            entry.classList.toggle("is-complete", index < activeIndex);
        });
        if (value === 100) {
            clearInterval(timer);
            window.location.assign(`/results${window.location.search}`);
        }
    }, 100);
});
