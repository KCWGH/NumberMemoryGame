export const BASE_URL = window.location.origin;

export const SOCIAL_LOGIN_URLS = {
    google: `${BASE_URL}/oauth2/authorization/google`,
    kakao: `${BASE_URL}/oauth2/authorization/kakao`,
    naver: `${BASE_URL}/oauth2/authorization/naver`,
    logout: `${BASE_URL}/api/logout`,
    user: `${BASE_URL}/api/user`,
    gameStart: `${BASE_URL}/api/game/start`,
    gameEnd: `${BASE_URL}/api/game/end`,
    leaderboard: `${BASE_URL}/api/leaderboard`
};

export const BASE_FREQUENCY = 440;
export const PITCH_STEP = 1.059463;

export const THEMES = {
    LIGHT: 'light',
    DARK: 'dark'
};

export const BREAKPOINT_MOBILE = 900;
