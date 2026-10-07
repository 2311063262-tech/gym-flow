(() => {
  "use strict";
  const SESSION_KEY = "gymflow.auth.session";
  let session;
  let currentUser;

  const byId = (id) => document.getElementById(id);

  function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (character) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;",
    })[character]);
  }

  function formatDate(value) {
    if (!value) return "Chưa đăng nhập";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "—" : new Intl.DateTimeFormat("vi-VN", { dateStyle: "medium", timeStyle: "short" }).format(date);
  }

  async function api(path, options = {}) {
    const response = await fetch(`/api/auth${path}`, {
      ...options,
      headers: {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...(session?.token ? { Authorization: `${session.type || "Bearer"} ${session.token}` } : {}),
      },
    });
    const text = await response.text();
    const data = text ? JSON.parse(text) : null;
    if (!response.ok) throw new Error(data?.error?.message || `Yêu cầu thất bại (HTTP ${response.status}).`);
    return data;
  }

  function showError(message) {
    const element = byId("portal-error");
    element.textContent = message;
    element.hidden = false;
  }

  function routeForRole(role) {
    return { ADMIN: "/admin/dashboard", STAFF: "/staff", TRAINER: "/trainer", MEMBER: "/member" }[role] || "/forbidden";
  }

  function renderProfile(user) {
    currentUser = user;
    byId("portal-role").textContent = user.role;
    byId("portal-name").textContent = user.fullName || user.username;
    byId("portal-email").textContent = user.email;
    byId("portal-avatar").textContent = (user.fullName || user.username || "G").trim().charAt(0).toUpperCase();
    byId("portal-username").textContent = user.username;
    byId("portal-id").textContent = user.id;
    byId("portal-created").textContent = formatDate(user.createdAt);
    byId("portal-last-login").textContent = formatDate(user.lastLoginAt);
    const form = byId("profile-form");
    for (const field of ["username", "email", "fullName", "phone"]) form.elements[field].value = user[field] || "";
  }

  async function loadStaffDirectory() {
    byId("staff-directory").hidden = false;
    const body = byId("staff-users");
    try {
      const result = await api("/users?page=0&size=20&sort=username");
      body.innerHTML = result.content?.length
        ? result.content.map((user) => `<tr><td>${escapeHtml(user.username)}</td><td>${escapeHtml(user.email)}</td><td>${escapeHtml(user.role)}</td><td>${user.active ? "Đang bật" : "Đã tắt"}</td></tr>`).join("")
        : '<tr><td colspan="4" class="empty-cell">Không có tài khoản.</td></tr>';
    } catch (error) {
      body.innerHTML = `<tr><td colspan="4" class="empty-cell">${escapeHtml(error.message)}</td></tr>`;
    }
  }

  async function start() {
    try {
      session = JSON.parse(sessionStorage.getItem(SESSION_KEY) || "null");
    } catch {
      session = null;
    }
    if (!session?.token) {
      window.location.replace("/");
      return;
    }
    try {
      const user = await api("/me");
      const expectedRole = window.location.pathname.includes("/staff") ? "STAFF"
        : window.location.pathname.includes("/trainer") ? "TRAINER" : "MEMBER";
      if (user.role !== expectedRole) {
        window.location.replace(routeForRole(user.role));
        return;
      }
      byId("portal-title").textContent = `Xin chào, ${user.fullName || user.username}.`;
      byId("portal-description").textContent = expectedRole === "STAFF"
        ? "Khu vực nhân sự với quyền xem tài khoản."
        : "Quản lý thông tin cá nhân của bạn.";
      renderProfile(user);
      if (expectedRole === "STAFF") {
        byId("profile-form").hidden = true;
        loadStaffDirectory();
      }
    } catch (error) {
      sessionStorage.removeItem(SESSION_KEY);
      showError(`Không thể xác minh phiên đăng nhập: ${error.message}`);
    }
  }

  byId("profile-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity() || !currentUser) return;
    const data = new FormData(form);
    const payload = Object.fromEntries(["username", "email", "fullName", "phone"].map((field) => [
      field, String(data.get(field) || "").trim() || null,
    ]));
    try {
      const result = await api(`/users/${currentUser.id}`, { method: "PUT", body: JSON.stringify(payload) });
      renderProfile(result);
      showError("Đã cập nhật hồ sơ thành công.");
      byId("portal-error").classList.add("is-success");
    } catch (error) {
      showError(`Không cập nhật được hồ sơ: ${error.message}`);
    }
  });

  byId("logout-button").addEventListener("click", async (event) => {
    event.currentTarget.disabled = true;
    try {
      await api("/logout", { method: "POST" });
      sessionStorage.removeItem(SESSION_KEY);
      window.location.replace("/");
    } catch (error) {
      showError(`Không thể đăng xuất: ${error.message}`);
      event.currentTarget.disabled = false;
    }
  });

  start();
})();
