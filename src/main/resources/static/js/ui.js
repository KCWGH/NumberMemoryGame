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

        const authContainer = document.createElement('div');
        authContainer.id = 'authContainer';
        authContainer.className = 'auth-pill';

        const userInfoDisplay = document.createElement('span');
        userInfoDisplay.id = 'userInfoDisplay';

        const providerIcon = document.createElement('img');
        providerIcon.src = iconPath;
        providerIcon.alt = provider;
        providerIcon.className = 'provider-icon';

        const userNameText = document.createTextNode(` ${userName}님`);
        userInfoDisplay.appendChild(providerIcon);
        userInfoDisplay.appendChild(userNameText);

        const divider = document.createElement('span');
        divider.className = 'auth-divider';

        const logoutBtn = document.createElement('button');
        logoutBtn.id = 'logoutBtn';
        logoutBtn.textContent = '로그아웃';

        authContainer.appendChild(userInfoDisplay);
        authContainer.appendChild(divider);
        authContainer.appendChild(logoutBtn);


        elements.userStatusContainer.innerHTML = '';
        elements.userStatusContainer.appendChild(authContainer);

        logoutBtn.addEventListener('click', onLogout);

        userInfoDisplay.addEventListener('click', () => {
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

export function setLeaderboardLoading(isLoading) {
    const ol = elements.leaderboardList;
    if (isLoading) {
        ol.innerHTML = `
            <div class="leaderboard-loader">
                <div class="spinner"></div>
                <span>데이터를 불러오는 중...</span>
            </div>
        `;
    }
}

export function setLeaderboardCooldown(isInCooldown, remainingMs = 3000) {
    const tabs = document.querySelectorAll('.tab-btn');
    const remainingSec = (remainingMs / 1000).toFixed(1);

    tabs.forEach(tab => {
        if (isInCooldown) {
            tab.classList.add('cooldown');
            const existingMsg = tab.querySelector('.cooldown-msg');
            if (existingMsg) existingMsg.remove();

            const msg = document.createElement('span');
            msg.classList.add('cooldown-msg');
            msg.innerText = `${remainingSec}초 후에 가능합니다`;
            tab.appendChild(msg);
        } else {
            tab.classList.remove('cooldown');
            const msg = tab.querySelector('.cooldown-msg');
            if (msg) msg.remove();
        }
    });
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

        const userInfoWrapper = document.createElement('div');
        userInfoWrapper.className = 'user-info-wrapper';

        const rankNumber = document.createElement('span');
        rankNumber.className = 'rank-number';
        rankNumber.textContent = rank;

        const userNameText = document.createTextNode(` ${userName}`);
        userInfoWrapper.appendChild(rankNumber);
        userInfoWrapper.appendChild(userNameText);

        const scoreProviderWrapper = document.createElement('div');
        scoreProviderWrapper.className = 'score-provider-wrapper';

        const providerIcon = document.createElement('img');
        providerIcon.src = `/icons/logo_${provider}.svg`;
        providerIcon.alt = provider;
        providerIcon.className = 'leaderboard-provider-icon';

        const scoreSpan = document.createElement('span');
        scoreSpan.className = 'leaderboard-score';
        scoreSpan.textContent = `${scoreValue} 점`;

        scoreProviderWrapper.appendChild(providerIcon);
        scoreProviderWrapper.appendChild(scoreSpan);

        li.appendChild(userInfoWrapper);
        li.appendChild(scoreProviderWrapper);
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
        elements.leaderboardWrapper.style.display = 'flex';
        elements.gameWrapper.style.display = 'flex';
        elements.leaderboardWrapper.classList.add('is-visible');
        elements.startBtn.style.display = 'block';
    }
}
