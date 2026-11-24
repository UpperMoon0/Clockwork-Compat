# Clockwork Compat

Forge 1.20.1 interoperability addon for Create: Clockwork 0.5.7.1 and Kelvin 0.5.2. The first integration makes an existing Clockwork Steam Extractor accept GregTech CEu steam from the facing fluid capability of an adjacent non-Create tank, boiler output or pipe. It never replaces a native Create boiler or drains water. GregTech is optional; no direct GT class links.

Conversion uses a defined gameplay unit (1 mB = 1 g Kelvin steam, up to 100 mB/t; maximum native extractor throughput 0.1 kg/t), not a claim about real-world steam density. Input temperature is the Forge fluid's registered temperature; Kelvin owns heat accounting. A pressure/temperature limit and persisted pending amount prevent uncontrolled injection or re-draining a rejected batch.

Build with Java 17: `python3 tools/fetch-deps.py && ./gradlew check build`. Binary dependencies are pinned and not bundled. Native Forge acceptance remains required before distribution: actual GT steam and non-steam sources, pressure, reload, no dual-sourcing, and full gas-engine/balloon operation in TFG.
