import { QueryPromise } from "drizzle-orm"

type QueryError = Error & { code?: unknown }

/**
 * 包装查询函数，捕获查询错误并返回错误信息
 * 
 * 使用方法：
 * 
 * const users = await queryWrap(getDb().select().from(userTable).orderBy(desc(userTable.createdAt)))
 * const users = await queryWrap(getDb().insert(userTable).values(user).returning({ id: userTable.id }))
 * 
 * @param promise 查询语句
 * @returns 查询结果或错误信息
 */
export async function queryWrap<D, P extends QueryPromise<D>>(promise: P) {
	try {
		const result = await promise
		return result
	} catch (origErr) {
		// replace traces
        console.error(origErr)
		const error = new Error('DB query error')
		// origErr.stack = error.stack?.split('\n').slice(2).join('\n')
		origErr.stack = error.stack?.split('\n').slice(2).join('\n')
		throw origErr
	}
}