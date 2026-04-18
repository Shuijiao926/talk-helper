import { getAxiosInstance } from "./http"

export async function queryChat(query: string, options: { json: boolean } = { json: false }) {
    if (!process.env.LLM_API_KEY) {
        throw new Error("LLM_API_KEY is not set")
    }
    if (!process.env.LLM_CHAT_URL) {
        throw new Error("LLM_CHAT_URL is not set")
    }
    if (!process.env.LLM_CHAT_MODEL) {
        throw new Error("LLM_CHAT_MODEL is not set")
    }
    const url = process.env.LLM_CHAT_URL!
    const headers = {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${process.env.LLM_API_KEY}`
    }
    const payload: any = {
        "messages": [
            {
                "role": "user",
                "content": query
            }
        ],
        "search_parameters": {
            "mode": "auto"
        },
        "model": process.env.LLM_CHAT_MODEL!
    }
    if (options.json) {
        payload.response_format = {
            type: "json_object"
        }
    }
    return getAxiosInstance().post(url, payload, { headers })
}

export async function querySearch(query: string, options: { json: boolean } = { json: false }) {
    if (!process.env.LLM_SEARCH_API_KEY) {
        throw new Error("LLM_SEARCH_API_KEY is not set")
    }
    if (!process.env.LLM_SEARCH_URL) {
        throw new Error("LLM_SEARCH_URL is not set")
    }
    if (!process.env.LLM_SEARCH_MODEL) {
        throw new Error("LLM_SEARCH_MODEL is not set")
    }
    const url = process.env.LLM_SEARCH_URL!
    const headers = {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${process.env.LLM_SEARCH_API_KEY}`
    }
    const payload: any = {
        "messages": [
            {
                "role": "user",
                "content": query
            }
        ],
        "search_parameters": {
            "mode": "auto"
        },
        "model": process.env.LLM_SEARCH_MODEL!
    }
    if (options.json) {
        payload.response_format = {
            type: "json_object"
        }
    }
    return getAxiosInstance().post(url, payload, { headers })
}