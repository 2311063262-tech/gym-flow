/**
 * Common utilities cho Auth Module với JWT
 */

const API_BASE = "http://localhost:8081/api";

/**
 * Lấy headers với JWT token từ localStorage.
 * @returns {Object} Headers object
 */
function getHeaders() {
    const token = localStorage.getItem("token");
    const headers = {
        "Content-Type": "application/json"
    };
    
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }
    
    return headers;
}

/**
 * Kiểm tra quyền truy cập. Nếu user không có role phù hợp, chuyển về trang login.
 * @param {...string} roles - Danh sách role được phép (ADMIN, TRAINER, MEMBER)
 */
function requireRole(...roles) {
    const currentUser = getCurrentUser();
    
    // Kiểm tra user có đăng nhập không
    if (!currentUser) {
        alert("Vui lòng đăng nhập để tiếp tục!");
        window.location.href = "index.html";
        return;
    }
    
    // Kiểm tra role
    if (!roles.includes(currentUser.role)) {
        alert("Bạn không có quyền truy cập trang này!");
        window.location.href = "index.html";
        return;
    }
}

/**
 * Lấy thông tin user hiện tại từ localStorage.
 * @returns {Object|null} User object hoặc null nếu chưa đăng nhập
 */
function getCurrentUser() {
    const userStr = localStorage.getItem("currentUser");
    if (!userStr) {
        return null;
    }
    try {
        return JSON.parse(userStr);
    } catch (e) {
        return null;
    }
}

/**
 * Lấy JWT token từ localStorage.
 * @returns {string|null} JWT token hoặc null
 */
function getToken() {
    return localStorage.getItem("token");
}

/**
 * Đăng xuất: xóa localStorage và chuyển về trang login.
 */
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("currentUser");
    window.location.href = "index.html";
}

/**
 * Copy token vào clipboard.
 */
function copyToken() {
    const token = document.getElementById("displayToken").value;
    navigator.clipboard.writeText(token).then(() => {
        alert("Token đã được copy!");
    });
}
