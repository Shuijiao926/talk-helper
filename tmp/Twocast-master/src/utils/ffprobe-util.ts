import os from 'os';
import path from 'path';
import fs from 'fs';
import FormData from 'form-data';
import axios from 'axios';

export async function probe(input: string | Buffer) {
    const form = new FormData();
    if (typeof input === 'string') {
        form.append('file', fs.createReadStream(input));
    } else {
        form.append('file', input, { filename: 'audio' });
    }
    const response = await axios.post(process.env.SERVICE_FFPROBE_API!, form, {
        headers: form.getHeaders(),
    });
    return response.data;
}

export async function parseAudioFile(path: string) {
    const metadata = await probe(path);
    return {
        duration: parseFloat(metadata.streams[0].duration),
        sampleRate: parseInt(metadata.streams[0].sample_rate),
        channels: metadata.streams[0].channels,
    };
}
export async function parseAudioBuffer(buffer: Buffer) {
    const metadata = await probe(buffer);
    return {
        duration: parseFloat(metadata.streams[0].duration),
        sampleRate: parseInt(metadata.streams[0].sample_rate),
        channels: metadata.streams[0].channels,
    };
}