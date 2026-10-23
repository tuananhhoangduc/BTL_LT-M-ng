# Color Match Arena 2.1 — presentation assets

Production PNG art is in `assets/images/`: two crystal pieces, three ceramic tiles,
two backgrounds and three result emblems. Generated with built-in image_gen using
`docs/concepts/sac-tran-art-sheet-v2.png` as a style reference. Exact prompts,
references and output filenames are recorded in `assets/images/art-prompts.json`.
The concept sheet is not sliced or used as a game interface. Runtime uses individual
PNGs via SpriteCatalog; Java2D supplies layout, text, animation and fallback shapes.

Tile readability revision: `tiles/neutral-v2.png`, `tiles/red-area-v2.png`,
`tiles/blue-area-v2.png`. Built-in image_gen recreated these from the previous tiles;
exact prompts and references are in `assets/images/tile-v2-prompts.json`.
The catalog selects these three files. Original tiles remain for comparison.

Audio is packaged as 16-bit PCM WAV for Java Sound; no extra runtime codec is needed.

| Runtime file | Original source |
|---|---|
| click.wav | Interface Sounds / click_003.ogg |
| move.wav | Interface Sounds / drop_002.ogg |
| capture.wav | Interface Sounds / confirmation_002.ogg |
| chat.wav | Interface Sounds / select_001.ogg |
| challenge.wav | Interface Sounds / confirmation_001.ogg |
| tick.wav | Interface Sounds / tick_001.ogg |
| start.wav | Interface Sounds / open_001.ogg |
| win.wav | Music Jingles / Pizzicato jingles / jingles_PIZZI10.ogg |
| lose.wav | Music Jingles / Pizzicato jingles / jingles_PIZZI02.ogg |
| bgm/puzzle_bright.wav | User-selected MP3: cyberwave-orchestra-puzzle-game-bright-casual-video-game-music-249202.mp3 |

Kenney (https://kenney.nl) created/distributed Interface Sounds and Music Jingles.
Both are CC0; original license texts are retained in assets/licenses.
Source pages: https://kenney.nl/assets/interface-sounds and
https://kenney.nl/assets/music-jingles. Downloaded 2026-10-23.
SFX conversion: peak normalized to 0.65 and encoded as PCM_16 using soundfile.
Tracks have not been renamed to imply a different author or license.

The selected BGM is supplied locally by the user. Its source checksum, duration,
conversion gain and 60 ms edge-fade are in `assets/audio/bgm/track.json`.
It is not a Kenney or generated track. `scripts/import_bgm.py` reproduces conversion
from the original file. The retired synthesized ambient.wav is retained in the
working tree, is not referenced at runtime and is excluded from the new JAR.

To reproduce: extract the official ZIPs into `.work/downloads/interface` and
`.work/downloads/jingles`, install `soundfile` into `.work/python-deps`, then run
`python scripts/prepare_audio.py`. Python is not required to build or play the game.
