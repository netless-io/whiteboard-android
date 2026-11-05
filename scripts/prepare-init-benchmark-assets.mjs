import assert from 'node:assert/strict';
import {readFile, mkdir, writeFile, rename, rm} from 'node:fs/promises';
import {execFileSync} from 'node:child_process';
import {createHash} from 'node:crypto';
import {fileURLToPath} from 'node:url';
import path from 'node:path';

// Explicit debug preparation only. SDK assets, the entry HTML and Worker code stay untouched.
const root = fileURLToPath(new URL('../', import.meta.url));
const debugAssets = path.join(root, 'app/src/debug/assets');
const sdkAssets = path.join(root, 'sdk/src/main/assets/whiteboard');
const output = path.join(root, 'app/build/generated/init-benchmark-assets/whiteboard');
// Keep a historical A/B measurement on exactly the same packaged JS/Workers.
// Only generated Debug overrides are written; the SDK resource tree is untouched.
const baselineIndex = process.argv.indexOf('--baseline-apk');
if (baselineIndex >= 0) {
    const apk = path.resolve(process.argv[baselineIndex + 1]);
    const files = execFileSync('unzip', ['-Z1', apk], {encoding:'utf8'}).split('\n')
        .filter(file => file.startsWith('assets/whiteboard/') && !file.endsWith('/'));
    assert.ok(files.includes('assets/whiteboard/index.html'));
    const rows = files.map(file => {
        const name = file.slice('assets/whiteboard/'.length);
        assert.ok(!name.includes('\\') && !name.split('/').some(part => !part || part === '..' || part === '.'));
        const bytes = execFileSync('unzip', ['-p', apk, file], {maxBuffer:32*1024*1024});
        return {name, bytes};
    });
    const probedMain = rows.find(row => row.name.startsWith('main.') && row.name.endsWith('.js'));
    assert.ok(probedMain?.bytes.includes(Buffer.from('window.__wbInitBench.mark("T3")')));
    assert.ok(probedMain?.bytes.includes(Buffer.from('window.__wbInitBench.mark("T4")')));
    await rm(output, {recursive:true,force:true});
    for (const row of rows) { const file = path.join(output,row.name); await mkdir(path.dirname(file),{recursive:true}); await writeFile(file,row.bytes); }
    const hash = bytes => createHash('sha256').update(bytes).digest('hex');
    await writeFile(path.join(root,'app/build/generated/init-benchmark-baseline.json'), JSON.stringify({
        apkSha256:hash(await readFile(apk)), assets:rows.map(({name,bytes})=>({name,sha256:hash(bytes)}))
    },null,2)+'\n');
    console.log(`Prepared ${rows.length} exact baseline APK Debug assets`);
    process.exit(0);
}
const patches = JSON.parse(await readFile(path.join(debugAssets, 'join-benchmark-patches.json'), 'utf8'));
const helper = await readFile(path.join(debugAssets, 'join-benchmark.js'), 'utf8');
const entries = Object.entries(patches);
assert.equal(entries.length, 3, 'Expected exactly three benchmark asset overrides');
const generated = [];
for (const [file, replacements] of entries) {
    assert.match(file, /^(?:web-sdk|main|netless)\.[a-f0-9]+\.js$/);
    let source = await readFile(path.join(sdkAssets, file), 'utf8');
    for (const {needle, replacement} of replacements) {
        assert.equal(typeof needle, 'string');
        assert.ok(needle.length > 0);
        assert.equal(typeof replacement, 'string');
        const offset = source.indexOf(needle);
        assert.ok(offset >= 0 && source.indexOf(needle, offset + needle.length) === -1,
            `Benchmark anchor must match exactly once: ${file}`);
        source = source.slice(0, offset) + replacement + source.slice(offset + needle.length);
    }
    // Main executes after the SDK chunk is registered and before any SDK join call can run.
    if (file.startsWith('main.')) source = helper + '\n' + source;
    generated.push({file, source});
}
// Validate every anchor before writing any generated asset. Never overwrite SDK input files.
// Remove prior overrides, including historical index/Worker files, before using current SDK assets.
await rm(output, {recursive: true, force: true});
await rm(path.join(root, 'app/build/generated/init-benchmark-baseline.json'), {force: true});
await mkdir(output, {recursive: true});
for (const {file, source} of generated) {
    const target = path.join(output, file);
    const temporary = target + '.tmp';
    await writeFile(temporary, source);
    await rename(temporary, target);
    console.log('Prepared ' + path.relative(root, target));
}
