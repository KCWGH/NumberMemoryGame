import { BASE_FREQUENCY, PITCH_STEP } from './constants.js';

let audioContext;

export function initAudioContext() {
    if (!audioContext) {
        audioContext = new (window.AudioContext || window.webkitAudioContext)();
    }
}

export function playSuccessSound(stepIndex) {
    if (!audioContext) initAudioContext();

    const frequency = BASE_FREQUENCY * Math.pow(PITCH_STEP, stepIndex);
    const oscillator = audioContext.createOscillator();
    const gainNode = audioContext.createGain();

    oscillator.type = 'sine';
    oscillator.frequency.setValueAtTime(frequency, audioContext.currentTime);
    gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);

    oscillator.connect(gainNode);
    gainNode.connect(audioContext.destination);

    const attackTime = 0.01;
    const decayTime = 0.15;

    oscillator.start();
    gainNode.gain.linearRampToValueAtTime(0.5, audioContext.currentTime + attackTime);
    gainNode.gain.exponentialRampToValueAtTime(0.0001, audioContext.currentTime + attackTime + decayTime);
    oscillator.stop(audioContext.currentTime + attackTime + decayTime + 0.05);
}
