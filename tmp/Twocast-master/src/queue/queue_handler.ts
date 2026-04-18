import { getDb } from "@/db/db";
import { tasksTable } from "@/db/schema";
import { NewTask } from "@/db/types";
import { taskGetStepItem, taskUpdateStepItem } from "@/lib/podcast/task";
import { PodcastStep } from "@/lib/podcast/types";
import { TaskStatus } from "@/types/task";
import { queryWrap } from "@/utils/db-util";
import { getTaskLogKey } from "@/utils/task";
import { eq } from "drizzle-orm";
import { getDelayTime } from "./utils";
import { BaseJobData } from "./types";
import { Queue } from 'bullmq';

export interface QueueHandlerParams {
    queue: Queue<BaseJobData>
    task: NewTask
    fn: (task: NewTask) => Promise<any>
    currentStep: PodcastStep
    afterError?: (error: any, task: NewTask) => Promise<void>
    afterCancel?: (error: any, task: NewTask) => Promise<void>
}

export async function QueueHandler(params: QueueHandlerParams) {
    const { task, fn, queue } = params
    const maxTryCount = 1
    const stepItem = taskGetStepItem(task, params.currentStep)
    stepItem.try_count = stepItem.try_count || 0
    stepItem.try_count++
    taskUpdateStepItem(task, params.currentStep, {
        error: '',
        try_count: stepItem.try_count,
    })
    console.log(`[QueueHandler] [${params.currentStep}] start, ${getTaskLogKey(task)}`)

    try {
        return await fn(task)
    } catch (error) {
        const errorStack = error.stack?.slice(0, 500) || error.message
        console.error(`[QueueHandler] [${params.currentStep}]`, error)
        taskUpdateStepItem(task, params.currentStep, {
            error: errorStack,
            try_count: stepItem.try_count,
            updated_at: new Date()
        })

        console.log(`[QueueHandler] update task [${params.currentStep}]`, getTaskLogKey(task))

        // 如果尝试次数大于3次，则取消任务
        if (stepItem.try_count && stepItem.try_count >= maxTryCount) {
            console.error(`[QueueHandler] Task failed after ${maxTryCount} tries [${params.currentStep}]`, getTaskLogKey(task))
            await queryWrap(getDb().update(tasksTable).set({
                status: TaskStatus.Failed,
                stepsDetail: task.stepsDetail
            }).where(eq(tasksTable.id, task.id!)))
            await params.afterCancel?.(new Error(`Task failed after ${maxTryCount} tries`), task)
            return null
        } else {
            await queryWrap(getDb().update(tasksTable).set({
                stepsDetail: task.stepsDetail
            }).where(eq(tasksTable.id, task.id!)))

            // 重试
            console.log(`[afterError] delay task [${params.currentStep}], delay=${getDelayTime()}`, getTaskLogKey(task))
            await queue.add('topic', { task: task }, { delay: getDelayTime() })

            // hook
            await params.afterError?.(error, task)
            return null
        }
    }
}