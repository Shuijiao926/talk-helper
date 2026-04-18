export enum ErrorCodes {
  CREDITS_INSUFFICIENT = 1001,
  TEXT_TOO_LONG = 1002,
  DAILY_LIMIT_EXCEEDED = 1003,
  INVALID_PARAMS = 1004,
  TOO_MANY_RUNNING_JOBS = 1005,
  ILLEGAL_IMAGE = 1006,

  // remote 
  REMOTE_GENERATION_FAILED = 2006,
  REMOTE_SERVER_ERROR = 2500,
}

export const ErrorMessages = {
  [ErrorCodes.CREDITS_INSUFFICIENT]: "credits insufficient",
  [ErrorCodes.TEXT_TOO_LONG]: "text too long",
  [ErrorCodes.DAILY_LIMIT_EXCEEDED]: "daily limit exceeded",
}

export const DEFAULT_CONSUMED_CREDITS = 3;

export enum CreditType {
  OneTime = 'one-time',
  Periodic = 'periodic',
}

export enum Period {
  Month = 'month',
  Day = 'day',
  Year = 'year',
  OneTime = 'one-time',
}
