import { integer, pgTable, varchar, json, timestamp, numeric, serial, bigint, smallint, boolean } from "drizzle-orm/pg-core";

export const tasksTable = pgTable('tasks', {
  id: integer().primaryKey().generatedByDefaultAsIdentity(),
  // UUID
  uuid: varchar('uuid', { length: 64 }).notNull().unique(),
  // 会员ID
  userId: bigint('user_id', { mode: 'number' }).notNull(),
  userEmail: varchar('user_email', { length: 255 }).notNull(),
  // 用户输入
  userInputs: json('user_inputs'),
  // 结果
  result: json('result').default({}),
  // 状态:0=待执行,1=已提交,2=成功,3=失败
  status: varchar('status', { length: 10 }).notNull(),
  // 状态原因
  statusReason: json('status_reason'),
  // 状态更新时间
  statusAt: timestamp('status_at', { withTimezone: true }),
  // 当前步骤
  currentStep: varchar('current_step', { length: 32 }).default(''),
  // 步骤详情，包含每个步骤的输入、输出和错误信息
  stepsDetail: json('steps_detail').default({}),
  // 消耗积分
  consumedCredits: integer('consumed_credits').notNull(),
  // 精选
  isFeatured: boolean('is_featured').default(false),
  // 创建时间
  createdAt: timestamp('created_at', { withTimezone: true }),
  // 更新时间
  updatedAt: timestamp('updated_at', { withTimezone: true })
});
