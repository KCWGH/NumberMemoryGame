import { SOCIAL_LOGIN_URLS } from './constants.js';

export function getCsrfToken() {
    const name = 'XSRF-TOKEN=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

export async function checkLoginStatus() {
    const cacheBreaker = Date.now();
    try {
        const response = await fetch(`${SOCIAL_LOGIN_URLS.user}?t=${cacheBreaker}`, {
            credentials: 'include',
            cache: 'no-store'
        });
        if (response.ok) {
            return await response.json();
        }
        return { authenticated: false };
    } catch (err) {
        console.error('Login check failed:', err);
        return { authenticated: false };
    }
}

export async function handleLogout() {
    try {
        const response = await fetch(SOCIAL_LOGIN_URLS.logout, {
            method: 'POST',
            headers: {
                'X-XSRF-TOKEN': getCsrfToken()
            },
            credentials: 'include'
        });
        return response.ok;
    } catch (err) {
        console.error('Logout error:', err);
        return false;
    }
}

export async function startGameSession() {
    const response = await fetch(SOCIAL_LOGIN_URLS.gameStart, {
        method: 'POST',
        headers: {
            'X-XSRF-TOKEN': getCsrfToken()
        },
        credentials: 'include'
    });

    if (!response.ok) {
        let errorData;
        try {
            errorData = await response.json();
        } catch (e) {
            errorData = { message: await response.text() };
        }
        throw { status: response.status, ...errorData };
    }
    return await response.json();
}

export async function submitGameEnd(data) {
    const response = await fetch(SOCIAL_LOGIN_URLS.gameEnd, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': getCsrfToken()
        },
        body: JSON.stringify(data),
        credentials: 'include'
    });

    if (response.ok) return { success: true };

    let errorData;
    try {
        errorData = await response.json();
    } catch (e) {
        errorData = { message: await response.text() };
    }

    return {
        success: false,
        status: response.status,
        ...errorData
    };
}

export async function fetchLeaderboardData(type = 'all') {
    const cacheBreaker = Date.now();
    let url = `${SOCIAL_LOGIN_URLS.leaderboard}?t=${cacheBreaker}`;
    if (type === 'my') {
        url += '&filter=my';
    }

    const response = await fetch(url, {
        credentials: 'include',
        cache: 'no-store'
    });

    if (!response.ok) {
        let errorData;
        try {
            errorData = await response.json();
        } catch (e) {
            errorData = {};
        }
        throw { status: response.status, ...errorData };
    }
    return await response.json();
}
