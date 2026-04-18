"use client";

import { useEffect, useRef } from "react";

// VoicePlayerButton 组件提前定义
export function VoicePlayerButton({ id, sample, label, playingVoiceId, setPlayingVoiceId, audioRefs }: {
  id: string;
  sample: string;
  label: string;
  playingVoiceId: string | null;
  setPlayingVoiceId: (id: string | null) => void;
  audioRefs: React.MutableRefObject<{ [key: string]: HTMLAudioElement | null }>;
}) {
  const isPlaying = playingVoiceId === id;
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const isInitialMount = useRef(true);

  // 添加调试日志监听 playingVoiceId 变化
  useEffect(() => {
    // console.log(`Voice ${id}: playingVoiceId changed to ${playingVoiceId}, isPlaying: ${isPlaying}`);
  }, [playingVoiceId, isPlaying, id]);

  // 专门监听 isPlaying 变化
  useEffect(() => {
    // console.log(`Voice ${id}: isPlaying changed to ${isPlaying}`);
  }, [isPlaying, id]);

  // 设置 audio ref 回调
  const setAudioRef = (element: HTMLAudioElement | null) => {
    // console.log('setAudioRef called for:', id, 'element:', !!element);
    audioRef.current = element;
    audioRefs.current[id] = element;
    // console.log('audioRefs.current[id] set to:', !!audioRefs.current[id]);
  };

  // 控制播放/暂停
  useEffect(() => {
    const checkIsPlaying = () => {
      if (isInitialMount.current) {
        isInitialMount.current = false;
        return;
      }
      const audio = audioRef.current;
      console.log('Audio useEffect triggered:', {
        id,
        isPlaying,
        hasAudio: !!audio,
        audioSrc: audio?.src,
        audioReadyState: audio?.readyState,
      });

      if (!audio) {
        // console.log('No audio element found for:', id);
        return;
      }

      if (isPlaying) {
        console.log('Attempting to play audio:', id, sample);

        // 简化播放逻辑，直接播放
        const playPromise = audio.play();
        if (playPromise !== undefined) {
          playPromise.catch(err => {
            console.error('Audio play failed:', err, {
              src: audio.src,
              readyState: audio.readyState,
              networkState: audio.networkState,
              error: audio.error
            });
            setPlayingVoiceId(null);
          });
        }
      } else {
        console.log('Pausing audio for:', id);
        audio.pause();
        audio.currentTime = 0;
      }
    }
    const timer = setTimeout(() => {
      checkIsPlaying()
    }, 1000)
    return () => clearTimeout(timer)
  }, [isPlaying, id, sample, setPlayingVoiceId]);

  // 播放结束后自动重置
  const handleEnded = () => {
    // console.log('Audio ended:', id);
    setPlayingVoiceId(null);
  };

  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    // 如果 sample 不存在，直接返回
    if (!sample) {
      return;
    }

    console.log('Button clicked:', id, 'isPlaying:', isPlaying, 'sample:', sample);
    console.log('Audio ref exists:', !!audioRef.current);
    console.log('Audio in refs:', !!audioRefs.current[id]);

    if (isPlaying) {
      console.log('Stopping playback for:', id);
      setPlayingVoiceId(null);
    } else {
      console.log('Starting playback for:', id);
      // Stop any other playing audio
      Object.entries(audioRefs.current).forEach(([key, audio]) => {
        if (audio && key !== id) {
          // console.log('Pausing other audio:', key);
          audio.pause();
          audio.currentTime = 0;
        }
      });

      const audio = audioRef.current;
      if (audio) {
        // Ensure the audio is loaded before playing
        if (audio.readyState < 2) { // HAVE_METADATA or more
          audio.load(); // Trigger loading if not already
        }
        audio.play()
          .then(() => {
            console.log('Setting playingVoiceId to:', id);
            setPlayingVoiceId(id);
          })
          .catch(err => {
            console.error('Audio play failed on click:', err);
            // If play fails, don't set it as playing.
            setPlayingVoiceId(null);
          });
      }
    }
  };

  // 清理函数
  useEffect(() => {
    return () => {
      if (audioRefs.current[id]) {
        audioRefs.current[id] = null;
      }
    };
  }, [id]);

  const hasSample = !!sample;

  return (
    <div className="flex items-center gap-2">
      {hasSample ? (
        <div
          onClick={handleClick}
          className="flex items-center justify-center w-8 h-8 rounded-full text-white shadow-md transition-all duration-200 focus:outline-none flex-shrink-0 bg-gradient-to-r from-indigo-500 to-purple-600 hover:from-indigo-600 hover:to-purple-700 hover:shadow-lg hover:scale-105 focus:ring-2 focus:ring-indigo-400 focus:ring-opacity-50 cursor-pointer"
        >
          {isPlaying ? (
            <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
              <path d="M6 4h4v16H6V4zm8 0h4v16h-4V4z" />
            </svg>
          ) : (
            <svg className="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
              <path d="M8 5v14l11-7z" />
            </svg>
          )}
        </div>
      ) : (
        // 没有音频样本时显示空白占位
        <div className="w-8 h-8 flex-shrink-0"></div>
      )}
      {hasSample && (
        <audio
          ref={setAudioRef}
          src={sample}
          onEnded={handleEnded}
          preload="metadata"
          onError={(e) => {
            console.error('Audio error for', id, ':', e.currentTarget.error);
          }}
          onLoadStart={() => {
            // console.log('Audio load start for:', id);
          }}
          onCanPlay={() => {
            // console.log('Audio can play for:', id);
          }}
          onLoadedData={() => {
            // console.log('Audio loaded data for:', id);
          }}
        />
      )}
      <span className="text-sm text-gray-700 dark:text-gray-300">{label}</span>
    </div>
  );
}
