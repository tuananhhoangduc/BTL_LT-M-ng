"""Import the user-selected MP3 as runtime PCM; no Python dependency when playing."""
from pathlib import Path
import argparse
import hashlib
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / '.work/python-deps'))
import numpy as np
import soundfile as sf

parser = argparse.ArgumentParser()
parser.add_argument('source', type=Path)
args = parser.parse_args()
data, rate = sf.read(args.source, dtype='float32', always_2d=True)
if len(data) < rate:
    raise ValueError('Music track must be at least one second long')
if not np.isfinite(data).all():
    raise ValueError('Invalid audio samples')
peak = float(np.max(np.abs(data)))
gain = min(1.0, .89 / max(peak, 1e-9))
data *= gain
# A short edge fade avoids a discontinuity/click when the complete song repeats.
# Preserve the chosen composition and duration; do not attempt an unverified beat cut.
edge = min(int(rate * .06), len(data) // 2)
fade = np.linspace(0, 1, edge, dtype='float32')[:, None]
data[:edge] *= fade
data[-edge:] *= fade[::-1]
output = ROOT / 'assets/audio/bgm/puzzle_bright.wav'
output.parent.mkdir(parents=True, exist_ok=True)
sf.write(output, data, rate, subtype='PCM_16')
metadata = {
    'source_filename': args.source.name,
    'source_sha256': hashlib.sha256(args.source.read_bytes()).hexdigest(),
    'runtime_file': 'audio/bgm/puzzle_bright.wav',
    'duration_seconds': round(len(data) / rate, 3),
    'sample_rate': rate,
    'channels': data.shape[1],
    'peak_before': round(peak, 6),
    'gain_applied': round(gain, 6),
    'edge_fade_ms': 60,
    'selection': 'User supplied and explicitly selected this local MP3.',
}
(output.parent / 'track.json').write_text(json.dumps(metadata, indent=2) + '\n', encoding='utf-8')
print(json.dumps(metadata, indent=2))
