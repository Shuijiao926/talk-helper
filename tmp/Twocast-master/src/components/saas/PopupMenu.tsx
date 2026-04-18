'use client'

import {useState} from "react";

export const PopupMenu = (props: {isOpen: boolean}) => {
  const isOpen = useState(props.isOpen)
  return (
    <>
      Hello! I am a PopupMenu.{isOpen}
    </>
  )
}