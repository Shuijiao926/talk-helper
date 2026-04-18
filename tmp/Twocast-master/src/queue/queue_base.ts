import { Task } from "@/db/types";
import { QueueHandler } from "./queue_handler"
import { BaseJobData } from "./types"
import { getRedis } from "@/utils/redis"
import { Queue, Worker, Job } from 'bullmq';
import { PodcastStep } from "@/lib/podcast/types";

export interface QueueConfig {
    queue: Queue<BaseJobData>,
    processTask: (task: Task) => Promise<any>,
    currentStep: PodcastStep,
    queueName: string,
    concurrency?: number
}

export function initQueue({ queue, processTask, currentStep, queueName, concurrency = 5 }: QueueConfig) {
    console.log(`initQueue: ${queueName}`)
    const worker = new Worker(queueName, async (job: Job<BaseJobData>) => {
        return await QueueHandler({
            queue: queue,
            task: job.data.task,
            fn: processTask,
            currentStep: currentStep,
        })
    }, {
        // 异常中断的任务，将在 15s 后重试， 对应参数为 runRetryDelay
        runRetryDelay: 5000,
        connection: getRedis(),
        removeOnFail: { count: 0 },
        removeOnComplete: { count: 0 },
        concurrency: concurrency
    })
}

export function addJob(queue: Queue<BaseJobData>, data: BaseJobData) {
    return queue.add(queue.name, data)
}