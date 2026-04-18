import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function asNumber(value: string | number | null | undefined, defaultValue: number = 0): number {
  let parsedValue = defaultValue;
  if (typeof value === 'number') {
    parsedValue = value;
  } else if (typeof value === 'string') {
    try {
      parsedValue = parseFloat(value);
    } catch (e) {
      console.error(e);
    }
  } else {
    parsedValue = defaultValue;
  }
  return isNaN(parsedValue) ? defaultValue : parsedValue;
}