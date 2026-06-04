document.addEventListener("DOMContentLoaded", () => {
    if (!requireAuth()) {
        return;
    }

    bindLogoutButton();
    bindProfileForm();

    if (document.getElementById("profilesContainer")) {
        loadProfiles();
    }

    if (document.getElementById("profileDetail")) {
        loadProfileDetail();
    }
});

async function loadProfiles() {
    clearError();

    try {
        const profiles = await apiFetch("/api/profiles");
        renderProfiles(profiles);
    } catch (error) {
        showError(error.message || "프로필 목록을 불러오지 못했습니다.");
    }
}

function renderProfiles(profiles) {
    const container = document.getElementById("profilesContainer");
    const emptyState = document.getElementById("emptyState");
    container.innerHTML = "";

    if (!profiles || profiles.length === 0) {
        emptyState.hidden = false;
        return;
    }

    emptyState.hidden = true;
    profiles.forEach((profile) => {
        const card = document.createElement("a");
        card.className = "profile-card";
        card.href = "/view/profiles/" + profile.id;
        card.innerHTML = `
            <strong>${escapeHtml(profile.profileName)}</strong>
            <span>${escapeHtml(profile.birthDate)} ${escapeHtml(profile.birthTime)}</span>
            <span>${formatCalendarType(profile.calendarType)} · ${formatGender(profile.gender)}</span>
        `;
        container.appendChild(card);
    });
}

function bindProfileForm() {
    const form = document.getElementById("profileForm");
    if (!form) {
        return;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        clearError();

        try {
            const profile = await apiFetch("/api/profiles", {
                method: "POST",
                body: JSON.stringify({
                    profileName: document.getElementById("profileName").value.trim(),
                    birthDate: document.getElementById("birthDate").value,
                    birthTime: normalizeTime(document.getElementById("birthTime").value),
                    calendarType: document.getElementById("calendarType").value,
                    gender: document.getElementById("gender").value,
                    birthPlace: document.getElementById("birthPlace").value.trim()
                })
            });
            window.location.href = "/view/profiles/" + profile.id;
        } catch (error) {
            showError(error.message || "프로필 등록에 실패했습니다.");
        }
    });
}

async function loadProfileDetail() {
    clearError();

    try {
        const profileId = getProfileIdFromPath();
        const profile = await apiFetch("/api/profiles/" + profileId);
        renderProfileDetail(profile);
        bindDeleteButton(profileId);
    } catch (error) {
        showError(error.message || "프로필 상세를 불러오지 못했습니다.");
    }
}

function renderProfileDetail(profile) {
    document.getElementById("profileDetail").hidden = false;
    setText("profileName", profile.profileName);
    setText("birthDate", profile.birthDate);
    setText("birthTime", profile.birthTime);
    setText("calendarType", formatCalendarType(profile.calendarType));
    setText("gender", formatGender(profile.gender));
    setText("birthPlace", profile.birthPlace || "-");
    setText("analysisSummary", profile.analysisSummary);
    setText("elementSummary", profile.elementSummary);
    renderList("strengths", profile.strengths);
    renderList("cautions", profile.cautions);
    renderList("recommendedQuestions", profile.recommendedQuestions);
}

function bindDeleteButton(profileId) {
    const deleteButton = document.getElementById("deleteButton");
    if (!deleteButton) {
        return;
    }

    deleteButton.addEventListener("click", async () => {
        const confirmed = window.confirm("프로필을 삭제할까요?");
        if (!confirmed) {
            return;
        }

        clearError();
        try {
            await apiFetch("/api/profiles/" + profileId, {
                method: "DELETE"
            });
            window.location.href = "/view/profiles";
        } catch (error) {
            showError(error.message || "프로필 삭제에 실패했습니다.");
        }
    });
}

function getProfileIdFromPath() {
    const segments = window.location.pathname.split("/").filter(Boolean);
    return segments[segments.length - 1];
}

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value || "";
    }
}

function renderList(id, values) {
    const list = document.getElementById(id);
    list.innerHTML = "";

    (values || []).forEach((value) => {
        const item = document.createElement("li");
        item.textContent = value;
        list.appendChild(item);
    });
}

function escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value || "";
    return div.innerHTML;
}
