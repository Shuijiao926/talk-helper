#!/usr/bin/env npx tsx
import yargs from 'yargs';
import { hideBin } from 'yargs/helpers';
import { setupAudioQueue } from '@/queue/audio_queue';
import { setupLongTextQueue } from '@/queue/long_text_queue';
import { setupLinkQueue } from '@/queue/link_queue';
import { setupTopicQueue } from '@/queue/topic_queue';
import { setupFrontPageQueue } from '@/queue/front_page_queue';

const NODE_ENV = process.env.NODE_ENV
const path = NODE_ENV === 'production' ? ".env.production" : ".env"
console.log({ path })
require('dotenv').config({ path, override: true })

yargs(hideBin(process.argv))
  .command('start', 'start the queue', (yargs) => {
    return yargs
  }, async (argv) => {
    startQueue()
  })
  .option('verbose', {
    alias: 'v',
    type: 'boolean',
    description: 'Run with verbose logging'
  })
  .parse();

function startQueue() {
  setupFrontPageQueue()
  setupTopicQueue()
  setupLongTextQueue()
  setupAudioQueue()
  setupLinkQueue()
  console.log('Queue started', process.env.REDIS_HOST)
}