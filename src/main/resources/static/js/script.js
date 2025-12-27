import * as api from './api.js';
import * as ui from './ui.js';
import * as audio from './audio.js';
import * as utils from './utils.js';
import { SOCIAL_LOGIN_URLS, THEMES, BREAKPOINT_MOBILE } from './constants.js';

let gameState = {
    currentStage: 1,
    totalScore: 0,
    isGameActive: false,
    nextNumberToClick: 1,
    timeLeft: 0,
    gameInterval: null,
    currentSessionId: null,
    clicksInCurrentStage: 0,
    isAuthenticated: false,
    userProfile: null,
    numbersToFind: []
};

// --- Core Game Logic ---

function startStage(stage) {
    clearInterval(gameState.gameInterval);
    gameState.isGameActive = true;
    gameState.currentStage = stage;
    gameState.clicksInCurrentStage = 0;
    gameState.nextNumberToClick = 1;
    gameState.timeLeft = 10 + (stage * 0.5);

    ui.updateStageUI(gameState.currentStage, gameState.totalScore);
    ui.updateTimerUI(gameState.timeLeft);
    ui.clearGrid();

    const numToRemember = stage + 2;
    const { availableRows, availableCols } = calculateGridConstraints();

    let gridLayout = utils.generateConnectedBlock(numToRemember, availableRows, availableCols);
    if (!gridLayout) {
        // Fallback grid
        const cols = Math.min(numToRemember, availableCols);
        gridLayout = Array.from({ length: numToRemember }, (_, i) => ({
            row: Math.floor(i / cols),
            col: i % cols
        }));
    }

    gameState.numbersToFind = utils.shuffleArray([...Array(numToRemember).keys()].map(i => i + 1));

    const maxCols = Math.max(...gridLayout.map(p => p.col)) + 1;
    ui.setGridColumns(maxCols);

    gridLayout.forEach((pos, index) => {
        const cell = document.createElement('div');
        cell.classList.add('cell', 'revealed');
        const number = gameState.numbersToFind[index];
        cell.dataset.number = number;
        cell.innerText = number;
        cell.style.gridRowStart = pos.row + 1;
        cell.style.gridColumnStart = pos.col + 1;

        cell.addEventListener('click', () => handleCellClick(cell));
        ui.elements.blockGridContainer.appendChild(cell);
    });

    setTimeout(() => {
        if (!gameState.isGameActive) return;
        ui.elements.blockGridContainer.querySelectorAll('.cell').forEach(c => {
            c.innerText = "";
            c.classList.remove('revealed');
        });
        startTimer();
    }, 2000);
}

function calculateGridConstraints() {
    const availableHeight = ui.elements.gameWrapper.clientHeight - 150; // Approximated
    const availableWidth = ui.elements.gameWrapper.clientWidth - 40;
    const cellSize = 65;
    return {
        availableRows: Math.max(1, Math.floor(availableHeight / cellSize)),
        availableCols: Math.max(1, Math.floor(availableWidth / cellSize))
    };
}

function startTimer() {
    gameState.gameInterval = setInterval(() => {
        gameState.timeLeft -= 0.1;
        if (gameState.timeLeft <= 0) {
            endGame();
            return;
        }
        ui.updateTimerUI(gameState.timeLeft);
    }, 100);
}

function handleCellClick(cell) {
    if (!gameState.isGameActive || cell.classList.contains('revealed') ||
        cell.classList.contains('correct') || cell.classList.contains('wrong')) return;

    const num = parseInt(cell.dataset.number);
    if (num === gameState.nextNumberToClick) {
        audio.playSuccessSound(gameState.nextNumberToClick - 1);
        cell.classList.add('correct');
        cell.innerText = num;
        gameState.nextNumberToClick++;
        gameState.totalScore++;
        gameState.clicksInCurrentStage++;
        ui.updateStageUI(gameState.currentStage, gameState.totalScore);

        if (gameState.nextNumberToClick > (gameState.currentStage + 2)) {
            clearInterval(gameState.gameInterval);
            gameState.isGameActive = false;
            setTimeout(() => startStage(gameState.currentStage + 1), 800);
        }
    } else {
        cell.classList.add('wrong');
        cell.innerText = num;
        endGame();
    }
}

async function endGame() {
    clearInterval(gameState.gameInterval);
    gameState.isGameActive = false;

    ui.elements.blockGridContainer.querySelectorAll('.cell').forEach(c => {
        if (!c.classList.contains('correct') && !c.classList.contains('wrong')) {
            c.classList.add('revealed');
            c.innerText = c.dataset.number;
        }
    });

    setTimeout(async () => {
        if (gameState.totalScore > 0 && gameState.currentSessionId) {
            await handleGameSubmission();
        } else {
            refreshLeaderboard();
        }

        resetGameState();
        ui.elements.startBtn.style.display = 'block';

        if (window.innerWidth < BREAKPOINT_MOBILE) {
            toggleMobileLeaderboard(true);
        }
    }, 500);
}

function resetGameState() {
    gameState.totalScore = 0;
    gameState.currentStage = 1;
    gameState.currentSessionId = null;
    gameState.clicksInCurrentStage = 0;
    ui.updateStageUI(1, 0);
    ui.updateTimerUI(10.0);
    ui.clearGrid();
}

// --- Auth & API Interaction ---

async function handleGameSubmission() {
    if (!gameState.isAuthenticated) {
        ui.showModal('로그인 필요', '점수를 기록하려면 로그인해 주세요.', () => {
            ui.showLoginOptions(provider => {
                window.location.href = SOCIAL_LOGIN_URLS[provider];
            });
        });
        refreshLeaderboard();
        return;
    }

    const result = await api.submitGameEnd({
        sessionId: gameState.currentSessionId,
        stage: gameState.currentStage,
        clicksInCurrentStage: gameState.clicksInCurrentStage
    });

    if (result.success) {
        ui.showModal('게임 종료', '점수가 성공적으로 기록되었습니다.', refreshLeaderboard);
    } else {
        const errorMsg = result.message.includes('cheating') ? '부정행위가 감지되었습니다.' : `오류: ${result.message}`;
        ui.showModal('점수 기록 실패', errorMsg, refreshLeaderboard);
    }
}

async function refreshLeaderboard(type = 'all') {
    try {
        const data = await api.fetchLeaderboardData(type);
        ui.updateLeaderboardUI(data);
    } catch (err) {
        if (err.status === 401 && type === 'my') {
            ui.showModal('로그인 필요', '나의 기록을 보려면 로그인이 필요합니다.', () => {
                ui.showLoginOptions(provider => window.location.href = SOCIAL_LOGIN_URLS[provider]);
            });
        }
    }
}

// --- Event Listeners ---

function setupEventListeners() {
    ui.elements.startBtn.addEventListener('click', onStartGame);

    ui.elements.themeToggle.addEventListener('click', () => {
        const currentTheme = document.body.classList.contains('dark-mode') ? THEMES.DARK : THEMES.LIGHT;
        ui.applyTheme(currentTheme === THEMES.LIGHT ? THEMES.DARK : THEMES.LIGHT);
    });

    ui.elements.leaderboardToggleBtn.addEventListener('click', () => toggleMobileLeaderboard());

    ui.elements.restartBtn.addEventListener('click', () => {
        toggleMobileLeaderboard(false);
        ui.elements.startBtn.style.display = 'block';
    });

    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            refreshLeaderboard(btn.dataset.tab);
        });
    });

    ui.elements.closeBtn.addEventListener('click', ui.closeModal);
    ui.elements.modalOverlay.addEventListener('click', (e) => {
        if (e.target === ui.elements.modalOverlay) ui.closeModal();
    });

    window.addEventListener('resize', ui.handleResponsive);
}

async function onStartGame() {
    if (gameState.isGameActive) return;

    audio.initAudioContext();
    ui.elements.startBtn.disabled = true;
    ui.elements.startBtn.innerText = '시작 중...';

    try {
        const data = await api.startGameSession();
        gameState.currentSessionId = data.sessionId;
        ui.elements.startBtn.style.display = 'none';
        startStage(1);
    } catch (err) {
        ui.showModal('게임 시작 실패', err.message);
    } finally {
        ui.elements.startBtn.disabled = false;
        ui.elements.startBtn.innerText = '게임 시작';
    }
}

function toggleMobileLeaderboard(forceShow) {
    const isVisible = forceShow !== undefined ? forceShow : !ui.elements.leaderboardWrapper.classList.contains('is-visible');

    ui.elements.leaderboardWrapper.classList.toggle('is-visible', isVisible);
    ui.elements.leaderboardToggleBtn.innerText = isVisible ? '❌' : '🏆';
    ui.elements.gameWrapper.style.display = isVisible ? 'none' : 'flex';
    ui.elements.leaderboardWrapper.style.display = isVisible ? 'flex' : 'none';
    ui.elements.restartBtn.style.display = isVisible ? 'block' : 'none';
}

// --- Initialization ---

async function init() {
    setupEventListeners();
    ui.handleResponsive();

    const savedTheme = localStorage.getItem('theme') ||
        (window.matchMedia('(prefers-color-scheme: dark)').matches ? THEMES.DARK : THEMES.LIGHT);
    ui.applyTheme(savedTheme);

    const user = await api.checkLoginStatus();
    gameState.isAuthenticated = user.authenticated;
    gameState.userProfile = user;

    ui.updateAuthUI(user,
        () => ui.showLoginOptions(provider => window.location.href = SOCIAL_LOGIN_URLS[provider]),
        async () => {
            if (await api.handleLogout()) {
                gameState.isAuthenticated = false;
                ui.updateAuthUI(null, () => ui.showLoginOptions(p => window.location.href = SOCIAL_LOGIN_URLS[p]));
                ui.showModal('로그아웃', '성공적으로 로그아웃되었습니다.');
                refreshLeaderboard();
            }
        }
    );

    refreshLeaderboard();
}

document.addEventListener('DOMContentLoaded', init);

// Service Worker Registration
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/service-worker.js').catch(console.error);
    });
}