#!/usr/bin/env python3
"""Portable trusted-main build; stage only the remapped addon and identity."""
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
def run(*argv):
    return subprocess.check_output(argv, cwd=ROOT, text=True).strip()

def main():
    subprocess.run([sys.executable, 'tools/fetch-deps.py'], cwd=ROOT, check=True)
    wrapper = (['cmd.exe', '/d', '/c', 'gradlew.bat'] if os.name == 'nt'
               else ['./gradlew'])
    subprocess.run(wrapper + ['--console=plain', 'clean', 'check', 'build'],
                   cwd=ROOT, check=True)
    jars = list((ROOT / 'build/libs').glob('clockwork-compat-forge-1.20.1-*.jar'))
    if len(jars) != 1:
        raise RuntimeError(f'Expected exactly one remapped addon JAR, got {jars}')
    jar = jars[0]
    with zipfile.ZipFile(jar) as archive:
        required = {'META-INF/mods.toml', 'clockworkcompat.mixins.json',
                    'dev/nstut/clockworkcompat/mixin/SteamExtractorMixin.class'}
        if not required.issubset(archive.namelist()):
            raise RuntimeError('Release JAR missing addon metadata or classes')
        if any(name.endswith('.jar') for name in archive.namelist()):
            raise RuntimeError('Release JAR unexpectedly bundles a dependency')
    out = ROOT / 'ci-production'
    out.mkdir(exist_ok=False)
    shutil.copy2(jar, out / jar.name)
    sha = hashlib.sha256(jar.read_bytes()).hexdigest()
    commit = run('git', 'rev-parse', 'HEAD')
    (out / 'identity.json').write_text(json.dumps({
        'commit': commit, 'artifact': jar.name, 'sha256': sha,
        'nativeAcceptance': 'required separately'
    }, indent=2) + '\n', encoding='utf-8')
    (out / 'SHA256SUMS').write_text(f'{sha}  {jar.name}\n', encoding='utf-8')
    print(f'CLOCKWORK_BUILD_PASS sha={commit} artifact={jar.name} sha256={sha}')

if __name__ == '__main__':
    main()
