import { getDb } from "@/db/db";
import { tasksTable } from "@/db/schema";
import { NewTask, Task } from "@/db/types";
import { eq } from "drizzle-orm";
import { queryWrap } from "@/utils/db-util";
import moment from "moment";
import { TaskStatus } from "@/types/task";
import _ from 'lodash'


export function getTaskLogKey(task: NewTask) {
    return `task_id=${task.id}, user_email=${task.userEmail}`
}

export function getTaskStatusHuman(status: TaskStatus, forClient?: boolean) {
    if (forClient && status === TaskStatus.Processing) {
        return 'Generating';
    }
    return _.capitalize(status);
}