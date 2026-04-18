import { PodcastStep } from "@/lib/podcast/types";
import { BaseJobData } from "./types";

import { Task } from "@/db/types";
import { Queue } from 'bullmq';
import { initQueue } from "./queue_base";
import { sleep } from "@/utils/time_util";
import { getRedis } from "@/utils/redis";

const QueueName = 'test_queue'
const currentStep = PodcastStep.Test
let queue: Queue<BaseJobData>;

export function getTestQueue(): Queue<BaseJobData> {
  if (queue) {
    return queue
  }

  queue = new Queue<BaseJobData>(QueueName, {connection: getRedis()})

  return queue
}

export function setupTestQueue() {
  const queue = getTestQueue();
  initQueue({
    queue: queue,
    processTask: processTask,
    currentStep: currentStep,
    queueName: QueueName
  })
}

async function processTask(task: Task) {
  console.log('test started')
  await sleep(5000)
  console.log('test finished')
}