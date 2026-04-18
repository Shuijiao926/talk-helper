import { AudioResult, Platform, PodcastStep, TaskUserInput } from "@/lib/podcast/types";
import { BaseJobData, LongTextResult } from "./types";

import { getDb } from "@/db/db";
import { tasksTable } from "@/db/schema";
import { Task } from "@/db/types";
import { genParts, genVoiceFishAudio, genVoiceMinimax } from "@/lib/podcast/audio_parts";
import { genWhole } from "@/lib/podcast/audio_whole";
import { taskGetStepItem, taskUpdateStepItem } from "@/lib/podcast/task";
import { TaskStatus } from "@/types/task";
import { queryWrap } from "@/utils/db-util";
import { getTaskLogKey } from "@/utils/task";
import { Queue } from 'bullmq';
import { eq } from "drizzle-orm";
import { initQueue } from "./queue_base";
import { getCache, getRedis } from "@/utils/redis";
import { getPlatformConcurrencies } from "@/lib/podcast/client_utils";
import fs from "fs";

const QueueName = 'audio_queue'
const currentStep = PodcastStep.Audio
let queue: Queue<BaseJobData>;

export function getAudioQueue(): Queue<BaseJobData> {
  if (queue) {
    return queue
  }

  queue = new Queue<BaseJobData>(QueueName, {connection: getRedis()})

  return queue
}

export function setupAudioQueue() {
  const queue = getAudioQueue();
  initQueue({
    queue: queue,
    processTask: processAudioTask,
    currentStep: currentStep,
    queueName: QueueName,
    concurrency: 1,
  })
}

export async function processAudioTask(task: Task) {
  const userInputs = task.userInputs as TaskUserInput
  const stepItem = taskGetStepItem(task, PodcastStep.Audio)
  const result = stepItem.input as LongTextResult
  let audioResult: AudioResult

  console.log(`[${currentStep}:processTask] generate audio, key=${getTaskLogKey(task)}`)
  if (process.env.MOCK_ENABLED) {
    audioResult = { audio: Buffer.from([]), format: 'mp3' }
  } else {
    console.log(`[${currentStep}:processTask] generate audio, platform=${userInputs.platform}, voice_id_1=${userInputs.voice_id_1}, voice_id_2=${userInputs.voice_id_2}`)
    let platform = userInputs.platform
    if (userInputs.platform === Platform.FishAudio + '_custom') {
      platform = Platform.FishAudio
    }
    switch (platform) {
      case Platform.Gemini:
        audioResult = await genWhole(result.script, userInputs.voice_id_1!, userInputs.voice_id_2!)
        break
      case Platform.Minimax:
        audioResult = await genParts({
          concurrency: getPlatformConcurrencies()[platform],
          items: result.script,
          genFn: genVoiceMinimax,
          voiceOption_1: { id: userInputs.voice_id_1! },
          voiceOption_2: { id: userInputs.voice_id_2! }
        })
        break
      case Platform.FishAudio:
        audioResult = await genParts({
          concurrency: getPlatformConcurrencies()[platform],
          items: result.script,
          genFn: genVoiceFishAudio,
          voiceOption_1: { id: userInputs.voice_id_1! },
          voiceOption_2: { id: userInputs.voice_id_2! }
        })
        break
      default:
        throw new Error(`Unsupported platform: ${userInputs.platform}`)
    }
  }

  console.log(`[${currentStep}:processTask] upload audio, key=${getTaskLogKey(task)}`)
  // 上传到 s3
  let location = ''
  if (process.env.MOCK_ENABLED) {
    location = `${process.env.NEXT_PUBLIC_SITE_URL}/assets/site/podcast.mp3`
  } else {
    const s3Key = `${task.uuid}-${Date.now()}.${audioResult.format}`
    const cached = await getCache(s3Key)
    if (cached) {
      location = cached as string
    } else {
      if (process.env.NEXT_PUBLIC_CLERK_ENABLED) {
      } else {
        const savePath = `assets/audio/${s3Key}`
        fs.mkdirSync('public/assets/audio', { recursive: true })
        fs.writeFileSync('public/' + savePath, new Uint8Array(audioResult.audio))
        location = process.env.NEXT_PUBLIC_SITE_URL! + '/' + savePath
      }
    }
    console.log(`[${currentStep}:processTask] audio location=${location}, key=${getTaskLogKey(task)}`)
  }

  taskUpdateStepItem(task, PodcastStep.Audio, {
    output: {
      location: location,
      duration: audioResult.duration,
    },
  })

  // save to db
  if (process.env.NEXT_PUBLIC_CLERK_ENABLED) {
  } else {
    await queryWrap(getDb().update(tasksTable).set({
      stepsDetail: task.stepsDetail,
      status: TaskStatus.Success,
    }).where(eq(tasksTable.id, task.id)))
  }
}