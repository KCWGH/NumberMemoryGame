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
