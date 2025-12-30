export function shuffleArray(array) {
    for (let i = array.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [array[i], array[j]] = [array[j], array[i]];
    }
    return array;
}

export function generateConnectedBlock(numBlocks, maxRows, maxCols) {
    if (maxRows <= 0 || maxCols <= 0) return null;

    const grid = Array(maxRows).fill(0).map(() => Array(maxCols).fill(false));
    const blockPositions = [];

    let startRow = Math.floor(Math.random() * Math.max(1, maxRows - 2));
    let startCol = Math.floor(Math.random() * Math.max(1, maxCols - 2));

    grid[startRow][startCol] = true;
    blockPositions.push({ row: startRow, col: startCol });

    const directions = [
        { dr: -1, dc: 0 }, { dr: 1, dc: 0 },
        { dr: 0, dc: -1 }, { dr: 0, dc: 1 }
    ];

    let attempts = 0;
    const maxAttempts = numBlocks * 20;

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

    if (blockPositions.length < numBlocks) return null;

    const minRow = Math.min(...blockPositions.map(p => p.row));
    const minCol = Math.min(...blockPositions.map(p => p.col));

    return blockPositions.map(p => ({
        row: p.row - minRow,
        col: p.col - minCol
    }));
}

export function getErrorMessage(error) {
    if (!error) return '알 수 없는 오류가 발생했습니다.';

    const status = error.status || (error.response && error.response.status);
    const code = error.code;
    const message = error.message || (typeof error === 'string' ? error : '');

    if (code === 'INVALID_SESSION') return '세션이 만료되었거나 유효하지 않습니다.\n다시 로그인해 주세요.';
    if (code === 'INVALID_SCORE') {
        if (message.includes('cheating')) return '부정행위가 감지되었습니다.\n정상적인 플레이를 부탁드립니다.';
        return '유효하지 않은 점수 기록 요청입니다.';
    }
    if (code === 'DUPLICATE_SCORE') return '동일한 점수가 오늘 이미 기록되어 있습니다.';
    if (code === 'USER_NOT_FOUND') return '사용자 정보를 찾을 수 없습니다.';
    if (code === 'TOO_MANY_REQUESTS') return '너무 많은 요청이 발생했습니다.\n잠시 후 다시 시도해 주세요.';

    if (status === 401) return '로그인이 필요하거나 세션이 만료되었습니다.';
    if (status === 403) return '요청하신 작업을 수행할 권한이 없습니다.';
    if (status === 404) return '요청하신 정보를 찾을 수 없습니다.';
    if (status >= 500) return '서버 오류가 발생했습니다.\n잠시 후 다시 시도해 주세요.';

    if (message.includes('Failed to fetch') || message.includes('NetworkError')) {
        return '서버에 연결할 수 없습니다.\n네트워크 상태를 확인해 주세요.';
    }

    return message || '요청 처리 중 오류가 발생했습니다.';
}
