#!/usr/bin/env node

const {spawn} = require('child_process');
const path = require('path');

const jarPath = path.join(__dirname, '..', 'lib', 'docutilians.jar');
const args = process.argv.slice(2);

const child = spawn('java', ['-jar', jarPath, ...args], {
    stdio: 'inherit',
    env: process.env
});

child.on('error', (err) => {
    if (err.code === 'ENOENT') {
        console.error('\x1b[31m❌ Java not found. Please install Java 17+.\x1b[0m');
        console.error('   https://adoptium.net/');
        process.exit(1);
    }
    throw err;
});

child.on('close', (code) => {
    process.exit(code ?? 0);
});