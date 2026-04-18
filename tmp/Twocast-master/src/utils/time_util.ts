export async function sleep(ms: number) {
  return new Promise(resolve => setTimeout(resolve, ms));
}
export async function waitFor<T>(fn: () => T | Promise<T>, interval = 1000): Promise<T> {
  while (true) {
    const result = await fn();
    if (result) {
      return result;
    }
    await sleep(interval);
  }
}