document.addEventListener("DOMContentLoaded", () => {

    const password = document.getElementById("registerPassword");
    const confirmPassword = document.getElementById("confirmPassword");
    const strengthFill = document.getElementById("passwordStrengthFill");
    const strengthText = document.getElementById("passwordStrengthText");
    const matchMessage = document.getElementById("passwordMatchMessage");
    const form = document.querySelector(".register-form");

    if (
        !password ||
        !confirmPassword ||
        !strengthFill ||
        !strengthText ||
        !matchMessage ||
        !form
    ) {
        return;
    }

    if (form.dataset.validationBound === "true") {
        return;
    }

    form.dataset.validationBound = "true";

    function updateStrength() {

        const value = password.value.trim();

        if (value.length === 0) {

            strengthFill.style.width = "0%";
            strengthFill.style.backgroundColor = "transparent";

            strengthText.textContent = "Password strength";
            strengthText.style.color = "";

            return;
        }

        let score = 1;

        if (value.length >= 8) score++;

        if (/[A-Z]/.test(value) && /[a-z]/.test(value)) score++;

        if (/[0-9]/.test(value)) score++;

        if (/[^A-Za-z0-9]/.test(value)) score++;

        score = Math.min(score, 4);

        const levels = {
            1: {
                width: "25%",
                text: "Weak",
                color: "#dc3545"
            },
            2: {
                width: "50%",
                text: "Fair",
                color: "#fd7e14"
            },
            3: {
                width: "75%",
                text: "Good",
                color: "#ffc107"
            },
            4: {
                width: "100%",
                text: "Strong",
                color: "#198754"
            }
        };

        const level = levels[score];

        strengthFill.style.width = level.width;
        strengthFill.style.backgroundColor = level.color;

        strengthText.textContent = level.text;
        strengthText.style.color = level.color;
    }

    function validatePasswordMatch() {

        if (confirmPassword.value === "") {

            confirmPassword.setCustomValidity("");

            matchMessage.textContent = "";
            matchMessage.className = "small mt-2";

            return false;
        }

        const matched =
            password.value === confirmPassword.value;

        confirmPassword.setCustomValidity(
            matched ? "" : "Passwords do not match."
        );

        matchMessage.textContent =
            matched
                ? "Passwords match."
                : "Passwords do not match.";

        matchMessage.className =
            matched
                ? "small mt-2 password-match-success"
                : "small mt-2 password-match-error";

        return matched;
    }

    password.addEventListener("input", () => {
        updateStrength();
        validatePasswordMatch();
    });

    confirmPassword.addEventListener(
        "input",
        validatePasswordMatch
    );

    form.addEventListener("submit", (event) => {

        password.setCustomValidity("");

        if (password.value.length < 8) {

            event.preventDefault();

            password.setCustomValidity(
                "Password must be at least 8 characters long."
            );

            password.reportValidity();
            password.focus();

            return;
        }

        if (!validatePasswordMatch()) {

            event.preventDefault();
            confirmPassword.reportValidity();
            confirmPassword.focus();
        }
    });

    updateStrength();
});