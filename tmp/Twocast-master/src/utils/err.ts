import { ErrorCodes, ErrorMessages } from "./constants"

export class MyError extends Error {
    code: number
    message: string
    detail?: string
    innerDetail?: string // 内部敏感信息，不返回给客户端, toString() 时也不会返回

    constructor(code: number, detail?: string, innerDetail?: string) {
        super();

        let message = getErrMsg(code)

        this.code = code
        this.message = message
        this.detail = detail || ''
        this.innerDetail = innerDetail || ''
    }

    public toString() {
        return JSON.stringify({
            code: this.code,
            message: this.message,
            detail: this.detail,
        })
    }
}


export function getErrMsg(code: number) {
    let message = 'unknown error'
    if (ErrorMessages[code]) {
        message = ErrorMessages[code]
    } else {
        for (const key in ErrorCodes) {
            if (ErrorCodes[key as keyof typeof ErrorCodes] === code) {
                message = key.replace(/_/g, ' ').toLocaleLowerCase().replace(/\b\w/g, char => char.toUpperCase()).trim();
                break
            }
        }
    }
    return message
}