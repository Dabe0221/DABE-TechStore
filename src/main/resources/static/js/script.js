/* ==================================================
   TechStore — Global UI Helpers
   ================================================== */

document.addEventListener("DOMContentLoaded", () => {
    initializeScrollToTop();
});

/**
 * Initializes the optional scroll-to-top button.
 *
 * The page may include a button with:
 * id="topBtn"
 *
 * If the button does not exist, this script safely does nothing.
 */
function initializeScrollToTop() {
    const topButton = document.getElementById("topBtn");

    if (!topButton) {
        return;
    }

    const updateButtonVisibility = () => {
        const shouldShow = window.scrollY > 300;

        topButton.hidden = !shouldShow;
        topButton.setAttribute(
            "aria-hidden",
            shouldShow ? "false" : "true"
        );
    };

    if (topButton.dataset.scrollBound === "true") {
        return;
    }

    topButton.dataset.scrollBound = "true";

    topButton.addEventListener("click", scrollToTop);

    window.addEventListener(
        "scroll",
        updateButtonVisibility,
        { passive: true }
    );

    updateButtonVisibility();
}

/**
 * Smoothly scrolls the page back to the top.
 */
function scrollToTop() {
    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}