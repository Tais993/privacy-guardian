document.addEventListener("DOMContentLoaded", () => {
    const closeAll = () => document.querySelectorAll(".modal").forEach((modal) => modal.hidden = true);
    document.querySelectorAll("[data-open-modal]").forEach((button) => button.addEventListener("click", () => {
        const modal = document.getElementById(button.dataset.openModal);
        modal.hidden = false;
        modal.querySelector("textarea, input")?.focus();
    }));
    document.querySelectorAll("[data-close-modal]").forEach((button) => button.addEventListener("click", closeAll));
    document.querySelectorAll(".modal").forEach((modal) => modal.addEventListener("click", (event) => {
        if (event.target === modal) modal.hidden = true;
    }));
    document.addEventListener("keydown", (event) => { if (event.key === "Escape") closeAll(); });
    document.querySelectorAll("[data-demo-form]").forEach((form) => form.addEventListener("submit", (event) => event.preventDefault()));
});
