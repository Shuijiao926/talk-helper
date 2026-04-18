import { ReactNode } from 'react'

interface SectionContainerProps {
  children: ReactNode
}

export default function SectionWideContainer({ children }: SectionContainerProps) {
  return (
    <section className="mx-auto">{children}</section>
  )
}
