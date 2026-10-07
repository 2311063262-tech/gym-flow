const API_BASE = "/api/auth";
const SESSION_KEY = "gymflow.auth.session";
const authView = document.querySelector("#auth-view");
const accountView = document.querySelector("#account-view");
const form = document.querySelector("#auth-form");
const usernameField = document.querySelector("#username-field");
const usernameInput = document.querySelector("#username");
const emailInput = document.querySelector("#email");
const passwordInput = document.querySelector("#password");
const passwordHint = document.querySelector("#password-hint");
const authNotice = document.querySelector("#auth-notice");
const accountNotice = document.querySelector("#account-notice");
const submitButton = document.querySelector("#submit-button");
const submitLabel = document.querySelector("#submit-label");
const loginTab = document.querySelector("#login-tab");
const registerTab = document.querySelector("#register-tab");
let mode = "login";
let session = readSession();

function readSession() {
  try {
    const saved = sessionStorage.getItem(SESSION_KEY);
    return saved ? JSON.parse(saved) : null;
  } catch (error) {
    sessionStorage.removeItem(SESSION_KEY);
    return null;
  }
}

function saveSession(value) {
  session = value;
  sessionStorage.setItem(SESSION_KEY, JSON.stringify(value));
}

function showNotice(element, message, success = false) {
  element.textContent = message;
  element.classList.toggle("is-success", success);
  element.hidden = false;
}

function hideNotice(element) {
  element.textContent = "";
  element.classList.remove("is-success");
  element.hidden = true;
}

function setMode(nextMode) {
  mode = nextMode;
  const registering = mode === "register";
  loginTab.classList.toggle("is-active", !registering);
  registerTab.classList.toggle("is-active", registering);
  loginTab.setAttribute("aria-selected", String(!registering));
  registerTab.setAttribute("aria-selected", String(registering));
  usernameField.hidden = !registering;
  usernameInput.required = registering;
  passwordHint.hidden = !registering;
  passwordInput.autocomplete = registering ? "new-password" : "current-password";
  if (registering) {
    passwordInput.minLength = 6;
  } else {
    passwordInput.removeAttribute("minlength");
  }
  submitLabel.textContent = registering ? "Tạo tài khoản" : "Đăng nhập";
  document.querySelector("#auth-title").innerHTML = registering
    ? 'Bắt đầu hành<br><span class="heading-light">trình của bạn.</span>'
    : 'Chào mừng<br><span class="heading-light">bạn quay trở lại.</span>';
  document.querySelector("#auth-description").textContent = registering
    ? "Tạo tài khoản để bắt đầu hành trình cùng GymFlow."
    : "Nhập thông tin để tiếp tục hành trình.";
  hideNotice(authNotice);
  form.reset();
}

function renderAccount(user) {
  authView.hidden = true;
  accountView.hidden = false;
  document.querySelector("#profile-name").textContent = user.username || "bạn";
  document.querySelector("#profile-avatar").textContent = (user.username || "G").trim().charAt(0).toUpperCase();
  document.querySelector("#profile-email").textContent = user.email || "—";
  document.querySelector("#profile-role").textContent = user.role || "MEMBER";
  document.querySelector("#profile-id").textContent = user.userId ?? user.id ?? "—";
  const createdAt = user.createdAt ? new Date(user.createdAt) : null;
  document.querySelector("#profile-created").textContent = createdAt && !Number.isNaN(createdAt.getTime())
    ? new Intl.DateTimeFormat("vi-VN", { dateStyle: "medium" }).format(createdAt)
    : "Đang cập nhật";
}

function renderAuth() {
  accountView.hidden = true;
  authView.hidden = false;
}

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(session?.token ? { Authorization: `${session.type || "Bearer"} ${session.token}` } : {}),
      ...(options.headers || {}),
    },
  });
  const bodyText = await response.text();
  let body = null;

  if (bodyText) {
    try {
      body = JSON.parse(bodyText);
    } catch (error) {
      throw new Error("Máy chủ trả về dữ liệu không đúng định dạng.");
    }
  }

  if (!response.ok) {
    throw new Error(body?.error?.message || `Yêu cầu thất bại (HTTP ${response.status}).`);
  }

  return body;
}

function routeForRole(role) {
  return {
    ADMIN: "/admin/dashboard",
    STAFF: "/staff",
    TRAINER: "/trainer",
    MEMBER: "/member",
  }[role] || "/forbidden";
}

loginTab.addEventListener("click", () => setMode("login"));
registerTab.addEventListener("click", () => setMode("register"));

document.querySelector("#password-toggle").addEventListener("click", (event) => {
  const button = event.currentTarget;
  const isVisible = passwordInput.type === "text";
  passwordInput.type = isVisible ? "password" : "text";
  button.setAttribute("aria-label", isVisible ? "Hiện mật khẩu" : "Ẩn mật khẩu");
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideNotice(authNotice);

  if (!form.reportValidity()) {
    return;
  }

  const payload = {
    email: emailInput.value.trim(),
    password: passwordInput.value,
  };

  if (mode === "register") {
    payload.username = usernameInput.value.trim();
    if (payload.username.length < 3 || payload.username.length > 50) {
      showNotice(authNotice, "Tên người dùng phải có từ 3 đến 50 ký tự.");
      usernameInput.focus();
      return;
    }
  }

  submitButton.disabled = true;
  submitLabel.textContent = mode === "register" ? "Đang tạo tài khoản..." : "Đang đăng nhập...";

  try {
    const result = await request(`/${mode}`, {
      method: "POST",
      body: JSON.stringify(payload),
    });
    saveSession(result);
    const profile = await request("/me");
    saveSession({ ...result, ...profile, token: result.token, userId: result.userId });
    window.location.assign(routeForRole(profile.role));
  } catch (error) {
    showNotice(authNotice, error.message || "Không thể kết nối đến máy chủ. Vui lòng thử lại.");
  } finally {
    submitButton.disabled = false;
    submitLabel.textContent = mode === "register" ? "Tạo tài khoản" : "Đăng nhập";
  }
});

document.querySelector("#logout-button").addEventListener("click", async () => {
  const button = document.querySelector("#logout-button");
  button.disabled = true;
  hideNotice(accountNotice);

  try {
    await request("/logout", {
      method: "POST",
      headers: session?.token ? { Authorization: `${session.type || "Bearer"} ${session.token}` } : {},
    });
    sessionStorage.removeItem(SESSION_KEY);
    session = null;
    setMode("login");
    renderAuth();
  } catch (error) {
    showNotice(accountNotice, `Không thể đăng xuất qua máy chủ: ${error.message}`);
  } finally {
    button.disabled = false;
  }
});

if (session?.token && session?.userId) {
  request("/me").then((profile) => {
    session = { ...session, ...profile, token: session.token, userId: session.userId };
    saveSession(session);
    window.location.replace(routeForRole(profile.role));
  }).catch((error) => {
    sessionStorage.removeItem(SESSION_KEY);
    session = null;
    renderAuth();
    showNotice(authNotice, `Phiên đăng nhập không còn hợp lệ: ${error.message}`);
  });
}
