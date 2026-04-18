import { NewTask, Task } from "@/db/types";
import { ScriptItem } from "@/lib/podcast/types";

export interface BaseJobData {
    task: NewTask
}

export interface LongTextResult {
    title: string;
    outline: string;
    key_points: string;
    script: ScriptItem[];
    script_length?: {
        cntChar: number;
        cntByte: number;
        cntMergedChar: number;
    };
}