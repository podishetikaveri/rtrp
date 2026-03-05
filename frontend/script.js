// Set this to your backend Render URL, e.g. "https://smart-queue-backend.onrender.com"
const API_BASE_URL = "https://new-smartqueuemanagement.onrender.com";

document.addEventListener("DOMContentLoaded", () => {
    // Custom Cursor Logic
    const cursor = document.querySelector('.cursor');
    const follower = document.querySelector('.cursor-follower');

    if (cursor && follower) {
        let mouseX = 0, mouseY = 0;
        let followerX = 0, followerY = 0;

        document.addEventListener('mousemove', (e) => {
            mouseX = e.clientX;
            mouseY = e.clientY;

            // Immediate cursor update
            cursor.style.left = `${mouseX}px`;
            cursor.style.top = `${mouseY}px`;
        });

        // Smooth follower animation
        function animateFollower() {
            followerX += (mouseX - followerX) * 0.15;
            followerY += (mouseY - followerY) * 0.15;

            follower.style.left = `${followerX}px`;
            follower.style.top = `${followerY}px`;

            requestAnimationFrame(animateFollower);
        }
        animateFollower();

        // Hover effects for interactive elements
        const interactiveElements = document.querySelectorAll('a, button, input');
        interactiveElements.forEach(el => {
            el.addEventListener('mouseenter', () => document.body.classList.add('cursor-hover'));
            el.addEventListener('mouseleave', () => document.body.classList.remove('cursor-hover'));
        });

        // Ensure new dynamic elements get the hover effect
        const observer = new MutationObserver((mutations) => {
            mutations.forEach(mutation => {
                mutation.addedNodes.forEach(node => {
                    if (node.nodeType === 1) { // Element node
                        if (node.matches('a, button, input')) {
                            node.addEventListener('mouseenter', () => document.body.classList.add('cursor-hover'));
                            node.addEventListener('mouseleave', () => document.body.classList.remove('cursor-hover'));
                        }
                        const children = node.querySelectorAll('a, button, input');
                        children.forEach(child => {
                            child.addEventListener('mouseenter', () => document.body.classList.add('cursor-hover'));
                            child.addEventListener('mouseleave', () => document.body.classList.remove('cursor-hover'));
                        });
                    }
                });
            });
        });
        observer.observe(document.body, { childList: true, subtree: true });
    }

    const bookingForm = document.getElementById("bookingForm");
    if (bookingForm) {
        bookingForm.addEventListener("submit", handleBookingSubmit);
        refreshQueue();
        setInterval(refreshQueue, 5000);

        const checkStatusBtn = document.getElementById("checkStatusBtn");
        if (checkStatusBtn) {
            checkStatusBtn.addEventListener("click", handleCheckStatus);
        }
    }

    const adminTable = document.getElementById("adminTable");
    if (adminTable) {
        loadAdminData();
        setInterval(loadAdminData, 5000);

        const adminSearchInput = document.getElementById("adminSearchInput");
        if (adminSearchInput) {
            adminSearchInput.addEventListener("input", filterAdminTable);
        }

        const filterPills = document.querySelectorAll(".filter-pill");
        filterPills.forEach(pill => {
            pill.addEventListener("click", () => {
                filterPills.forEach(p => p.classList.remove("active"));
                pill.classList.add("active");
                filterAdminTable();
            });
        });
    }

    init3DCardTilt();
    hidePreloader();
});

let allAdminData = [];

async function handleBookingSubmit(event) {
    event.preventDefault();

    const name = document.getElementById("name").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const isVip = document.getElementById("isVip") ? document.getElementById("isVip").checked : false;

    const resultDiv = document.getElementById("bookingResult");
    resultDiv.textContent = "Booking...";
    resultDiv.className = "result";

    try {
        const body = new URLSearchParams();
        body.append("name", name);
        body.append("phone", phone);
        body.append("isVip", isVip);

        const res = await fetch(`${API_BASE_URL}/api/book`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body
        });

        const data = await res.json();
        if (data.success) {
            resultDiv.classList.add("success");
            resultDiv.textContent = `Your token is ${data.token}. Your position in queue is ${data.position}.`;
            showToast(`Success! Your token is ${data.token}. Position: ${data.position}`, "success");
            document.getElementById("bookingForm").reset();
            refreshQueue();
        } else {
            resultDiv.classList.add("error");
            resultDiv.textContent = data.message || "Failed to book appointment.";
        }
    } catch (err) {
        console.error(err);
        resultDiv.classList.add("error");
        resultDiv.textContent = "Error connecting to server.";
    }
}

async function refreshQueue() {
    const currentServingEl = document.getElementById("currentServing");
    const queueBody = document.getElementById("queueBody");
    if (!currentServingEl || !queueBody) return;

    try {
        const res = await fetch(`${API_BASE_URL}/api/queue`);
        const data = await res.json();

        currentServingEl.textContent =
            data.currentServing ? `Currently serving token: ${data.currentServing}` :
                "No token is currently being served.";

        queueBody.innerHTML = "";
        data.waiting.forEach(item => {
            const tr = document.createElement("tr");
            const vipBadge = item.isVip ? `<span class="vip-badge">VIP</span>` : '';
            tr.innerHTML = `
                <td>${item.id}</td>
                <td>${item.name} ${vipBadge}</td>
                <td>${item.phone}</td>
                <td><span class="status-badge status-${item.status}">${item.status}</span></td>
            `;
            queueBody.appendChild(tr);
        });
    } catch (err) {
        console.error(err);
        currentServingEl.textContent = "Failed to load queue.";
    }
}

async function loadAdminData() {
    const adminBody = document.getElementById("adminBody");
    if (!adminBody) return;

    try {
        const res = await fetch(`${API_BASE_URL}/api/admin`);
        allAdminData = await res.json();
        filterAdminTable();
    } catch (err) {
        console.error(err);
    }
}

function filterAdminTable() {
    const adminBody = document.getElementById("adminBody");
    if (!adminBody) return;

    const searchInput = document.getElementById("adminSearchInput")?.value.toLowerCase() || "";
    const activeFilter = document.querySelector(".filter-pill.active")?.dataset.filter || "ALL";

    adminBody.innerHTML = "";

    const filteredData = allAdminData.filter(item => {
        const matchesSearch = (item.name || "").toLowerCase().includes(searchInput) ||
            (item.phone || "").toLowerCase().includes(searchInput);
        const matchesFilter = activeFilter === "ALL" || item.status === activeFilter;
        return matchesSearch && matchesFilter;
    });

    filteredData.forEach(item => {
        const tr = document.createElement("tr");

        const isWaiting = item.status === "WAITING";
        const isServing = item.status === "SERVING";
        const vipBadge = item.isVip ? `<span class="vip-badge">VIP</span>` : '';

        tr.innerHTML = `
            <td>${item.id}</td>
            <td>${item.name} ${vipBadge}</td>
            <td>${item.phone}</td>
            <td><span class="status-badge status-${item.status}">${item.status}</span></td>
            <td>
                <button ${isWaiting ? "" : "disabled"} onclick="updateStatus(${item.id}, 'SERVING')">
                    Mark Serving
                </button>
                <button ${isServing ? "" : "disabled"} onclick="updateStatus(${item.id}, 'COMPLETED')">
                    Mark Completed
                </button>
            </td>
        `;
        adminBody.appendChild(tr);
    });
}

async function updateStatus(id, action) {
    try {
        const body = new URLSearchParams();
        body.append("id", id);
        body.append("action", action);

        const res = await fetch(`${API_BASE_URL}/api/admin`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body
        });

        const data = await res.json();
        if (!data.success) {
            showToast(data.message || "Failed to update status.", "error");
        } else {
            showToast(`Token ${id} marked as ${action}.`, "success");
        }
        loadAdminData();
        refreshQueue();
    } catch (err) {
        console.error(err);
        showToast("Error updating status.", "error");
    }
}

// ---- NEW COOL FEATURES LOGIC ----

function hidePreloader() {
    const preloader = document.getElementById('preloader');
    if (preloader) {
        setTimeout(() => {
            preloader.classList.add('hidden');
        }, 800);
    }
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <span>${message}</span>
        </div>
    `;

    container.appendChild(toast);
    void toast.offsetWidth; // Trigger reflow
    toast.classList.add('show');

    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    }, 4000);
}

function init3DCardTilt() {
    const cards = document.querySelectorAll('.tilt-card');
    cards.forEach(card => {
        card.addEventListener('mousemove', e => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            const rotateX = ((y - centerY) / centerY) * -5;
            const rotateY = ((x - centerX) / centerX) * 5;

            card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateY(-4px)`;
        });

        card.addEventListener('mouseleave', () => {
            card.style.transform = `perspective(1000px) rotateX(0) rotateY(0) translateY(0)`;
        });
    });
}

async function handleCheckStatus() {
    const tokenInput = document.getElementById("checkTokenInput")?.value.trim();
    const resultDiv = document.getElementById("checkStatusResult");
    if (!tokenInput || !resultDiv) return;

    resultDiv.textContent = "Checking...";
    resultDiv.className = "result";

    try {
        const res = await fetch(`${API_BASE_URL}/api/queue`);
        const data = await res.json();

        let found = null;
        let position = 0;

        for (let i = 0; i < data.waiting.length; i++) {
            if (data.waiting[i].id.toString() === tokenInput) {
                found = data.waiting[i];
                position = i + 1;
                break;
            }
        }

        if (found) {
            const waitMins = position * 5;
            resultDiv.classList.add("success");
            resultDiv.textContent = `You are at position ${position} in the queue. Estimated wait: ~${waitMins} mins.`;
            showToast("Token status found!", "success");
        } else {
            if (data.currentServing && data.currentServing.toString() === tokenInput) {
                resultDiv.classList.add("success");
                resultDiv.textContent = `It is your turn! Token ${tokenInput} is currently being served.`;
                showToast("It's your turn!", "success");
            } else {
                resultDiv.classList.add("error");
                resultDiv.textContent = `Token ${tokenInput} not found in the waiting queue.`;
                showToast("Token not found.", "error");
            }
        }
    } catch (err) {
        console.error(err);
        resultDiv.classList.add("error");
        resultDiv.textContent = "Error checking status.";
        showToast("Error connecting to server.", "error");
    }
}
