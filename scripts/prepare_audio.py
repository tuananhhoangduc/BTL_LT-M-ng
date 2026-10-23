"""Convert Kenney CC0 packs to PCM WAV; Python is only needed to rebuild assets."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / '.work/python-deps'))
import soundfile as sf

OUT = ROOT / 'assets/audio'
OUT.mkdir(parents=True, exist_ok=True)
SOURCES = {
    'click': 'interface/Audio/click_003.ogg',
    'move': 'interface/Audio/drop_002.ogg',
    'capture': 'interface/Audio/confirmation_002.ogg',
    'chat': 'interface/Audio/select_001.ogg',
    'challenge': 'interface/Audio/confirmation_001.ogg',
    'tick': 'interface/Audio/tick_001.ogg',
    'win': 'jingles/Audio/Pizzicato jingles/jingles_PIZZI10.ogg',
    'lose': 'jingles/Audio/Pizzicato jingles/jingles_PIZZI02.ogg',
    'start': 'interface/Audio/open_001.ogg',
}
for name, relative in SOURCES.items():
    data, rate = sf.read(ROOT / '.work/downloads' / relative, always_2d=True)
    peak = abs(data).max()
    if peak > 0:
        data = data * (.65 / peak)
    sf.write(OUT / (name + '.wav'), data, rate, subtype='PCM_16')
    print(name, round(len(data) / rate, 2), 'seconds')

# Music is imported separately by import_bgm.py. Never regenerate the old drone.
