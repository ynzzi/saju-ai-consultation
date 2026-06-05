let currentProfileId = null;
let requestLocked = false;

document.addEventListener("DOMContentLoaded", async () => {
    if (!requireAuth()) {
        return;
    }

    bindLogoutButton();
    currentProfileId = getConsultationProfileIdFromPath();
    bindProfileLink(currentProfileId);
    bindConsultationForm();

    await loadConsultationPage(currentProfileId);
});

async function loadConsultationPage(profileId) {
    clearError();

    try {
        const profile = await apiFetch("/api/profiles/" + profileId);
        renderConsultationProfile(profile);
        fillQuestionFromQuery();

        const consultations = await apiFetch("/api/profiles/" + profileId + "/consultations");
        renderConsultations(consultations);
        scrollChatToBottom();
    } catch (error) {
        showError(error.message || "상담 화면을 불러오지 못했습니다.");
    }
}

function renderConsultationProfile(profile) {
    setText("profileName", profile.profileName);
    setText("birthDate", profile.birthDate);
    setText("birthTime", profile.birthTime);
    setText("calendarType", formatCalendarType(profile.calendarType));
    setText("gender", formatGender(profile.gender));
    setText("analysisSummary", profile.analysisSummary);
    renderRecommendedQuestionButtons(profile.recommendedQuestions);
}

function renderConsultations(consultations) {
    const messages = document.getElementById("chatMessages");
    messages.innerHTML = "";

    const chronological = [...(consultations || [])].reverse();
    if (chronological.length === 0) {
        const empty = document.createElement("div");
        empty.className = "empty-chat";
        empty.textContent = "아직 상담 기록이 없습니다.";
        messages.appendChild(empty);
        return;
    }

    chronological.forEach((consultation) => appendConsultation(consultation));
}

function bindConsultationForm() {
    const form = document.getElementById("consultationForm");
    if (!form) {
        return;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (requestLocked) {
            return;
        }

        const questionInput = document.getElementById("question");
        const question = questionInput.value.trim();
        if (!question) {
            showError("질문을 입력해주세요.");
            return;
        }

        setSubmitting(true);
        clearError();
        clearEmptyChat();
        const pendingQuestionMessage = appendUserQuestion(question);
        const loadingMessage = appendLoadingMessage();
        scrollChatToBottom();

        try {
            const consultation = await apiFetch("/api/profiles/" + currentProfileId + "/consultations", {
                method: "POST",
                body: JSON.stringify({ question })
            });
            replaceLoadingMessageWithAnswer(loadingMessage, consultation);
            questionInput.value = "";
            scrollChatToBottom();
        } catch (error) {
            removeLoadingMessage(loadingMessage);
            removeLoadingMessage(pendingQuestionMessage);
            restoreEmptyChatIfNeeded();
            if ((error.message || "").includes("오늘의 AI 상담 요청 횟수")) {
                requestLocked = true;
                disableQuestionForm();
                showError("오늘의 AI 상담 요청 횟수를 초과했습니다. 내일 다시 이용해주세요.");
                return;
            }
            showError(error.message || "상담 요청 중 오류가 발생했습니다.");
        } finally {
            if (!requestLocked) {
                setSubmitting(false);
            }
        }
    });
}

function clearEmptyChat() {
    const empty = document.querySelector(".empty-chat");
    if (empty) {
        empty.remove();
    }
}

function restoreEmptyChatIfNeeded() {
    const messages = document.getElementById("chatMessages");
    if (messages.children.length > 0) {
        return;
    }

    const empty = document.createElement("div");
    empty.className = "empty-chat";
    empty.textContent = "아직 상담 기록이 없습니다.";
    messages.appendChild(empty);
}

function appendConsultation(consultation) {
    const messages = document.getElementById("chatMessages");
    messages.appendChild(createMessage("user", consultation.question, consultation.createdAt));
    messages.appendChild(createMessage("ai", consultation.answer, consultation.createdAt));
}

function appendUserQuestion(question) {
    const messages = document.getElementById("chatMessages");
    const questionMessage = createMessage("user", question, new Date().toISOString());
    messages.appendChild(questionMessage);
    return questionMessage;
}

function appendLoadingMessage() {
    const messages = document.getElementById("chatMessages");
    const loadingMessage = createMessage("ai", "답변을 생성하고 있습니다", new Date().toISOString());
    loadingMessage.classList.add("loading-message");
    messages.appendChild(loadingMessage);
    return loadingMessage;
}

function replaceLoadingMessageWithAnswer(loadingMessage, consultation) {
    const answerMessage = createMessage("ai", consultation.answer, consultation.createdAt);
    loadingMessage.replaceWith(answerMessage);
}

function removeLoadingMessage(loadingMessage) {
    if (loadingMessage) {
        loadingMessage.remove();
    }
}

function createMessage(type, content, createdAt) {
    const wrapper = document.createElement("article");
    wrapper.className = "chat-message " + (type === "user" ? "user-message" : "ai-message");

    const label = document.createElement("span");
    label.className = "chat-label";
    label.textContent = type === "user" ? "나" : "AI";

    const bubble = document.createElement("p");
    bubble.className = "chat-bubble";
    bubble.textContent = content || "";

    const time = document.createElement("time");
    time.className = "chat-time";
    time.textContent = formatDateTime(createdAt);

    wrapper.appendChild(label);
    wrapper.appendChild(bubble);
    wrapper.appendChild(time);
    return wrapper;
}

function bindProfileLink(profileId) {
    const link = document.getElementById("profileLink");
    if (link) {
        link.href = "/view/profiles/" + profileId;
    }
}

function renderRecommendedQuestionButtons(values) {
    const container = document.getElementById("recommendedQuestions");
    container.innerHTML = "";

    (values || []).forEach((value) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "question-chip";
        button.textContent = value;
        button.addEventListener("click", () => {
            const questionInput = document.getElementById("question");
            questionInput.value = value;
            questionInput.focus();
        });
        container.appendChild(button);
    });
}

function fillQuestionFromQuery() {
    const question = new URLSearchParams(window.location.search).get("question");
    if (!question) {
        return;
    }

    const questionInput = document.getElementById("question");
    questionInput.value = question;
    questionInput.focus();
}

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = value || "";
    }
}

function getConsultationProfileIdFromPath() {
    const segments = window.location.pathname.split("/").filter(Boolean);
    return segments[segments.length - 2];
}

function setSubmitting(isSubmitting) {
    const button = document.getElementById("submitQuestionButton");
    const questionInput = document.getElementById("question");
    button.disabled = isSubmitting;
    questionInput.disabled = isSubmitting;
    button.textContent = isSubmitting ? "요청 중" : "질문하기";
}

function disableQuestionForm() {
    const button = document.getElementById("submitQuestionButton");
    const questionInput = document.getElementById("question");
    button.disabled = true;
    questionInput.disabled = true;
    button.textContent = "요청 제한";
}

function scrollChatToBottom() {
    const messages = document.getElementById("chatMessages");
    messages.scrollTop = messages.scrollHeight;
}

function formatDateTime(value) {
    if (!value) {
        return "";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return date.toLocaleString("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });
}
