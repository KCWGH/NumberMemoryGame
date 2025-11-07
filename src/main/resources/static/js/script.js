let currentStage = 1;
let numbersToFind, nextNumberToClick, timeLeft, gameInterval, totalScore = 0;
let isGameActive = false;

const scoreEl = document.getElementById('score');
const stageEl = document.getElementById('stage');
const timerEl = document.getElementById('timerDisplay'); 
const gameWrapper = document.getElementById('gameWrapper');
const startBtn = document.getElementById('startBtn');
const blockGridContainer = document.getElementById('blockGridContainer');
const leaderboardWrapper = document.getElementById('leaderboardWrapper');
const themeToggle = document.getElementById('theme-toggle');
const leaderboardToggleBtn = document.getElementById('leaderboard-toggle-btn');

const isMobile = window.matchMedia('(max-width: 899px)').matches;

// =========================================================
// 모바일/PC 레이아웃 초기 설정 및 리더보드 토글
// =========================================================

if (isMobile) {
    leaderboardToggleBtn.style.display = 'flex'; 

    leaderboardToggleBtn.onclick = () => {
        leaderboardWrapper.classList.toggle('is-visible');
        leaderboardToggleBtn.innerText = leaderboardWrapper.classList.contains('is-visible') ? '❌' : '🏆';
    };
} else {
    leaderboardToggleBtn.style.display = 'none';
    leaderboardWrapper.classList.add('is-visible'); 
}


// =========================================================
// 테마 토글 로직
// =========================================================
function applyTheme(theme) {
    document.body.classList.toggle('dark-mode', theme === 'dark');
    localStorage.setItem('theme', theme);
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

// =========================================================
// 이하 게임 및 서버 통신 로직
// =========================================================

startBtn.onclick = () => {
    if (!isGameActive) {
        // Start 버튼은 게임 시작 시 자동으로 숨겨집니다.
        startBtn.style.display = 'none'; 
        startStage(1);
        if (isMobile) {
            leaderboardWrapper.classList.remove('is-visible'); 
            leaderboardToggleBtn.innerText = '🏆';
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
    const timerDisplayHeight = timerEl.offsetHeight + 30; // 타이머 높이 + 하단 여백

    // 블록 그리드가 사용 가능한 높이는 infoContainer와 timerDisplay 공간을 제외한 영역입니다.
    const availableHeight = wrapperHeight - infoContainerHeight - timerDisplayHeight; 
    const availableWidth = wrapperWidth - 40; 
    
    // 블록 크기 계산 (셀 60px + 간격 5px)
    const cellSize = 60 + 5;
    const maxCols = Math.floor(availableWidth / cellSize);
    const maxRows = Math.floor(availableHeight / cellSize);

    let gridLayout = generateConnectedBlock(numToRemember, maxRows, maxCols);

    if (!gridLayout) { 
        const numBlocks = numToRemember;
        let cols = Math.min(numBlocks, maxCols);
        let rows = Math.ceil(numBlocks / cols);
        while(rows > maxRows && cols > 1) { 
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

        alert(`Game Over! Total Score: ${totalScore}`);
        totalScore = 0;
        stageEl.innerText = `스테이지: 1`;
        scoreEl.innerText = `점수: 0`;
        timerEl.innerText = `남은 시간: 10.0s`;
        startBtn.style.display = 'block'; // Start 버튼 다시 표시
        blockGridContainer.innerHTML = '';
        
        if (isMobile) {
            leaderboardWrapper.classList.add('is-visible');
            leaderboardToggleBtn.innerText = '❌';
            leaderboardWrapper.scrollIntoView({ behavior: 'smooth', block: 'start' });
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
            return fetchLeaderboard();
        } else if (response.status === 401) {
            alert('Score submission failed: Please log in with Google first.');
        } else {
             console.error('Score submission failed with status:', response.status);
             fetchLeaderboard(); 
        }
    })
    .catch(err => {
        console.error('Score submission network error:', err);
        alert('Network error during score submission.');
        fetchLeaderboard(); 
    });
}

function fetchLeaderboard() {
    fetch('/api/leaderboard', 	{
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
            
            const userName = s.user ? s.user.name : 'Unknown User';
            
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


// 초기 리더보드 로드
fetchLeaderboard();

// =========================================================
// Service Worker 등록 로직 (파일의 맨 끝에 추가)
// =========================================================
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