#!/usr/bin/env python3
import hashlib, json, pathlib, urllib.request
root = pathlib.Path(__file__).resolve().parents[1]
target = root / 'libs'
target.mkdir(exist_ok=True)
for dep in json.loads((root/'tools/dependencies.json').read_text()):
    path = target / dep['name']
    if not path.exists():
        with urllib.request.urlopen(dep['url'], timeout=60) as response:
            data = response.read()
        if hashlib.sha256(data).hexdigest() != dep['sha256']:
            raise RuntimeError('Checksum mismatch: ' + dep['name'])
        path.write_bytes(data)
    if hashlib.sha256(path.read_bytes()).hexdigest() != dep['sha256']:
        raise RuntimeError('Checksum mismatch: ' + dep['name'])
    print('Verified ' + dep['name'])
