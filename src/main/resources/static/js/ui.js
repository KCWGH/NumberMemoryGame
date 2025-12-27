import { BREAKPOINT_MOBILE, THEMES } from './constants.js';

export const elements = {
    score: document.getElementById('score'),
    stage: document.getElementById('stage'),
    timer: document.getElementById('timerDisplay'),
    gameWrapper: document.getElementById('gameWrapper'),
    startBtn: document.getElementById('startBtn'),
    blockGridContainer: document.getElementById('blockGridContainer'),
    leaderboardWrapper: document.getElementById('leaderboardWrapper'),
    themeToggle: document.getElementById('theme-toggle'),
    leaderboardToggleBtn: document.getElementById('leaderboard-toggle-btn'),
    themeColorMeta: document.getElementById('theme-color-meta'),
    modalOverlay: document.getElementById('modalOverlay'),
    modalTitle: document.getElementById('modalTitle'),
    modalMessage: document.getElementById('modalMessage'),
    modalConfirmBtn: document.getElementById('modalConfirmBtn'),
    closeBtn: document.querySelector('.close-btn'),
    mainContent: document.getElementById('mainContent'),
    restartBtn: document.getElementById('restartBtn'),
    userStatusContainer: document.getElementById('userStatusContainer'),
    loginModalContent: document.getElementById('loginModalContent'),
    socialLoginContainer: document.getElementById('socialLoginContainer'),
    leaderboardList: document.getElementById('leaderboard')
};

export function updateStageUI(stage, score) {
    elements.stage.innerText = `스테이지: ${stage}`;
    elements.score.innerText = `점수: ${score}`;
}

export function updateTimerUI(timeLeft) {
    elements.timer.innerText = `남은 시간: ${timeLeft.toFixed(1)}s`;
}

export function setGridColumns(cols) {
    elements.blockGridContainer.style.gridTemplateColumns = `repeat(${cols}, auto)`;
}

export function clearGrid() {
    elements.blockGridContainer.innerHTML = '';
}

export function showModal(title, message, callback) {
    resetModal();
    elements.modalTitle.innerText = title;
    elements.modalMessage.innerText = message;
    elements.modalMessage.style.display = 'block';

    const onConfirm = () => {
        elements.modalOverlay.classList.remove('show');
        if (callback) callback();
        elements.modalConfirmBtn.removeEventListener('click', onConfirm);
    };
    elements.modalConfirmBtn.addEventListener('click', onConfirm);
    elements.modalOverlay.classList.add('show');
}

export function showLoginOptions(onProviderSelect) {
    resetModal();
    elements.modalTitle.innerText = "소셜 로그인";
    elements.modalMessage.innerText = "로그인하여 점수를 기록하세요.";
    elements.modalMessage.style.display = 'block';
    elements.modalConfirmBtn.parentElement.style.display = 'none';
    elements.loginModalContent.style.display = 'block';
    elements.modalOverlay.classList.add('show');

    // Remove old listeners by cloning
    const newContainer = elements.socialLoginContainer.cloneNode(true);
    elements.socialLoginContainer.parentNode.replaceChild(newContainer, elements.socialLoginContainer);
    elements.socialLoginContainer = newContainer;

    elements.socialLoginContainer.addEventListener('click', (e) => {
        const btn = e.target.closest('.social-btn');
        if (btn) {
            onProviderSelect(btn.dataset.provider);
        }
    });
}

function resetModal() {
    elements.loginModalContent.style.display = 'none';
    elements.modalMessage.style.display = 'none';
    elements.modalConfirmBtn.parentElement.style.display = 'block';
}

export function closeModal() {
    elements.modalOverlay.classList.remove('show');
}

export function updateAuthUI(user, onLogin, onLogout) {
    elements.userStatusContainer.innerHTML = '';
    if (user && user.authenticated) {
        const userName = user.id || '사용자';
        const provider = user.provider ? user.provider.toLowerCase() : 'unknown';
        let iconPath = `/icons/logo_${provider}.svg`;

        elements.userStatusContainer.innerHTML = `
            <div id="authContainer" class="auth-pill">
                <span id="userInfoDisplay">
                    <img src="${iconPath}" alt="${provider}" class="provider-icon"> ${userName}님
                </span>
                <span class="auth-divider"></span>
                <button id="logoutBtn">로그아웃</button>
            </div>
        `;

        const userInfo = document.getElementById('userInfoDisplay');
        const logoutBtn = document.getElementById('logoutBtn');

        // Handle logout button click
        logoutBtn.addEventListener('click', onLogout);

        // Handle username click (Actionable on Mobile)
        userInfo.addEventListener('click', () => {
            const isMobile = window.innerWidth < BREAKPOINT_MOBILE;
            if (isMobile) {
                showModal("로그아웃", "로그아웃 하시겠습니까?", onLogout);
            }
        });
    } else {
        elements.userStatusContainer.innerHTML = `<button id="loginBtn">로그인</button>`;
        document.getElementById('loginBtn').addEventListener('click', onLogin);
    }
}

export function updateLeaderboardUI(data) {
    const ol = elements.leaderboardList;
    ol.innerHTML = "";
    if (data.length === 0) {
        ol.innerHTML = "<li class='empty-state'>기록이 없습니다!</li>";
        return;
    }
    data.forEach((s, index) => {
        const scoreValue = s.scoreValue || s.score || 0;
        const userName = s.id || 'Unknown User';
        const provider = (s.provider || s.loginProvider || 'unknown').toLowerCase();
        const rank = index + 1;

        const li = document.createElement('li');
        li.classList.add(`rank-${rank}`);
        li.innerHTML = `
            <div class="user-info-wrapper">
                <span class="rank-number">${rank}</span>
                ${userName}
            </div>
            <div class="score-provider-wrapper">
                <img src="/icons/logo_${provider}.svg" alt="${provider}" class="leaderboard-provider-icon">
                <span class="leaderboard-score">${scoreValue} 점</span>
            </div>
        `;
        ol.appendChild(li);
    });
}

export function applyTheme(theme) {
    document.body.classList.toggle('dark-mode', theme === THEMES.DARK);
    localStorage.setItem('theme', theme);
    const themeColor = theme === THEMES.DARK ? '#011612' : '#f0fdfa';
    if (elements.themeColorMeta) {
        elements.themeColorMeta.setAttribute('content', themeColor);
    }
}

export function handleResponsive() {
    const isMobile = window.innerWidth < BREAKPOINT_MOBILE;
    elements.mainContent.classList.toggle('mobile-layout', isMobile);

    if (isMobile) {
        elements.leaderboardToggleBtn.style.display = 'flex';
    } else {
        elements.leaderboardToggleBtn.style.display = 'none';
        // Reset styles for PC version
        elements.leaderboardWrapper.style.display = 'flex';
        elements.gameWrapper.style.display = 'flex';
        elements.leaderboardWrapper.classList.add('is-visible');
        elements.startBtn.style.display = 'block';
    }
}
