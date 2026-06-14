document.addEventListener("DOMContentLoaded", () => {
    if (!requireAuth()) {
        return;
    }

    bindLogoutButton();
    bindProfileForm();
    bindCalendarTypeToggle();

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
                    birthPlace: optionalInputValue("birthPlace"),
                    leapMonth: document.getElementById("calendarType").value === "LUNAR"
                            && document.getElementById("leapMonth").checked
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
        bindConsultationLink(profileId);
        bindReanalyzeButton(profileId);
        bindDeleteButton(profileId);
    } catch (error) {
        showError(error.message || "프로필 상세를 불러오지 못했습니다.");
    }
}

function bindReanalyzeButton(profileId) {
    const reanalyzeButton = document.getElementById("reanalyzeButton");
    if (!reanalyzeButton) {
        return;
    }

    reanalyzeButton.addEventListener("click", async () => {
        const confirmed = window.confirm("최신 기준으로 분석 결과를 다시 생성할까요?");
        if (!confirmed) {
            return;
        }

        clearError();
        clearSuccess();
        setButtonLoading(reanalyzeButton, true, "생성 중");

        try {
            const profile = await apiFetch("/api/profiles/" + profileId + "/reanalyze", {
                method: "POST"
            });
            renderProfileDetail(profile);
            bindConsultationLink(profileId);
            showSuccess("분석 결과를 다시 생성했습니다.");
        } catch (error) {
            const message = error.message === "접근할 수 없는 프로필입니다."
                    ? error.message
                    : "분석 재생성 중 오류가 발생했습니다.";
            showError(message);
        } finally {
            setButtonLoading(reanalyzeButton, false, "분석 다시 생성");
        }
    });
}

function bindConsultationLink(profileId) {
    const link = document.getElementById("consultationLink");
    if (link) {
        link.href = "/view/profiles/" + profileId + "/consultations";
    }
}

function renderProfileDetail(profile) {
    document.getElementById("profileDetail").hidden = false;
    setText("profileName", profile.profileName);
    setText("birthDate", profile.birthDate);
    setText("birthTime", profile.birthTime);
    setText("calendarType", formatCalendarType(profile.calendarType));
    setText("leapMonth", profile.leapMonth ? "윤달" : "아님");
    setText("gender", formatGender(profile.gender));
    setText("birthPlace", profile.birthPlace || "");
    setText("yearPillar", displayCalculatedValue(profile.yearPillar));
    setText("monthPillar", displayCalculatedValue(profile.monthPillar));
    setText("dayPillar", displayCalculatedValue(profile.dayPillar));
    setText("hourPillar", displayCalculatedValue(profile.hourPillar));
    renderList("fiveElementsSummary", profile.fiveElementsSummary, "계산 정보를 불러오지 못했습니다.");
    renderList("yinYangSummary", profile.yinYangSummary, "계산 정보를 불러오지 못했습니다.");
    setText("analysisSummary", sanitizeDisplayText(profile.analysisSummary));
    setText("elementSummary", sanitizeDisplayText(profile.elementSummary));
    renderList("strengths", profile.strengths);
    renderList("cautions", profile.cautions);
    renderRecommendedQuestionLinks(profile.id, profile.recommendedQuestions);
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
        clearSuccess();
        setButtonLoading(deleteButton, true, "삭제 중");
        try {
            await apiFetch("/api/profiles/" + profileId, {
                method: "DELETE"
            });
            window.location.href = "/view/profiles";
        } catch (error) {
            showError(error.message || "프로필 삭제에 실패했습니다.");
            setButtonLoading(deleteButton, false, "삭제");
        }
    });
}

function bindCalendarTypeToggle() {
    const calendarType = document.getElementById("calendarType");
    const leapMonthField = document.getElementById("leapMonthField");
    const leapMonth = document.getElementById("leapMonth");
    if (!calendarType || !leapMonthField || !leapMonth) {
        return;
    }

    const sync = () => {
        const isLunar = calendarType.value === "LUNAR";
        leapMonthField.hidden = !isLunar;
        leapMonth.disabled = !isLunar;
        if (!isLunar) {
            leapMonth.checked = false;
        }
    };

    calendarType.addEventListener("change", sync);
    sync();
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

function renderList(id, values, fallbackMessage = "") {
    const list = document.getElementById(id);
    if (!list) {
        return;
    }
    list.innerHTML = "";

    const items = values || [];
    if (items.length === 0 && fallbackMessage) {
        const item = document.createElement("li");
        item.textContent = fallbackMessage;
        list.appendChild(item);
        return;
    }

    items.forEach((value) => {
        const item = document.createElement("li");
        item.textContent = value;
        list.appendChild(item);
    });
}

function renderRecommendedQuestionLinks(profileId, values) {
    const container = document.getElementById("recommendedQuestions");
    if (!container) {
        return;
    }
    container.innerHTML = "";

    (values || []).forEach((value) => {
        const link = document.createElement("a");
        link.className = "question-chip";
        link.href = "/view/profiles/" + profileId + "/consultations?question=" + encodeURIComponent(value);
        link.textContent = value;
        container.appendChild(link);
    });
}

function showSuccess(message) {
    const target = document.getElementById("successMessage");
    if (!target) {
        return;
    }
    target.textContent = message;
    target.hidden = false;
}

function clearSuccess() {
    const target = document.getElementById("successMessage");
    if (!target) {
        return;
    }
    target.textContent = "";
    target.hidden = true;
}

function setButtonLoading(button, isLoading, text) {
    button.disabled = isLoading;
    button.textContent = text;
}

function optionalInputValue(id) {
    const input = document.getElementById(id);
    if (!input) {
        return null;
    }

    const value = input.value.trim();
    return value || null;
}

function displayCalculatedValue(value) {
    return value || "계산 정보를 불러오지 못했습니다.";
}

function sanitizeDisplayText(value) {
    if (!value) {
        return "";
    }

    return value
            .replaceAll("MVP 만세력 계산", "기본 계산")
            .replaceAll("MVP 계산", "기본 계산")
            .replaceAll("MVP", "기본");
}

function escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value || "";
    return div.innerHTML;
}
