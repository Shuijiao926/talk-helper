import { getDb } from "@/db/db";
import { tasksTable } from "@/db/schema";
import { asNumber } from "@/utils/utils";
import { Task } from "@/db/types";
import { gte, eq, and, desc, inArray, count } from "drizzle-orm";
import { v4 as uuidv4 } from 'uuid';
import { queryWrap } from "@/utils/db-util";
import { TaskStatus } from "@/types/task";

export async function insertTask(task: typeof tasksTable.$inferInsert) {
  const db = getDb();
  return db.insert(tasksTable).values(task).returning();
}

export async function getUserTasks(user_email: string, created_from: Date, page: number, pageSize: number): Promise<{ tasks: Task[], total: number }> {
  const db = getDb();
  const total = await db.select({ count: count() }).from(tasksTable).where(
    and(
      eq(tasksTable.userEmail, user_email),
      gte(tasksTable.createdAt, created_from)
    )
  );
  const tasks = await queryWrap(db.select().from(tasksTable).where(
    and(
      eq(tasksTable.userEmail, user_email),
      gte(tasksTable.createdAt, created_from)
    )
  ).orderBy(desc(tasksTable.createdAt)).limit(pageSize).offset((page - 1) * pageSize));
  return {
    tasks,
    total: total[0].count
  }
}

export async function getFeaturedTasks(page: number, pageSize: number): Promise<{ tasks: Task[], total: number }> {
  const db = getDb();
  const where = eq(tasksTable.isFeatured, true);
  const total = await db.select({ count: count() }).from(tasksTable).where(where);
  const tasks = await queryWrap(db.select().from(tasksTable).where(where)
    .orderBy(desc(tasksTable.createdAt)).limit(pageSize).offset((page - 1) * pageSize));
  return {
    tasks,
    total: total[0].count
  }
}

export async function getUserTaskBilling(
  user_email: string,
  created_from: Date
) {
  const db = getDb();
  const rows = await queryWrap(db.select().from(tasksTable).where(
    and(
      eq(tasksTable.userEmail, user_email),
      eq(tasksTable.status, TaskStatus.Success),
      gte(tasksTable.createdAt, created_from)
    )
  ));

  const billing = { credits: 0, money: 0.0 }
  rows.forEach(row => {
    billing.credits += asNumber(row.consumedCredits);
    // billing.money += asNumber(row.consumedMoney);
  });

  return billing
}

export async function getUserTaskPending(
  user_email: string
) {
  const db = getDb();
  
  // const res = await db.query(
  //   `SELECT * FROM tasks WHERE user_email = $1 AND status IN ($2, $3)`,
  //   [user_email, TaskStatus.Pending, TaskStatus.Submitted]
  // );
  // return res.rows;
  return queryWrap(db.select().from(tasksTable).where(
    and(
      eq(tasksTable.userEmail, user_email),
      inArray(tasksTable.status, [TaskStatus.Pending, TaskStatus.Processing])
    )
  ));
}

export function genTaskId() {
  return 'vs' + uuidv4().slice(2);
}

export async function getTaskByUuid(uuid: string): Promise<Task | null> {
  const db = getDb();
  const tasks = await queryWrap(db.select().from(tasksTable).where(eq(tasksTable.uuid, uuid)));
  return tasks.length > 0 ? tasks[0] : null;
}