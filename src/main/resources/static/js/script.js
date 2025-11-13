let currentStage = 1;
let numbersToFind, nextNumberToClick, timeLeft, gameInterval, totalScore = 0;
let isGameActive = false;
let mobileLayout = false;

const scoreEl = document.getElementById('score');
const stageEl = document.getElementById('stage');
const timerEl = document.getElementById('timerDisplay'); 
const gameWrapper = document.getElementById('gameWrapper');
const startBtn = document.getElementById('startBtn');
const blockGridContainer = document.getElementById('blockGridContainer');
const leaderboardWrapper = document.getElementById('leaderboardWrapper');
const themeToggle = document.getElementById('theme-toggle');
const leaderboardToggleBtn = document.getElementById('leaderboard-toggle-btn');
const themeColorMeta = document.getElementById('theme-color-meta');
const modalOverlay = document.getElementById('modalOverlay');
const modalTitle = document.getElementById('modalTitle');
const modalMessage = document.getElementById('modalMessage');
const modalConfirmBtn = document.getElementById('modalConfirmBtn');
const closeBtn = document.querySelector('.close-btn');
const mainContent = document.getElementById('mainContent');
const restartBtn = document.getElementById('restartBtn');

function checkLayoutMode() {
    mobileLayout = window.innerWidth < 900;
    mainContent.classList.toggle('mobile-layout', mobileLayout);

    if (mobileLayout) {
        leaderboardToggleBtn.style.display = 'flex';
        if (leaderboardWrapper.classList.contains('is-visible') && !isGameActive) {
            restartBtn.style.display = 'block';
        } else {
            restartBtn.style.display = 'none';
        }
        leaderboardToggleBtn.innerText = leaderboardWrapper.classList.contains('is-visible') ? '❌' : '🏆';
    } else {
        leaderboardToggleBtn.style.display = 'none';
        leaderboardWrapper.classList.add('is-visible');
        restartBtn.style.display = 'none';
        gameWrapper.style.display = 'flex';
        leaderboardWrapper.style.display = 'block';
    }
}

leaderboardToggleBtn.onclick = () => {
    if (!mobileLayout) return;

    const isVisible = leaderboardWrapper.classList.toggle('is-visible');
    leaderboardToggleBtn.innerText = isVisible ? '❌' : '🏆';
    
    gameWrapper.style.display = isVisible ? 'none' : 'flex'; 
    leaderboardWrapper.style.display = isVisible ? 'block' : 'none';
    
    restartBtn.style.display = (isVisible && !isGameActive) ? 'block' : 'none';
};

if (restartBtn) {
    restartBtn.onclick = () => {
        if (mobileLayout) {
            leaderboardWrapper.classList.remove('is-visible');
            leaderboardWrapper.style.display = 'none';
            gameWrapper.style.display = 'flex';
            leaderboardToggleBtn.innerText = '🏆';
            restartBtn.style.display = 'none';
            startBtn.style.display = 'block';
            gameWrapper.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    };
}

window.addEventListener('resize', checkLayoutMode);
document.addEventListener('DOMContentLoaded', checkLayoutMode);

function applyTheme(theme) {
    document.body.classList.toggle('dark-mode', theme === 'dark');
    localStorage.setItem('theme', theme);

    const themeColor = theme === 'dark' ? '#1a202c' : '#f0f4f8';
    if (themeColorMeta) {
        themeColorMeta.setAttribute('content', themeColor);
    }
}

const savedTheme = localStorage.getItem('theme');
if (savedTheme) {
    applyTheme(savedTheme);
} else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    applyTheme('dark');
} else {
    applyTheme('light');
}

themeToggle.onclick = () => {
    const currentTheme = document.body.classList.contains('dark-mode') ? 'dark' : 'light';
    applyTheme(currentTheme === 'light' ? 'dark' : 'light');
};

function showModal(title, message, callback) {
    modalTitle.innerText = title;
    modalMessage.innerText = message;
    
    modalConfirmBtn.onclick = () => {
        modalOverlay.classList.remove('show');
        if (callback) callback();
    };

    const closeModalOnly = (e) => {
        if (e.target === modalOverlay || e.currentTarget === closeBtn) {
            modalOverlay.classList.remove('show');
            modalOverlay.removeEventListener('click', closeModalOnly);
            closeBtn.onclick = null;
        }
    };

    closeBtn.onclick = closeModalOnly;
    modalOverlay.onclick = closeModalOnly;
    
    modalOverlay.classList.add('show');
}

startBtn.onclick = () => {
    if (!isGameActive) {
        startBtn.style.display = 'none'; 
        startStage(1);
        
        if (mobileLayout) {
            leaderboardWrapper.classList.remove('is-visible');
            gameWrapper.style.display = 'flex';
            leaderboardToggleBtn.innerText = '🏆';
            restartBtn.style.display = 'none';
        }
    }
};

function startStage(stage) {
    clearInterval(gameInterval);
    isGameActive = true;
    currentStage = stage;
    stageEl.innerText = `스테이지: ${currentStage}`;
    scoreEl.innerText = `점수: ${totalScore}`;
    blockGridContainer.innerHTML = '';

    const numToRemember = currentStage + 2;
    numbersToFind = shuffleArray([...Array(numToRemember).keys()].map(i => i + 1));
    nextNumberToClick = 1;
    timeLeft = 10 + (currentStage * 0.5);
    timerEl.innerText = `남은 시간: ${timeLeft.toFixed(1)}s`;

    const wrapperHeight = gameWrapper.clientHeight;
    const wrapperWidth = gameWrapper.clientWidth; 
    
    const infoContainerHeight = document.getElementById('infoContainer').offsetHeight;
    const timerDisplayHeight = timerEl.offsetHeight + 30;

    const availableHeight = wrapperHeight - infoContainerHeight - timerDisplayHeight - 20; 
    const availableWidth = wrapperWidth - 40; 
    
    const cellSize = 65; 
    const maxCols = Math.floor(availableWidth / cellSize);
    const maxRows = Math.floor(availableHeight / cellSize);

    const actualMaxCols = Math.max(1, maxCols);
    const actualMaxRows = Math.max(1, maxRows);

    let gridLayout = generateConnectedBlock(numToRemember, actualMaxRows, actualMaxCols);

    if (!gridLayout) { 
        const numBlocks = numToRemember;
        let cols = Math.min(numBlocks, actualMaxCols);
        let rows = Math.ceil(numBlocks / cols);
        
        while(rows > actualMaxRows && cols > 1) { 
            cols--;
            rows = Math.ceil(numBlocks / cols);
        }
        if (cols === 0) cols = 1;

        gridLayout = [];
        for (let i = 0; i < numBlocks; i++) {
            gridLayout.push({ row: Math.floor(i / cols), col: i % cols });
        }
    }

    const actualCols = Math.max(...gridLayout.map(p => p.col)) + 1;
    blockGridContainer.style.gridTemplateColumns = `repeat(${actualCols}, 60px)`;

    gridLayout.forEach(pos => {
        const cell = document.createElement('div');
        cell.classList.add('cell', 'revealed'); 
        const number = numbersToFind.shift();
        cell.dataset.number = number;
        cell.innerText = number;
        cell.style.gridRowStart = pos.row + 1;
        cell.style.gridColumnStart = pos.col + 1;

        cell.onclick = () => handleClick(cell);
        blockGridContainer.appendChild(cell);
    });

    setTimeout(() => {
        blockGridContainer.querySelectorAll('.cell').forEach(c => {
            c.innerText = "";
            c.classList.remove('revealed'); 
        });
        startTimer();
    }, 2000);
}

function startTimer() {
    gameInterval = setInterval(() => {
        timeLeft -= 0.1;
        if (timeLeft <= 0) {
            endGame();
            return;
        }
        timerEl.innerText = `남은 시간: ${timeLeft.toFixed(1)}s`;
    }, 100);
}

function handleClick(cell) {
    if (!isGameActive || cell.classList.contains('revealed') || cell.classList.contains('correct') || cell.classList.contains('wrong')) {
        return;
    }

    const num = parseInt(cell.dataset.number);
    if (num === nextNumberToClick) {
        cell.classList.add('correct');
        cell.innerText = num;
        nextNumberToClick++;
        totalScore++;
        scoreEl.innerText = `점수: ${totalScore}`;

        if (nextNumberToClick > (currentStage + 2)) {
            clearInterval(gameInterval);
            isGameActive = false;
            setTimeout(() => startStage(currentStage + 1), 800);
        }
    } else {
        cell.classList.add('wrong');
        cell.innerText = num;
        endGame();
    }
}

function endGame() {
    clearInterval(gameInterval);
    isGameActive = false;
    blockGridContainer.querySelectorAll('.cell').forEach(c => {
        c.onclick = null;
        if (!c.classList.contains('correct') && !c.classList.contains('wrong')) {
            c.classList.add('revealed'); 
            c.innerText = c.dataset.number;
        }
    });

    setTimeout(() => {
        if (totalScore > 0) {
            submitScore(totalScore); 
        } else {
            fetchLeaderboard();
        }
        
        totalScore = 0;
        currentStage = 1;
        stageEl.innerText = `스테이지: 1`;
        scoreEl.innerText = `점수: 0`;
        timerEl.innerText = `남은 시간: 10.0s`;
        
        blockGridContainer.innerHTML = '';
        
        if (mobileLayout) {
            gameWrapper.style.display = 'none';
            leaderboardWrapper.classList.add('is-visible');
            leaderboardWrapper.style.display = 'block';
            leaderboardToggleBtn.innerText = '❌';
            restartBtn.style.display = 'block';
            startBtn.style.display = 'block';
            leaderboardWrapper.scrollIntoView({ behavior: 'smooth', block: 'start' });
        } else {
            startBtn.style.display = 'block';
        }
    }, 500);
}

function submitScore(score) {
    fetch(`/api/score?score=${score}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include'
    })
    .then(response => {
        if (response.ok) {
            console.log('Score submitted successfully.');
            showModal('점수 기록 성공 🎉', `총 점수 ${score}점을 성공적으로 기록했습니다.`, fetchLeaderboard);
        } else if (response.status === 401) {
            showModal('점수 기록 실패', '점수를 기록하려면 먼저 Google로 로그인해야 합니다.', fetchLeaderboard);
        } else if (response.status === 409 || response.status === 429 || response.status === 500) {
            console.log('Duplicate score submission detected or server error.');
            showModal('점수 기록 생략', `${score}점은 이미 기록된 점수입니다.\n 중복된 점수는 기록되지 않습니다.`, fetchLeaderboard);
        } else {
            console.error('Score submission failed with status:', response.status);
            showModal('점수 기록 오류', `점수 기록 중 알 수 없는 오류가 발생했습니다 (Code: ${response.status}).`, fetchLeaderboard);
        }
    })
    .catch(err => {
        console.error('Score submission network error:', err);
        showModal('네트워크 오류', '점수 기록 중 네트워크 오류가 발생했습니다.', fetchLeaderboard);
    });
}

function fetchLeaderboard() {
    fetch('/api/leaderboard',   {
        credentials: 'include' 
    })
    .then(res => {
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        return res.json();
    })
    .then(data => {
        const ol = document.getElementById('leaderboard');
        ol.innerHTML = "";
        
        if (data.length === 0) {
            ol.innerHTML = "<li>No scores yet!</li>";
            return;
        }
        
        data.forEach(s => {
            const scoreValue = s.scoreValue; 
            const userName = s.user ? s.user : 'Unknown User';
            ol.innerHTML += `<li>${userName} <span>${scoreValue} 점</span></li>`;
        });
    })
    .catch(err => {
        console.error('Failed to fetch leaderboard:', err);
        const ol = document.getElementById('leaderboard');
        ol.innerHTML = '<li>Error loading leaderboard.</li>';
    });
}

function shuffleArray(array) {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
    }
    return array;
}

function generateConnectedBlock(numBlocks, maxRows, maxCols) {
    const grid = Array(maxRows).fill(0).map(() => Array(maxCols).fill(false));
    const blockPositions = [];
    let startRow = Math.floor(Math.random() * Math.max(1, maxRows - 2)) + 1;
    let startCol = Math.floor(Math.random() * Math.max(1, maxCols - 2)) + 1;

    if (maxRows <= 0 || maxCols <= 0) return null;

    if (startRow >= maxRows) startRow = maxRows - 1;
    if (startCol >= maxCols) startCol = maxCols - 1;
    if (startRow < 0) startRow = 0;
    if (startCol < 0) startCol = 0;

    grid[startRow][startCol] = true;
    blockPositions.push({ row: startRow, col: startCol });

    const directions = [
        { dr: -1, dc: 0 }, { dr: 1, dc: 0 },
        { dr: 0, dc: -1 }, { dr: 0, dc: 1 }
    ];

    let attempts = 0;
    const maxAttempts = numBlocks * 10;

    while (blockPositions.length < numBlocks && attempts < maxAttempts) {
        attempts++;
        const randBlockIndex = Math.floor(Math.random() * blockPositions.length);
        const { row: currentRow, col: currentCol } = blockPositions[randBlockIndex];

        const shuffledDirections = shuffleArray([...directions]);

        for (const dir of shuffledDirections) {
            const newRow = currentRow + dir.dr;
            const newCol = currentCol + dir.dc;

            if (newRow >= 0 && newRow < maxRows &&
                newCol >= 0 && newCol < maxCols &&
                !grid[newRow][newCol]) {

                grid[newRow][newCol] = true;
                blockPositions.push({ row: newRow, col: newCol });
                break;
            }
        }
    }

    if (blockPositions.length < numBlocks) {
        return null;
    }

    const minRow = Math.min(...blockPositions.map(p => p.row));
    const minCol = Math.min(...blockPositions.map(p => p.col));

    return blockPositions.map(p => ({
        row: p.row - minRow,
        col: p.col - minCol
    }));
}


fetchLeaderboard();

if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/service-worker.js')
        .then(registration => {
            console.log('Service Worker registered with scope:', registration.scope);
        })
        .catch(error => {
            console.error('Service Worker registration failed:', error);
        });
    });
}