import { defineConfig } from 'drizzle-kit';

const NODE_ENV = process.env.NODE_ENV
const path = NODE_ENV === 'production' ? ".env.production" : ".env"
console.log({ path })
require('dotenv').config({ path, override: true })
console.log({ DATABASE_URL: process.env.DATABASE_URL })

export default defineConfig({
  out: './drizzle',
  schema: './src/db/schema.ts',
  dialect: 'postgresql',
  dbCredentials: {
    url: process.env.DATABASE_URL!,
  },
});
