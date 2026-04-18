import { StepsDetailItem, PodcastStep } from "./types";
import { NewTask, Task } from "@/db/types";

export function taskAddCredit(task: NewTask, credit: number) {
  task.consumedCredits = (task.consumedCredits || 0) + credit
  return task
}

export function taskGetStepItem(task: NewTask, step: PodcastStep) {
  return task.stepsDetail?.[step] || {} as StepsDetailItem
}

export function taskSetStepItem(task: NewTask, step: PodcastStep, item: StepsDetailItem) {
  const stepsDetail = task.stepsDetail || {}

  item.updated_at = new Date()
  stepsDetail[step] = item
  task.stepsDetail = stepsDetail
  return task
}

export function taskUpdateStepItem(task: NewTask, step: PodcastStep, item: Partial<StepsDetailItem>) {
  const stepsDetail = task.stepsDetail || {}
  const stepItem = stepsDetail[step] || {} as StepsDetailItem
  item.updated_at = new Date()
  Object.assign(stepItem, item)
  stepsDetail[step] = stepItem

  task.stepsDetail = stepsDetail
  return task
}