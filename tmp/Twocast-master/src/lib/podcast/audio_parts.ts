import axios from 'axios';
import { ScriptItem, VoiceOption } from './types';
import msgpack from 'msgpack-lite';
import pLimit from 'p-limit';
import { AudioResult } from './types';
import { parseAudioBuffer } from '@/utils/ffprobe-util';
import { getAxiosInstance } from '@/utils/http';

export async function genVoiceMinimax(text: string, voiceOption: VoiceOption): Promise<AudioResult> {
    const groupId = process.env.MINIMAX_GROUP_ID;
    const url = `https://api.minimax.chat/v1/t2a_v2?GroupId=${groupId}`;

    const headers = {
        'Authorization': `Bearer ${process.env.MINIMAX_TOKEN}`,
        'Content-Type': 'application/json',
    };

    const payload = {
        model: 'speech-02-turbo',
        text: text,
        voice_setting: {
            speed: voiceOption.speed,
            vol: voiceOption.volume,
            voice_id: voiceOption.id
        },
        language_boost: 'auto',
    };

    const response = await axios.post(url, payload, { headers });
    if (response.status !== 200) {
        throw new Error(`http error: status code ${response.status}, ${response.data}`);
    }

    const data = response.data;
    let status: number;
    try {
        status = data.data.status;
    } catch {
        status = 0;
    }

    if (status !== 2) {
        const text = JSON.stringify(data.base_resp);
        throw new Error(`generate voice failed: ${text}`);
    }

    const audioHex = data.data.audio;
    return {audio: Buffer.from(audioHex, 'hex'), format: 'mp3'};
}

export async function genVoiceFishAudio(text: string, voiceOption: VoiceOption): Promise<AudioResult> {
    const url = 'https://api.fish.audio/v1/tts';
    const headers = {
        'Authorization': `Bearer ${process.env.FISH_AUDIO_TOKEN}`,
        'Content-Type': 'application/msgpack',
        'model': 's1',
        'developer-id': 'ee3ddd082fe14364a30abefadd797dac',
    };

    let prosody: any = null;
    if (voiceOption.speed != undefined) {
        prosody = {
            speed: voiceOption.speed,
        }
    }
    if (voiceOption.volume != undefined) {
        prosody = {
            volume: voiceOption.volume,
        }
    }

    const request: any = {
        text: text,
        chunk_length: 200,
        format: 'mp3',
        mp3_bitrate: 128,
        references: [],
        reference_id: voiceOption.id,
        normalize: true,
        latency: 'normal',
    };
    if (prosody) {
        request.prosody = prosody;
    }
    // console.log(JSON.stringify(request))
    // return Buffer.from([])

    const response = await getAxiosInstance().post(url, msgpack.encode(request), {
        headers,
        responseType: 'arraybuffer',
    });

    if (response.status !== 200) {
        switch (response.status) {
            case 400:
                console.log(JSON.stringify(request))
                throw new Error(`HTTP error: status code ${response.status}, ${response.data}`);
            default:
                throw new Error(`HTTP error: status code ${response.status}`);
        }
    }

    return {audio: Buffer.from(response.data), format: 'mp3'};
}

export interface GenPartsParams {
    concurrency: number
    items: ScriptItem[]
    genFn: (text: string, voiceOption: VoiceOption) => Promise<AudioResult>
    voiceOption_1: VoiceOption
    voiceOption_2: VoiceOption
}
export async function genParts(params: GenPartsParams): Promise<AudioResult> {
    // 使用 p-limit 控制并发，最多5个任务同时运行
    console.log(`[genParts] concurrency=${params.concurrency}`)
    const limit = pLimit(params.concurrency || 1);
    let audios: AudioResult[] = Array(params.items.length).fill(null);

    const tasks = params.items.map((item, idx) =>
        limit(async () => {
            const text = item.text?.trim();
            if (!text) {
                return;
            }
            const voiceOpt = idx % 2 === 0 ? params.voiceOption_1 : params.voiceOption_2;
            // console.log(`[genParts] id=${idx}`)
            const audio = await params.genFn(text, voiceOpt);
            audios[idx] = audio;
        })
    );

    await Promise.all(tasks);

    const audioBytes = Buffer.concat(
        audios
            .filter(item => item)
            .map(item => new Uint8Array(item.audio))
    );

    // 计算时长
    const duration = await parseAudioBuffer(audioBytes);

    return {audio: audioBytes, format: audios[0].format, duration: duration.duration};
}
