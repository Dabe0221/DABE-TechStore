document.addEventListener("DOMContentLoaded", () => {
    const toasts = document.querySelectorAll("[data-store-toast]");

    toasts.forEach((toast) => {
        if (toast.dataset.toastBound === "true") {
            return;
        }

        toast.dataset.toastBound = "true";

        let closeTimer = null;

        const closeToast = () => {
            if (toast.classList.contains("is-hiding")) {
                return;
            }

            stopTimer();
            toast.classList.add("is-hiding");

            window.setTimeout(() => {
                toast.remove();
            }, 300);
        };

        const startTimer = () => {
            stopTimer();
            closeTimer = window.setTimeout(closeToast, 5000);
        };

        const stopTimer = () => {
            if (closeTimer !== null) {
                window.clearTimeout(closeTimer);
                closeTimer = null;
            }
        };

        const closeButton = toast.querySelector("[data-toast-close]");

        if (closeButton) {
            closeButton.addEventListener("click", closeToast);
        }

        toast.addEventListener("mouseenter", stopTimer);
        toast.addEventListener("mouseleave", startTimer);

        toast.addEventListener("focusin", stopTimer);
        toast.addEventListener("focusout", startTimer);

        startTimer();
    });
});