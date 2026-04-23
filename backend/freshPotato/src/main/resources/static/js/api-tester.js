const storageKey = "freshpotato_api_tester_tokens";

const elements = {
    username: document.getElementById("username"),
    password: document.getElementById("password"),
    method: document.getElementById("method"),
    endpoint: document.getElementById("endpoint"),
    endpointSelect: document.getElementById("endpointSelect"),
    requestBody: document.getElementById("requestBody"),
    loginBtn: document.getElementById("loginBtn"),
    logoutBtn: document.getElementById("logoutBtn"),
    sendBtn: document.getElementById("sendBtn"),
    formatBodyBtn: document.getElementById("formatBodyBtn"),
    clearBtn: document.getElementById("clearBtn"),
    responseMeta: document.getElementById("responseMeta"),
    responseOutput: document.getElementById("responseOutput"),
    tokenState: document.getElementById("tokenState")
};

function getTokens() {
    const raw = sessionStorage.getItem(storageKey);
    return raw ? JSON.parse(raw) : null;
}

function setTokens(tokens) {
    sessionStorage.setItem(storageKey, JSON.stringify(tokens));
    refreshTokenBadge();
}

function clearTokens() {
    sessionStorage.removeItem(storageKey);
    refreshTokenBadge();
}

function refreshTokenBadge() {
    const tokens = getTokens();
    if (tokens && tokens.jwtToken && tokens.refreshToken) {
        elements.tokenState.textContent = "Logged in";
        elements.tokenState.className = "badge text-bg-success";
        return;
    }
    elements.tokenState.textContent = "Not logged in";
    elements.tokenState.className = "badge text-bg-secondary";
}

function toPrettyJson(value) {
    return JSON.stringify(value, null, 2);
}

async function parseResponseBody(response) {
    const contentType = response.headers.get("content-type") || "";
    const text = await response.text();

    if (!text) {
        return { body: "", isJson: false };
    }

    if (contentType.includes("application/json")) {
        try {
            return { body: toPrettyJson(JSON.parse(text)), isJson: true };
        } catch (_) {
            return { body: text, isJson: false };
        }
    }

    return { body: text, isJson: false };
}

function renderResponse(meta, payloadText) {
    elements.responseMeta.innerHTML = meta;
    elements.responseOutput.textContent = payloadText || "";
}

function populateEndpointDropdown(options) {
    const select = elements.endpointSelect;
    select.innerHTML = "";

    const placeholder = document.createElement("option");
    placeholder.value = "";
    placeholder.textContent = "Select an endpoint...";
    select.appendChild(placeholder);

    options.forEach((entry) => {
        const option = document.createElement("option");
        option.value = entry.method + " " + entry.path;
        option.textContent = entry.method + " " + entry.path;
        select.appendChild(option);
    });
}

async function loadEndpointsFromApiDocs() {
    const response = await fetch("/v3/api-docs", {
        headers: {
            "Accept": "application/json"
        }
    });

    if (!response.ok) {
        throw new Error("Could not load /v3/api-docs");
    }

    const docs = await response.json();
    const paths = docs.paths || {};
    const httpMethods = new Set(["get", "post", "put", "patch", "delete", "head", "options"]);
    const entries = [];

    Object.entries(paths).forEach(([path, operations]) => {
        Object.keys(operations || {}).forEach((method) => {
            if (!httpMethods.has(method.toLowerCase())) {
                return;
            }

            entries.push({
                method: method.toUpperCase(),
                path
            });
        });
    });

    entries.sort((a, b) => {
        if (a.path === b.path) {
            return a.method.localeCompare(b.method);
        }
        return a.path.localeCompare(b.path);
    });

    populateEndpointDropdown(entries);
}

async function refreshTokens() {
    const tokens = getTokens();
    if (!tokens || !tokens.refreshToken) {
        throw new Error("No refresh token available");
    }

    const response = await fetch("/api/auth/refresh", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ refreshToken: tokens.refreshToken })
    });

    if (!response.ok) {
        clearTokens();
        throw new Error("Refresh token is invalid or expired");
    }

    const refreshed = await response.json();
    setTokens(refreshed);
    return refreshed;
}

async function sendWithAutoRefresh(input, init) {
    const tokens = getTokens();
    const headers = new Headers(init.headers || {});

    if (tokens && tokens.jwtToken) {
        headers.set("Authorization", "Bearer " + tokens.jwtToken);
    }

    const firstResponse = await fetch(input, { ...init, headers });

    if (firstResponse.status !== 401 || input === "/api/auth/refresh") {
        return firstResponse;
    }

    await refreshTokens();
    const refreshedTokens = getTokens();
    const retryHeaders = new Headers(init.headers || {});
    retryHeaders.set("Authorization", "Bearer " + refreshedTokens.jwtToken);

    return fetch(input, { ...init, headers: retryHeaders });
}

function buildRequestInit(method, bodyText) {
    const init = {
        method,
        headers: {
            "Accept": "application/json"
        }
    };

    if (method !== "GET") {
        if (bodyText.trim()) {
            try {
                const parsed = JSON.parse(bodyText);
                init.headers["Content-Type"] = "application/json";
                init.body = JSON.stringify(parsed);
            } catch (error) {
                throw new Error("Body is not valid JSON");
            }
        }
    }

    return init;
}

async function login() {
    const username = elements.username.value.trim();
    const password = elements.password.value;

    if (!username || !password) {
        throw new Error("Username and password are required");
    }

    const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ username, password })
    });

    const parsed = await parseResponseBody(response);

    if (!response.ok) {
        throw new Error(parsed.body || "Login failed");
    }

    const tokens = JSON.parse(parsed.body);
    setTokens(tokens);
    renderResponse("<strong>Login successful</strong>", parsed.body);
}

async function logout() {
    const tokens = getTokens();

    if (!tokens || !tokens.refreshToken) {
        clearTokens();
        renderResponse("<strong>No active session</strong>", "");
        return;
    }

    await fetch("/api/auth/logout", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ refreshToken: tokens.refreshToken })
    });

    clearTokens();
    renderResponse("<strong>Logged out</strong>", "");
}

async function sendRequest() {
    const method = elements.method.value;
    const endpoint = elements.endpoint.value.trim();
    const bodyText = elements.requestBody.value;

    if (!endpoint.startsWith("/")) {
        throw new Error("Endpoint must start with '/'");
    }

    const init = buildRequestInit(method, bodyText);
    const startedAt = performance.now();
    const response = await sendWithAutoRefresh(endpoint, init);
    const elapsedMs = Math.round(performance.now() - startedAt);
    const parsed = await parseResponseBody(response);

    const meta = [
        `<strong>Status:</strong> ${response.status} ${response.statusText}`,
        `<strong>Time:</strong> ${elapsedMs} ms`,
        `<strong>Content-Type:</strong> ${response.headers.get("content-type") || "(none)"}`
    ].join(" | ");

    renderResponse(meta, parsed.body);
}

function formatRequestBody() {
    const bodyText = elements.requestBody.value.trim();
    if (!bodyText) {
        return;
    }

    const parsed = JSON.parse(bodyText);
    elements.requestBody.value = toPrettyJson(parsed);
}

function setupEventListeners() {
    elements.loginBtn.addEventListener("click", async () => {
        try {
            await login();
        } catch (error) {
            renderResponse("<strong>Login error</strong>", String(error.message || error));
        }
    });

    elements.logoutBtn.addEventListener("click", async () => {
        try {
            await logout();
        } catch (error) {
            renderResponse("<strong>Logout error</strong>", String(error.message || error));
        }
    });

    elements.sendBtn.addEventListener("click", async () => {
        try {
            await sendRequest();
        } catch (error) {
            renderResponse("<strong>Request error</strong>", String(error.message || error));
        }
    });

    elements.formatBodyBtn.addEventListener("click", () => {
        try {
            formatRequestBody();
        } catch (error) {
            renderResponse("<strong>Format error</strong>", String(error.message || error));
        }
    });

    elements.clearBtn.addEventListener("click", () => {
        renderResponse("No response yet.", "{}");
    });

    elements.endpointSelect.addEventListener("change", () => {
        const selected = elements.endpointSelect.value;
        if (!selected) {
            return;
        }

        const firstSpace = selected.indexOf(" ");
        if (firstSpace <= 0) {
            return;
        }

        const method = selected.substring(0, firstSpace);
        const path = selected.substring(firstSpace + 1);

        elements.method.value = method;
        elements.endpoint.value = path;
    });
}

async function initialize() {
    refreshTokenBadge();
    setupEventListeners();

    try {
        await loadEndpointsFromApiDocs();
    } catch (error) {
        elements.endpointSelect.innerHTML = "";
        const option = document.createElement("option");
        option.value = "";
        option.textContent = "Could not load /v3/api-docs";
        elements.endpointSelect.appendChild(option);
        renderResponse("<strong>OpenAPI load warning</strong>", String(error.message || error));
    }
}

initialize();

