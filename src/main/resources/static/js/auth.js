document.addEventListener("DOMContentLoaded", () => {
    bindSignupForm();
    bindLoginForm();
});

function bindSignupForm() {
    const form = document.getElementById("signupForm");
    if (!form) {
        return;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearError();

        try {
            const response = await apiFetch("/api/auth/signup", {
                method: "POST",
                auth: false,
                body: JSON.stringify({
                    email: document.getElementById("email").value.trim(),
                    password: document.getElementById("password").value,
                    nickname: document.getElementById("nickname").value.trim()
                })
            });
            saveAccessToken(response.accessToken);
            window.location.href = "/view/profiles";
        } catch (error) {
            showError(error.message || "회원가입에 실패했습니다.");
        }
    });
}

function bindLoginForm() {
    const form = document.getElementById("loginForm");
    if (!form) {
        return;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearError();

        try {
            const response = await apiFetch("/api/auth/login", {
                method: "POST",
                auth: false,
                body: JSON.stringify({
                    email: document.getElementById("email").value.trim(),
                    password: document.getElementById("password").value
                })
            });
            saveAccessToken(response.accessToken);
            window.location.href = "/view/profiles";
        } catch (error) {
            showError(error.message || "로그인에 실패했습니다.");
        }
    });
}
