import Redis from 'ioredis';

let redis: Redis;

const PREFIX = process.env.NEXT_PUBLIC_SITE_URL?.split('://')[1];

export function getRedis() {
    if (!redis) {
        redis = new Redis({
            host: process.env.REDIS_HOST || 'localhost',
            port: Number(process.env.REDIS_PORT) || 6379,
            password: process.env.REDIS_PASSWORD,
            retryStrategy: (times) => {
                const delay = Math.min(times * 50, 2000);
                return delay;
            },
            maxRetriesPerRequest: null,
        });

        redis.on('error', (err) => {
            console.error('Redis connection error:', err);
        });

        redis.on('connect', () => {
            console.log('Redis connected successfully');
        });
    }
    return redis;
}

// Helper function to get cached data
export async function getCache<T>(key: string): Promise<T | null> {
    const redis = getRedis();
    const data = await redis.get(`${PREFIX}:${key}`);
    return data ? JSON.parse(data) : null;
}

// Helper function to set cache with expiration
export async function setCache(key: string, value: any, expireSeconds: number = 60): Promise<void> {
    const redis = getRedis();
    await redis.set(`${PREFIX}:${key}`, JSON.stringify(value), 'EX', expireSeconds);
}

// Helper function to delete cache
export async function deleteCache(key: string): Promise<void> {
    const redis = getRedis();
    await redis.del(`${PREFIX}:${key}`);
} 