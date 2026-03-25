/** 통계 상단 — 숫자 전에 동기·피드백 톤 */
export type FeedbackIntroProps = {
  headline: string
  body: string
}

export function FeedbackIntro({ headline, body }: FeedbackIntroProps) {
  return (
    <section className="an-feedback" aria-labelledby="an-feedback-title">
      <h2 id="an-feedback-title" className="an-feedback__title">
        {headline}
      </h2>
      <p className="an-feedback__body">{body}</p>
    </section>
  )
}
