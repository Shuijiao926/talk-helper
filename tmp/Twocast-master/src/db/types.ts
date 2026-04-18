import { InferSelectModel, InferInsertModel } from 'drizzle-orm';
import {
  tasksTable,
} from './schema';

// 从 schema 定义中推导出类型
export type Task = InferSelectModel<typeof tasksTable>;
export type NewTask = InferInsertModel<typeof tasksTable>;

// Keep the TaskStatus type definition
export type TaskStatus = 0 | 1 | 2 | 3; // 0=待执行,1=已提交,2=成功,3=失败
