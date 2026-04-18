import {ErrorMessages} from "@/utils/constants";
import { getErrMsg } from "./err";

export function respData(data: any) {
  return respJson({code: 0, message: "ok", data: data, status: 200});
}

export function respOk() {
  return respJson({code: 0, message: "ok", status: 200});
}

export function respErr(message: string) {
  return respJson({code: 1, message: message, status: 400});
}

export function respErrCode(code: number) {
  // return respJson(code, ErrorMessages[code] || "error")
  return respJson({code: code, message: getErrMsg(code), status: 400});
}


interface respJsonParams {
  code: number
  message: string
  data?: any
  status?: number
}

export function respJson(argv: respJsonParams) {
  const json = {
    code: argv.code,
    message: argv.message,
    data: argv.data,
  };
  if (argv.data) {
    json["data"] = argv.data;
  }

  return Response.json(json, {status: argv.status});
}

export function respSuccess(data: any) {
  return respJson({code: 0, message: "ok", data: data, status: 200});
}