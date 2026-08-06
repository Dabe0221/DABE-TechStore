document.addEventListener("DOMContentLoaded", () => {
    const forms = document.querySelectorAll("[data-loading-form]");

    forms.forEach((form) => {
        if (form.dataset.loadingBound === "true") {
            return;
        }

        form.dataset.loadingBound = "true";

        form.addEventListener("submit", (event) => {
            const submitButton = form.querySelector(
                'button[type="submit"][data-loading-button], ' +
                'input[type="submit"][data-loading-button]'
            );

            if (!submitButton) {
                return;
            }

            if (submitButton.disabled) {
                event.preventDefault();
                return;
            }

            const loadingText =
                submitButton.dataset.loadingText || "Processing...";

            saveOriginalState(submitButton);
            setLoadingState(submitButton, loadingText);
        });
    });
});

window.addEventListener("pageshow", () => {
    const loadingButtons = document.querySelectorAll(
        "[data-loading-button].is-loading"
    );

    loadingButtons.forEach((button) => {
        restoreButton(button);
    });
});

function saveOriginalState(button) {
    if (button.dataset.originalContent !== undefined) {
        return;
    }

    if (button.tagName === "INPUT") {
        button.dataset.originalContent = button.value;
    } else {
        button.dataset.originalContent = button.innerHTML;
    }
}

function setLoadingState(button, loadingText) {
    button.disabled = true;
    button.setAttribute("aria-disabled", "true");
    button.setAttribute("aria-busy", "true");
    button.classList.add("is-loading");

    if (button.tagName === "INPUT") {
        button.value = loadingText;
        return;
    }

    button.innerHTML = `
        <span class="store-button-spinner"
              aria-hidden="true"></span>
        <span>${escapeHtml(loadingText)}</span>
    `;
}

function restoreButton(button) {
    const originalContent = button.dataset.originalContent;

    button.disabled = false;
    button.removeAttribute("aria-disabled");
    button.removeAttribute("aria-busy");
    button.classList.remove("is-loading");

    if (originalContent === undefined) {
        return;
    }

    if (button.tagName === "INPUT") {
        button.value = originalContent;
    } else {
        button.innerHTML = originalContent;
    }
}

function escapeHtml(value) {
    const temporaryElement = document.createElement("div");
    temporaryElement.textContent = String(value);
    return temporaryElement.innerHTML;
}