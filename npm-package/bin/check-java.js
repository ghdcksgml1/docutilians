#!/usr/bin/env node

const {execSync} = require('child_process');

try {
    const version = execSync('java -version 2>&1').toString();
    const match = version.match(/version "(\d+)/);
    const major = match ? parseInt(match[1]) : 0;

    if (major < 17) {
        console.warn('\x1b[33m');
        console.warn('⚠️  Warning: Docutilians requires Java 17+');
        console.warn('   Current:', version.split('\n')[0]);
        console.warn('   Download: https://adoptium.net/');
        console.warn('\x1b[0m');
    } else {
        console.log('\x1b[32m✓ Java', major, 'detected\x1b[0m');
    }
} catch (e) {
    console.warn('\x1b[33m');
    console.warn('⚠️  Warning: Java not found');
    console.warn('   Docutilians requires Java 17+ to run.');
    console.warn('   Download: https://adoptium.net/');
    console.warn('\x1b[0m');
}