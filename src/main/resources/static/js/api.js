const TOKEN_KEY = "accessToken";

function getAccessToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function saveAccessToken(accessToken) {
    localStorage.setItem(TOKEN_KEY, accessToken);
}

function clearAccessToken() {
    localStorage.removeItem(TOKEN_KEY);
}

function requireAuth() {
    if (!getAccessToken()) {
        window.location.href = "/view/login";
        return false;
    }
    return true;
}

function logout() {
    clearAccessToken();
    window.location.href = "/view/login";
}

function bindLogoutButton() {
    const logoutButton = document.getElementById("logoutButton");
    if (logoutButton) {
        logoutButton.addEventListener("click", logout);
    }
}

function showError(message, targetId = "errorMessage") {
    const target = document.getElementById(targetId);
    if (!target) {
        return;
    }
    target.textContent = message;
    target.hidden = false;
}

function clearError(targetId = "errorMessage") {
    const target = document.getElementById(targetId);
    if (!target) {
        return;
    }
    target.textContent = "";
    target.hidden = true;
}

async function apiFetch(url, options = {}) {
    const headers = {
        ...(options.headers || {})
    };

    const hasBody = options.body !== undefined && options.body !== null;
    if (hasBody && !headers["Content-Type"]) {
        headers["Content-Type"] = "application/json";
    }

    if (options.auth !== false) {
        const token = getAccessToken();
        if (token) {
            headers.Authorization = "Bearer " + token;
        }
    }

    const response = await fetch(url, {
        ...options,
        headers
    });

    if (response.status === 401) {
        clearAccessToken();
        window.location.href = "/view/login";
        throw new Error("로그인이 필요합니다.");
    }

    if (!response.ok) {
        const errorBody = await readJsonSafely(response);
        throw new Error(resolveErrorMessage(response.status, errorBody));
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
}

async function readJsonSafely(response) {
    try {
        return await response.json();
    } catch (error) {
        return null;
    }
}

function resolveErrorMessage(status, errorBody) {
    if (status === 403 || status === 404) {
        return "접근할 수 없는 프로필입니다.";
    }
    if (status === 429) {
        return "오늘의 AI 상담 요청 횟수를 초과했습니다. 내일 다시 이용해주세요.";
    }
    if (errorBody && errorBody.message) {
        return errorBody.message;
    }
    return "요청을 처리하지 못했습니다.";
}

function formatCalendarType(value) {
    return value === "LUNAR" ? "음력" : "양력";
}

function formatGender(value) {
    if (value === "MALE") {
        return "남성";
    }
    if (value === "FEMALE") {
        return "여성";
    }
    return "-";
}

function normalizeTime(value) {
    if (!value) {
        return "";
    }
    return value.length >= 5 ? value.substring(0, 5) : value;
}
