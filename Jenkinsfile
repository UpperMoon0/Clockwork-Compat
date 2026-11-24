@Library('nstut-ci') _
// Trusted canonical main only. Native interoperability is gated separately
// by the TFG full-stack Forge acceptance of the exact candidate artifact.
nstutPortable(
    branch: 'main',
    runtime: 'any',
    python: true,
    tasks: [[name: 'Forge 1.20.1 check and build', minutes: 90,
             linuxArgv: ['python3', 'tools/ci-build.py'],
             windowsArgv: ['python', 'tools/ci-build.py']]],
    tests: 'build/test-results/test/*.xml',
    artifacts: 'ci-production/**,build/reports/tests/**'
)
