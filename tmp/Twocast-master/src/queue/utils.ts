export function getDelayTime() {
    let delay = 1000 * 15
    if (process.env.NODE_ENV === 'development') {
        delay = 1000 * 1
    }
    // return new Date(Date.now() + delay)
    return delay
}
