'use client'

import {useContext, useEffect, useState} from "react";
import {AppContext} from "@/contexts/AppContext";
import {useRouter} from "next/navigation";
import {apiRequest} from "../../lib/client-api/base";
import {toast} from "sonner";
import {BeatLoader} from "react-spinners";
import {ErrorCodes} from "@/utils/constants";

export default function Editor() {
  const [text, setText] = useState('')
  const [result, setResult] = useState('')
  const [loading, setLoading] = useState(false)
  const [wordCnt, setWordCnt] = useState(0)
  const {user, fetchUserInfo} = useContext(AppContext)
  const router = useRouter()
  const humanize = async () => {
    console.log('text', user, text)
    if (!text) {
      toast.error('Please enter some text')
      return
    }
    if (!user || !user.id) {
      localStorage.setItem('ai-text', text)
      router.push('/sign-in')
      return
    }
    setResult('')
    setLoading(true)
    try {
      const resp = await apiRequest({
        url: '/api/protected/humanize',
        method: 'POST',
        data: {
          text,
        }
      })
      console.log('resp', resp)
      setResult(resp.data.data.text)
      fetchUserInfo() // 更新界面上显示的 credits
    } catch (e) {
      console.error('humanize failed', e)
      if (e?.response?.data.code === ErrorCodes.CREDITS_INSUFFICIENT) {
        // toast.error('Insufficient credits')
        router.push('/pricing')
      }
    }
    setLoading(false)
  }
  const onChange = async (e) => {
    setText(e.target.value)
    let cnt = 0
    if (e.target.value.trim()) {
      cnt = e.target.value.trim().split(/\s+/).length
    }
    setWordCnt(cnt)
  }

  useEffect(() => {
    const text = localStorage.getItem('ai-text')
    if (text) {
      setText(text)
      localStorage.removeItem('ai-text')
    }
  }, [])

  return (
    <div className="flex flex-col w-[90%] lg:w-[80%] mx-auto pb-[70px]">
      <div className="dark:text-white border-0 border-slate-600 rounded-lg">
        <div>
          <textarea name="" id="" rows={10} className="w-full rounded-lg" value={text} onChange={onChange}></textarea>
          <div className="flex flex-col justify-end">
            <div>{wordCnt} / words</div>
          </div>
        </div>
      </div>
      <div className="flex items-center justify-center">
        <button disabled={loading} onClick={humanize} className="cursor-pointer md:py-[12px] md:px-[24px] main-cta-btn py-[9px] px-[17px] rounded-lg flex items-center w-fit justify-center gap-[0.6rem] bg-[#7E4BFA] hover:bg-[#8858FF]  transition duration-150 ease-in-out text-base font-medium md:font-semibold leading-[24px] text-white border-[1.8px] border-[#C2A8FF] hover:text-white">
          {loading ? <BeatLoader color="#fff"/> : <div>Humanize</div>}
        </button>
      </div>
      {result &&
          <div className="dark:text-white border-0 border-slate-600 rounded-lg mt-2">
              <div>
                  <textarea name="" id="" rows={10} className="w-full rounded-lg" defaultValue={result} readOnly={true}></textarea>
              </div>
          </div>
      }
    </div>
  )
}