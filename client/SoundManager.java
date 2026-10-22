package client;

import javax.sound.sampled.*;

final class SoundManager {
    private static boolean enabled = true;

    private SoundManager() {}

    static void setEnabled(boolean on) { enabled = on; }
    static boolean isEnabled() { return enabled; }

    static void playClick() { playTone(880, 60, 0.3); }
    static void playMove() { playTone(660, 80, 0.25); }
    static void playCapture() { playChord(new int[]{523, 659, 784}, 120, 0.3); }
    static void playChat() { playTone(1200, 50, 0.15); }
    static void playChallenge() { playChord(new int[]{440, 554, 659}, 200, 0.35); }

    static void playWin() {
        new Thread(() -> {
            playTone(523, 150, 0.4);
            sleep(100);
            playTone(659, 150, 0.4);
            sleep(100);
            playTone(784, 150, 0.4);
            sleep(100);
            playTone(1047, 300, 0.5);
        }, "sound-win").start();
    }

    static void playLose() {
        new Thread(() -> {
            playTone(440, 200, 0.3);
            sleep(150);
            playTone(370, 200, 0.3);
            sleep(150);
            playTone(330, 400, 0.25);
        }, "sound-lose").start();
    }

    static void playGameStart() {
        new Thread(() -> {
            playTone(523, 100, 0.3);
            sleep(80);
            playTone(659, 100, 0.3);
            sleep(80);
            playTone(784, 200, 0.4);
        }, "sound-start").start();
    }

    private static void playTone(int freqHz, int durationMs, double volume) {
        if (!enabled) return;
        new Thread(() -> {
            try {
                float sampleRate = 44100;
                int samples = (int) (sampleRate * durationMs / 1000);
                byte[] buf = new byte[samples * 2];
                for (int i = 0; i < samples; i++) {
                    double angle = 2.0 * Math.PI * freqHz * i / sampleRate;
                    // Envelope: fade in 10%, sustain 60%, fade out 30%
                    double env;
                    double pos = (double) i / samples;
                    if (pos < 0.1) env = pos / 0.1;
                    else if (pos < 0.7) env = 1.0;
                    else env = (1.0 - pos) / 0.3;
                    short val = (short) (Math.sin(angle) * 32767 * volume * env);
                    buf[i * 2] = (byte) (val & 0xff);
                    buf[i * 2 + 1] = (byte) ((val >> 8) & 0xff);
                }
                AudioFormat fmt = new AudioFormat(sampleRate, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(fmt);
                line.start();
                line.write(buf, 0, buf.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {}
        }, "sound-tone").start();
    }

    private static void playChord(int[] freqs, int durationMs, double volume) {
        if (!enabled) return;
        new Thread(() -> {
            try {
                float sampleRate = 44100;
                int samples = (int) (sampleRate * durationMs / 1000);
                byte[] buf = new byte[samples * 2];
                for (int i = 0; i < samples; i++) {
                    double mixed = 0;
                    for (int f : freqs) {
                        mixed += Math.sin(2.0 * Math.PI * f * i / sampleRate);
                    }
                    mixed /= freqs.length;
                    double pos = (double) i / samples;
                    double env = pos < 0.05 ? pos / 0.05 : (1.0 - pos) / 0.95;
                    short val = (short) (mixed * 32767 * volume * env);
                    buf[i * 2] = (byte) (val & 0xff);
                    buf[i * 2 + 1] = (byte) ((val >> 8) & 0xff);
                }
                AudioFormat fmt = new AudioFormat(sampleRate, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(fmt);
                line.start();
                line.write(buf, 0, buf.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {}
        }, "sound-chord").start();
    }

    private static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}

