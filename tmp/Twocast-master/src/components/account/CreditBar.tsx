'use client'
import {useContext, useEffect, useState} from "react";
import {AppContext} from "src/contexts/AppContext";

interface CreditBarParams {
  initCredits: number;
}

export function CreditBar({initCredits}: CreditBarParams) {
  const {user, fetchUserInfo} = useContext(AppContext);
  const [credits, setCredits] = useState(initCredits);
  // let timer: NodeJS.Timeout | null = null;
  useEffect(() => {
    // console.log('user?.credits?.left_credits', [user?.credits?.left_credits])
    if (typeof user?.credits?.left_credits !== 'undefined') {
      setCredits(user?.credits?.left_credits)
    }
  }, [user, user?.credits, user?.credits?.left_credits])
  return (
    <div className="dark:text-white">Credits: {credits}</div>
  )
}