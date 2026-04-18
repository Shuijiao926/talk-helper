import { PodcastInputType } from "@/lib/podcast/types";

export type GenPodcastParams = {
    type: PodcastInputType
    text?: string
    file?: File
}