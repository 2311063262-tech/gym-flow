(() => {
  "use strict";

  const API = "/api/auth";
  const SESSION_KEY = "gymflow.auth.session";
  let session;
  let currentUser;
  let currentPage = 0;
  let totalPages = 0;
  let searchTimer;
  let toastTimer;
  let dialogMode = "view";
  let selectedUserId;

  const byId = (id) => document.getElementById(id);
  const pages = {
    dashboard: byId("dashboard-page"),
    users: byId("users-page"),
    profile: byId("profile-page"),
  };
  const labels = { dashboard: "Tổng quan", users: "Tài khoản người dùng", profile: "Hồ sơ cá nhân" };

  function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (character) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;",
    })[character]);
  }

  function formatDate(value) {
    if (!value) return "Chưa đăng nhập";
    const date = new Date(value);
    return Number.isNaN(date.getTime())
      ? "—"
      : new Intl.DateTimeFormat("vi-VN", { dateStyle: "medium", timeStyle: "short" }).format(date);
  }

  function initials(user) {
    return (user.fullName || user.username || "G").trim().charAt(0).toUpperCase();
  }

  function roleClass(role) {
    return role === "ADMIN" ? "role-admin" : role === "STAFF" ? "role-staff" : "";
  }

  function setNotice(element, message, visible = true) {
    element.textContent = message;
    element.hidden = !visible;
  }

  function showToast(message) {
    const toast = byId("toast");
    toast.textContent = message;
    toast.classList.add("is-visible");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove("is-visible"), 3200);
  }

  async function api(path, options = {}) {
    const response = await fetch(`${API}${path}`, {
      ...options,
      headers: {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...(session?.token ? { Authorization: `${session.type || "Bearer"} ${session.token}` } : {}),
        ...(options.headers || {}),
      },
    });
    const text = await response.text();
    let body = null;
    if (text) {
      try {
        body = JSON.parse(text);
      } catch {
        throw new Error("Máy chủ trả về dữ liệu không hợp lệ.");
      }
    }
    if (!response.ok) {
      if (response.status === 401) {
        sessionStorage.removeItem(SESSION_KEY);
        window.location.replace("/");
      }
      throw new Error(body?.error?.message || `Yêu cầu thất bại (HTTP ${response.status}).`);
    }
    return body;
  }

  function setPage(page) {
    Object.entries(pages).forEach(([key, element]) => { element.hidden = key !== page; });
    byId("page-breadcrumb").textContent = labels[page];
    document.querySelectorAll(".admin-nav a[data-page]").forEach((link) => {
      link.classList.toggle("is-active", link.dataset.page === page);
    });
    if (page === "dashboard") loadDashboard();
    if (page === "users") loadUsers();
    if (page === "profile") loadProfile();
  }

  function routePage() {
    const path = window.location.pathname;
    return path.endsWith("/users") ? "users" : path.endsWith("/profile") ? "profile" : "dashboard";
  }

  async function loadDashboard() {
    const error = byId("dashboard-error");
    const retry = byId("retry-dashboard");
    setNotice(error, "", false);
    retry.hidden = true;
    try {
      const stats = await api("/users/stats");
      for (const key of ["total", "members", "trainers", "staff", "active", "inactive"]) {
        document.querySelector(`[data-stat="${key}"]`).textContent = new Intl.NumberFormat("vi-VN").format(stats[key] ?? 0);
      }
      const users = stats.recentUsers || [];
      byId("recent-users").innerHTML = users.length
        ? users.map((user) => `
          <tr>
            <td><div class="person-cell"><span class="table-avatar">${escapeHtml(initials(user))}</span><span class="person-copy"><strong>${escapeHtml(user.fullName || user.username)}</strong><span>${escapeHtml(user.username)}</span></span></div></td>
            <td>${escapeHtml(user.email)}</td>
            <td><span class="role-badge ${roleClass(user.role)}">${escapeHtml(user.role)}</span></td>
            <td><span class="status-badge ${user.active ? "" : "inactive"}">${user.active ? "Đang bật" : "Đã tắt"}</span></td>
            <td>${escapeHtml(formatDate(user.createdAt))}</td>
          </tr>`).join("")
        : '<tr><td colspan="5" class="empty-cell">Chưa có tài khoản nào.</td></tr>';
    } catch (errorValue) {
      setNotice(error, `Không tải được số liệu dashboard: ${errorValue.message}`);
      retry.hidden = false;
      byId("recent-users").innerHTML = '<tr><td colspan="5" class="empty-cell">Không thể tải tài khoản mới.</td></tr>';
    }
  }

  async function loadUsers() {
    const body = byId("users-table");
    const error = byId("users-error");
    setNotice(error, "", false);
    body.innerHTML = '<tr><td colspan="7" class="loading-cell">Đang tải dữ liệu…</td></tr>';
    const params = new URLSearchParams({
      page: String(currentPage),
      size: "10",
      sort: "createdAt",
      status: byId("status-filter").value,
    });
    const search = byId("search-input").value.trim();
    const role = byId("role-filter").value;
    if (search) params.set("search", search);
    if (role) params.set("role", role);

    try {
      const result = await api(`/users?${params}`);
      totalPages = result.totalPages || 0;
      const users = result.content || [];
      byId("result-count").textContent = `${new Intl.NumberFormat("vi-VN").format(result.totalElements || 0)} tài khoản`;
      byId("page-info").textContent = totalPages
        ? `Trang ${result.number + 1} / ${totalPages}`
        : "Không có dữ liệu";
      byId("prev-page").disabled = currentPage <= 0;
      byId("next-page").disabled = totalPages === 0 || currentPage >= totalPages - 1;
      body.innerHTML = users.length
        ? users.map(renderUserRow).join("")
        : '<tr><td colspan="7" class="empty-cell">Không tìm thấy tài khoản phù hợp.</td></tr>';
    } catch (errorValue) {
      body.innerHTML = '<tr><td colspan="7" class="empty-cell">Không tải được danh sách.</td></tr>';
      byId("result-count").textContent = "Tải dữ liệu thất bại";
      byId("page-info").textContent = "";
      setNotice(error, `Không tải được danh sách tài khoản: ${errorValue.message}`);
    }
  }

  function renderUserRow(user) {
    const canManage = user.id !== currentUser.id && ["TRAINER", "MEMBER"].includes(user.role);
    const statusAction = user.active ? "deactivate" : "activate";
    return `<tr>
      <td><div class="person-cell"><span class="table-avatar">${escapeHtml(initials(user))}</span><span class="person-copy"><strong>${escapeHtml(user.fullName || user.username)}</strong><span>${escapeHtml(user.username)}</span></span></div></td>
      <td>${escapeHtml(user.email)}</td>
      <td><span class="role-badge ${roleClass(user.role)}">${escapeHtml(user.role)}</span></td>
      <td><span class="status-badge ${user.active ? "" : "inactive"}">${user.active ? "Đang bật" : "Đã tắt"}</span></td>
      <td>${escapeHtml(formatDate(user.createdAt))}</td>
      <td>${escapeHtml(formatDate(user.lastLoginAt))}</td>
      <td><div class="actions-cell">
        <button class="row-action" data-action="view" data-id="${user.id}" type="button">Xem</button>
        ${canManage ? `<button class="row-action" data-action="edit" data-id="${user.id}" type="button">Sửa</button>
        <button class="row-action" data-action="${statusAction}" data-id="${user.id}" data-name="${escapeHtml(user.username)}" type="button">${user.active ? "Tắt" : "Bật"}</button>
        <button class="row-action danger" data-action="delete" data-id="${user.id}" data-name="${escapeHtml(user.username)}" type="button">Xóa</button>` : ""}
      </div></td>
    </tr>`;
  }

  async function openUserDialog(id, mode) {
    dialogMode = mode;
    selectedUserId = id || null;
    const dialog = byId("user-dialog");
    const form = byId("user-form");
    const fields = byId("dialog-fields");
    const title = byId("dialog-title");
    const error = byId("dialog-error");
    const actions = byId("dialog-actions");
    const password = form.elements.password;
    setNotice(error, "", false);
    form.reset();

    if (mode === "create") {
      title.textContent = "Tạo tài khoản";
      password.required = true;
      password.disabled = false;
      byId("role-field").hidden = false;
      fields.querySelectorAll("input, select").forEach((field) => { field.disabled = false; });
      actions.hidden = false;
      byId("save-user").textContent = "Tạo tài khoản";
      dialog.showModal();
      return;
    }

    title.textContent = mode === "edit" ? "Chỉnh sửa tài khoản" : "Chi tiết tài khoản";
    try {
      const user = await api(`/users/${id}`);
      for (const field of ["username", "email", "fullName", "phone"]) {
        form.elements[field].value = user[field] || "";
      }
      form.elements.role.value = user.role === "TRAINER" ? "TRAINER" : "MEMBER";
      password.value = "";
      password.required = false;
      password.disabled = true;
      byId("role-field").hidden = mode === "view";
      fields.querySelectorAll("input, select").forEach((field) => {
        field.disabled = mode === "view" || field === password;
      });
      actions.hidden = mode === "view";
      byId("save-user").textContent = "Lưu thay đổi";
      dialog.showModal();
    } catch (errorValue) {
      showToast(`Không tải được chi tiết: ${errorValue.message}`);
    }
  }

  function closeDialog() {
    byId("user-dialog").close();
  }

  async function saveUser(event) {
    event.preventDefault();
    const form = byId("user-form");
    const button = byId("save-user");
    const error = byId("dialog-error");
    setNotice(error, "", false);
    if (!form.reportValidity()) return;
    const data = new FormData(form);
    const payload = {
      username: String(data.get("username")).trim(),
      email: String(data.get("email")).trim(),
      fullName: String(data.get("fullName") || "").trim() || null,
      phone: String(data.get("phone") || "").trim() || null,
    };
    if (dialogMode === "create") {
      payload.role = data.get("role");
      payload.password = data.get("password");
    } else {
      payload.role = data.get("role");
      const passwordValue = String(data.get("password") || "");
      if (passwordValue) payload.password = passwordValue;
    }
    button.disabled = true;
    try {
      if (dialogMode === "create") {
        await api("/users", { method: "POST", body: JSON.stringify(payload) });
        showToast("Đã tạo tài khoản thành công.");
      } else {
        await api(`/users/${selectedUserId}`, { method: "PUT", body: JSON.stringify(payload) });
        showToast("Đã cập nhật tài khoản.");
      }
      closeDialog();
      await Promise.all([loadUsers(), loadDashboard()]);
    } catch (errorValue) {
      setNotice(error, errorValue.message);
    } finally {
      button.disabled = false;
    }
  }

  async function handleTableAction(event) {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const { action, id, name } = button.dataset;
    if (action === "view" || action === "edit") {
      await openUserDialog(id, action);
      return;
    }
    if (action === "activate" || action === "deactivate") {
      const active = action === "activate";
      const verb = active ? "kích hoạt" : "vô hiệu hóa";
      if (!window.confirm(`Bạn có chắc muốn ${verb} tài khoản ${name}?`)) return;
      try {
        await api(`/users/${id}/status`, { method: "PATCH", body: JSON.stringify({ active }) });
        showToast(`Đã ${active ? "kích hoạt" : "vô hiệu hóa"} tài khoản ${name}.`);
        await Promise.all([loadUsers(), loadDashboard()]);
      } catch (errorValue) {
        showToast(`Không thể cập nhật trạng thái: ${errorValue.message}`);
      }
      return;
    }
    if (action === "delete" && window.confirm(`Xóa mềm tài khoản ${name}? Tài khoản sẽ không thể đăng nhập.`)) {
      try {
        await api(`/users/${id}`, { method: "DELETE" });
        showToast(`Đã xóa mềm tài khoản ${name}.`);
        await Promise.all([loadUsers(), loadDashboard()]);
      } catch (errorValue) {
        showToast(`Không thể xóa tài khoản: ${errorValue.message}`);
      }
    }
  }

  async function loadProfile() {
    try {
      currentUser = await api("/me");
      byId("profile-name").textContent = currentUser.fullName || currentUser.username;
      byId("profile-email").textContent = currentUser.email;
      byId("profile-avatar").textContent = initials(currentUser);
      byId("profile-username").textContent = currentUser.username;
      byId("profile-id").textContent = currentUser.id;
      byId("profile-created").textContent = formatDate(currentUser.createdAt);
      byId("profile-last-login").textContent = formatDate(currentUser.lastLoginAt);
      const form = byId("profile-form");
      for (const field of ["username", "email", "fullName", "phone"]) {
        form.elements[field].value = currentUser[field] || "";
      }
      byId("sidebar-name").textContent = currentUser.fullName || currentUser.username;
      byId("sidebar-email").textContent = currentUser.email;
      byId("sidebar-avatar").textContent = initials(currentUser);
    } catch (errorValue) {
      setNotice(byId("profile-error"), `Không tải được hồ sơ: ${errorValue.message}`);
    }
  }

  async function saveProfile(event) {
    event.preventDefault();
    const form = event.currentTarget;
    if (!form.reportValidity()) return;
    const data = new FormData(form);
    const payload = Object.fromEntries(["username", "email", "fullName", "phone"].map((field) => [
      field,
      String(data.get(field) || "").trim() || null,
    ]));
    try {
      await api(`/users/${currentUser.id}`, { method: "PUT", body: JSON.stringify(payload) });
      showToast("Đã cập nhật hồ sơ.");
      await loadProfile();
    } catch (errorValue) {
      setNotice(byId("profile-error"), `Không lưu được hồ sơ: ${errorValue.message}`);
    }
  }

  async function logout() {
    const button = byId("logout-button");
    button.disabled = true;
    try {
      await api("/logout", { method: "POST" });
    } catch (errorValue) {
      showToast(`Không thể thu hồi phiên: ${errorValue.message}`);
      button.disabled = false;
      return;
    }
    sessionStorage.removeItem(SESSION_KEY);
    window.location.replace("/");
  }

  function bindEvents() {
    byId("retry-dashboard").addEventListener("click", loadDashboard);
    byId("refresh-users").addEventListener("click", loadUsers);
    byId("users-table").addEventListener("click", handleTableAction);
    byId("create-user-button").addEventListener("click", () => openUserDialog(null, "create"));
    byId("user-form").addEventListener("submit", saveUser);
    byId("close-dialog").addEventListener("click", closeDialog);
    byId("cancel-dialog").addEventListener("click", closeDialog);
    byId("prev-page").addEventListener("click", () => { currentPage = Math.max(0, currentPage - 1); loadUsers(); });
    byId("next-page").addEventListener("click", () => { if (currentPage + 1 < totalPages) { currentPage += 1; loadUsers(); } });
    byId("search-input").addEventListener("input", () => {
      clearTimeout(searchTimer);
      searchTimer = setTimeout(() => { currentPage = 0; loadUsers(); }, 280);
    });
    byId("role-filter").addEventListener("change", () => { currentPage = 0; loadUsers(); });
    byId("status-filter").addEventListener("change", () => { currentPage = 0; loadUsers(); });
    byId("profile-form").addEventListener("submit", saveProfile);
    byId("logout-button").addEventListener("click", logout);
    byId("mobile-menu").addEventListener("click", () => byId("admin-sidebar").classList.toggle("is-open"));
    document.querySelectorAll(".admin-nav a").forEach((link) => {
      link.addEventListener("click", () => byId("admin-sidebar").classList.remove("is-open"));
    });
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
      currentUser = await api("/me");
      if (currentUser.role !== "ADMIN") {
        window.location.replace("/forbidden");
        return;
      }
      byId("sidebar-name").textContent = currentUser.fullName || currentUser.username;
      byId("sidebar-email").textContent = currentUser.email;
      byId("sidebar-avatar").textContent = initials(currentUser);
      byId("today-date").textContent = new Intl.DateTimeFormat("vi-VN", { dateStyle: "full" }).format(new Date());
      bindEvents();
      setPage(routePage());
    } catch (errorValue) {
      sessionStorage.removeItem(SESSION_KEY);
      showToast(`Không thể xác minh tài khoản: ${errorValue.message}`);
      setTimeout(() => window.location.replace("/"), 1200);
    }
  }

  start();
})();
