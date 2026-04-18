import { useTranslation } from "@/i18n/client";
import { useParams } from "next/navigation";
import { LocaleTypes } from "@/i18n/settings";

export const RelatedTweets = () => {
  const locale = useParams()?.locale as LocaleTypes
  const { t } = useTranslation(locale, 'home')
  // const t = useTranslations('related-tweets')

  return (
    <section id="related-tweets" className="related-tweets">
      <div className="container">
        <div className="section-header">
          <h2>{t('related-tweets.h2')}</h2>
        </div>
        <ul className="tweet-list w-full row row-cols-1 row-cols-lg-2 row-cols-md-2 px-0">
          <li className="tweet-one break-inside-avoid">
            <blockquote className="twitter-tweet"><p lang="en" dir="ltr">“With my machines I can dispatch you from this world without a trace. Our nostalgia ghosts are ready to take your place.”<br/><br/>None of Them Knew …<br/><br/>This image was created using AI-Comic-Factory. <a href="https://t.co/mOdHXzWxn1">pic.twitter.com/mOdHXzWxn1</a></p>&mdash; Chad Mairn (@cmairn) <a href="https://twitter.com/cmairn/status/1701617366382358607?ref_src=twsrc%5Etfw">September 12, 2023</a></blockquote>
          </li>
          <li className="tweet-one break-inside-avoid">
            <blockquote className="twitter-tweet"><p lang="en" dir="ltr">AI comics meet Javascript. <br/><br/>Prompt: &quot;business men making jokes about javascript&quot;<br/><br/>Generated using jbilcke-hf/ai-comic-factory model <a href="https://t.co/IaTPJUMRlm">pic.twitter.com/IaTPJUMRlm</a></p>&mdash; Charlie Greenman (@razroo_chief) <a href="https://twitter.com/razroo_chief/status/1761697374320075174?ref_src=twsrc%5Etfw">February 25, 2024</a></blockquote>
          </li>
          <li className="tweet-one break-inside-avoid">
            <blockquote className="twitter-tweet"><p lang="en" dir="ltr">Tried the ai-comic-factory space. <br/><br/>Story: An alien lands on Earth, eager to conquer. It pulls out a laser gun, but a grandma mistakes it for a TV remote and grabs it. She clicks a button, and the alien is teleported back to its planet. <a href="https://t.co/TqjPuMuQqA">pic.twitter.com/TqjPuMuQqA</a></p>&mdash; Akhil Ivaturi (@GordianKnot256) <a href="https://twitter.com/GordianKnot256/status/1716904579269624126?ref_src=twsrc%5Etfw">October 24, 2023</a></blockquote>
          </li>
          <li className="tweet-one break-inside-avoid">
            <blockquote className="twitter-tweet"><p lang="en" dir="ltr">Art and captions generated with Ai Comic Factory: <a href="https://twitter.com/hashtag/aiart?src=hash&amp;ref_src=twsrc%5Etfw">#aiart</a> <a href="https://twitter.com/hashtag/comicsgate?src=hash&amp;ref_src=twsrc%5Etfw">#comicsgate</a> <a href="https://t.co/LxBExRWnZ8">pic.twitter.com/LxBExRWnZ8</a></p>&mdash; Sheila Aliens 👽 ⁂ (@Sheilaaliens) <a href="https://twitter.com/Sheilaaliens/status/1763974556212170797?ref_src=twsrc%5Etfw">March 2, 2024</a></blockquote>
          </li>
          <li className="tweet-one break-inside-avoid">
            <blockquote className="twitter-tweet"><p lang="en" dir="ltr">Testing the AI comic factory by <a href="https://twitter.com/flngr?ref_src=twsrc%5Etfw">@flngr</a> <br/><br/>So nice! And such a high quality when upscaled! <a href="https://t.co/gHZbEXxCJm">pic.twitter.com/gHZbEXxCJm</a></p>&mdash; ToneDice (@JosiTonedice) <a href="https://twitter.com/JosiTonedice/status/1734821119373279700?ref_src=twsrc%5Etfw">December 13, 2023</a></blockquote>
          </li>
          <li className="tweet-one break-inside-avoid">
            <blockquote className="twitter-tweet"><p lang="en" dir="ltr">do you like comics? huggingface has a comic generator that can generate comics in various styles. this one is in Japanese style and i only provided the text: guy in a studio full of synthesizers. here&#39;s the ai-comic-factory -  <a href="https://t.co/CIxknjvlOz">pic.twitter.com/CIxknjvlOz</a></p>&mdash; jmac4207 (@jmac4207) <a href="https://twitter.com/jmac4207/status/1697245054866477155?ref_src=twsrc%5Etfw">August 31, 2023</a></blockquote>
          </li>
          <li className="tweet-one break-inside-avoid">
            <blockquote className="twitter-tweet"><p lang="en" dir="ltr">The AI comic factory is pretty amazing. <a href="https://t.co/pPDY5GBo3z">pic.twitter.com/pPDY5GBo3z</a></p>&mdash; Ray (@kennethray25) <a href="https://twitter.com/kennethray25/status/1724485238821376293?ref_src=twsrc%5Etfw">November 14, 2023</a></blockquote>
          </li>
          <li className="tweet-one break-inside-avoid">
            <blockquote className="twitter-tweet"><p lang="en" dir="ltr">I remember being greatly inspired by &#39;Zarya of the Dawn&#39; by <a href="https://twitter.com/icreatelife?ref_src=twsrc%5Etfw">@icreatelife</a> and &#39;AI Comic Factory&#39; by <a href="https://twitter.com/flngr?ref_src=twsrc%5Etfw">@flngr</a>. <br/><br/>However, achieving consistency and control was always a challenge, which I overcame using <a href="https://twitter.com/Scenario_gg?ref_src=twsrc%5Etfw">@Scenario_gg</a> .<br/><br/>Here&#39;s my method: <a href="https://t.co/pCGzaqnjn1">pic.twitter.com/pCGzaqnjn1</a></p>&mdash; Rodrigo (@rodrigon) <a href="https://twitter.com/rodrigon/status/1744923377276502322?ref_src=twsrc%5Etfw">January 10, 2024</a></blockquote>
          </li>
          <li className="tweet-one break-inside-avoid">
            <blockquote className="twitter-tweet"><p lang="en" dir="ltr">Holy #%3&amp;! - exploring <a href="https://twitter.com/huggingface?ref_src=twsrc%5Etfw">@huggingface</a> and tried th AI Comic Factory application. I am just blown away. There is so much I need to unpack but first want to amplify the creator <a href="https://twitter.com/flngr?ref_src=twsrc%5Etfw">@flngr</a> - Follow them for updates.<br/><br/><br/><br/>&quot;Lion goes for walk in Central Park&quot; <a href="https://t.co/LhRt2BsVAk">pic.twitter.com/LhRt2BsVAk</a></p>&mdash; Nitya Narasimhan, PhD (@nitya) <a href="https://twitter.com/nitya/status/1708518085232062895?ref_src=twsrc%5Etfw">October 1, 2023</a></blockquote>
          </li>
          <li className="tweet-one break-inside-avoid">
            <blockquote className="twitter-tweet"><p lang="en" dir="ltr">AI Comic Factory is a free tool that can create entire pages with multiple panels in different styles, based on your input.<br/><br/> <a href="https://t.co/EXoP9tGh3u">pic.twitter.com/EXoP9tGh3u</a></p>&mdash; Alex Utopia (@alexutopia) <a href="https://twitter.com/alexutopia/status/1706456486082232591?ref_src=twsrc%5Etfw">September 25, 2023</a></blockquote>
          </li>
          <li className="tweet-one break-inside-avoid">
            <blockquote className="twitter-tweet"><p lang="en" dir="ltr">Recently, I got a take home assignment. I tried having some fun and generated this quick Tintin style comic for user story.🫢<br/><br/>Much credit to for this cool AI comic factory <a href="https://twitter.com/flngr?ref_src=twsrc%5Etfw">@flngr</a> <a href="https://twitter.com/huggingface?ref_src=twsrc%5Etfw">@huggingface</a> <a href="https://t.co/TG3C9cfHYd">pic.twitter.com/TG3C9cfHYd</a></p>&mdash; Shrayana (@ShrayanaRay) <a href="https://twitter.com/ShrayanaRay/status/1740274354683080830?ref_src=twsrc%5Etfw">December 28, 2023</a></blockquote>
          </li>
        </ul>
        <script async src="https://platform.twitter.com/widgets.js" charSet="utf-8" defer></script>
      </div>
    </section>
  )
}
