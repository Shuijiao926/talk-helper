'use client';

import React, { createContext, useContext, useState, useRef, useEffect, ReactNode } from 'react';
import { toast } from 'sonner';

// 音频轨道接口
interface AudioTrack {
  id: string;
  url: string;
  title: string;
  artist?: string;
  thumbnail?: string;
  duration?: number;
}

// 播放器状态接口
interface AudioPlayerState {
  currentTrack: AudioTrack | null;
  isPlaying: boolean;
  isLoading: boolean;
  currentTime: number;
  duration: number;
  volume: number;
  isMuted: boolean;
  isVisible: boolean;
  playbackRate: number; // 播放速度
}

// 播放器控制接口
interface AudioPlayerControls {
  play: (track: AudioTrack) => Promise<void>;
  pause: () => void;
  resume: () => void;
  seek: (time: number) => void;
  setVolume: (volume: number) => void;
  toggleMute: () => void;
  hide: () => void;
  show: () => void;
  setPlaybackRate: (rate: number) => void; // 设置播放速度
}

// Context类型
interface AudioPlayerContextType extends AudioPlayerState, AudioPlayerControls {}

const AudioPlayerContext = createContext<AudioPlayerContextType | undefined>(undefined);

// Provider组件
export function AudioPlayerProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AudioPlayerState>({
    currentTrack: null,
    isPlaying: false,
    isLoading: false,
    currentTime: 0,
    duration: 0,
    volume: 1,
    isMuted: false,
    isVisible: false,
    playbackRate: 1, // 默认播放速度为1
  });

  const audioRef = useRef<HTMLAudioElement>(null);

  // 播放新音频
  const play = async (track: AudioTrack) => {
    if (!audioRef.current) return;

    try {
      setState(prev => ({ 
        ...prev, 
        isLoading: true, 
        currentTrack: track, 
        isVisible: true,
        // 如果track有duration预设值，先使用它
        duration: track.duration || prev.duration
      }));
      
      // 如果是新的音频文件，重新加载
      if (audioRef.current.src !== track.url) {
        audioRef.current.src = track.url;
        await new Promise((resolve) => {
          const handleCanPlay = () => {
            audioRef.current?.removeEventListener('canplay', handleCanPlay);
            resolve(void 0);
          };
          audioRef.current?.addEventListener('canplay', handleCanPlay);
        });
      }
      
      // 同步播放速度
      audioRef.current.playbackRate = state.playbackRate;
      await audioRef.current.play();
    } catch (error) {
      console.error('播放失败:', error);
      toast.error('音频播放失败，请稍后重试');
      setState(prev => ({ ...prev, isLoading: false, isPlaying: false }));
    }
  };

  // 暂停播放
  const pause = () => {
    if (audioRef.current && !audioRef.current.paused) {
      audioRef.current.pause();
    }
  };

  // 恢复播放
  const resume = async () => {
    if (!audioRef.current) return;
    
    try {
      setState(prev => ({ ...prev, isLoading: true }));
      await audioRef.current.play();
    } catch (error) {
      console.error('恢复播放失败:', error);
      toast.error('音频播放失败，请稍后重试');
      setState(prev => ({ ...prev, isLoading: false }));
    }
  };

  // 跳转到指定时间
  const seek = (time: number) => {
    if (audioRef.current) {
      audioRef.current.currentTime = time;
      setState(prev => ({ ...prev, currentTime: time }));
    }
  };

  // 设置音量
  const setVolume = (volume: number) => {
    if (audioRef.current) {
      audioRef.current.volume = volume;
      setState(prev => ({ 
        ...prev, 
        volume, 
        isMuted: volume === 0 
      }));
    }
  };

  // 切换静音
  const toggleMute = () => {
    if (!audioRef.current) return;
    
    setState(prev => {
      const newIsMuted = !prev.isMuted;
      audioRef.current!.volume = newIsMuted ? 0 : prev.volume;
      return { ...prev, isMuted: newIsMuted };
    });
  };

  // 隐藏播放器
  const hide = () => {
    setState(prev => ({ ...prev, isVisible: false }));
  };

  // 显示播放器
  const show = () => {
    setState(prev => ({ ...prev, isVisible: true }));
  };

  // 设置播放速度
  const setPlaybackRate = (rate: number) => {
    if (audioRef.current) {
      audioRef.current.playbackRate = rate;
      setState(prev => ({ ...prev, playbackRate: rate }));
    }
  };

  // 音频事件监听
  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;

    const handleLoadedMetadata = () => {
      // 优先使用音频元素的实际duration，但保留预设值作为后备
      const actualDuration = audio.duration && !isNaN(audio.duration) ? audio.duration : undefined;
      setState(prev => ({ 
        ...prev, 
        duration: actualDuration || prev.duration || 0 
      }));
    };

    const handleTimeUpdate = () => {
      setState(prev => ({ ...prev, currentTime: audio.currentTime }));
    };

    const handlePlay = () => {
      setState(prev => ({ ...prev, isPlaying: true, isLoading: false }));
    };

    const handlePause = () => {
      setState(prev => ({ ...prev, isPlaying: false }));
    };

    const handleEnded = () => {
      setState(prev => ({ ...prev, isPlaying: false, currentTime: 0 }));
    };

    const handleError = (e: Event) => {
      const error = (e.target as HTMLAudioElement).error;
      let errorMessage = '音频播放失败';
      
      if (error) {
        switch (error.code) {
          case MediaError.MEDIA_ERR_NETWORK:
            errorMessage += ': 网络错误';
            break;
          case MediaError.MEDIA_ERR_DECODE:
            errorMessage += ': 音频格式不支持';
            break;
          case MediaError.MEDIA_ERR_SRC_NOT_SUPPORTED:
            errorMessage += ': 音频源不支持';
            break;
          default:
            errorMessage += ': 未知错误';
        }
      }
      
      toast.error(errorMessage);
      setState(prev => ({ ...prev, isPlaying: false, isLoading: false }));
    };

    // 监听播放速度变化（外部设置时同步到audio元素）
    audio.playbackRate = state.playbackRate;

    audio.addEventListener('loadedmetadata', handleLoadedMetadata);
    audio.addEventListener('timeupdate', handleTimeUpdate);
    audio.addEventListener('play', handlePlay);
    audio.addEventListener('pause', handlePause);
    audio.addEventListener('ended', handleEnded);
    audio.addEventListener('error', handleError);

    return () => {
      audio.removeEventListener('loadedmetadata', handleLoadedMetadata);
      audio.removeEventListener('timeupdate', handleTimeUpdate);
      audio.removeEventListener('play', handlePlay);
      audio.removeEventListener('pause', handlePause);
      audio.removeEventListener('ended', handleEnded);
      audio.removeEventListener('error', handleError);
    };
  }, []);

  const contextValue: AudioPlayerContextType = {
    ...state,
    play,
    pause,
    resume,
    seek,
    setVolume,
    toggleMute,
    hide,
    show,
    setPlaybackRate, // 暴露设置播放速度方法
  };

  return (
    <AudioPlayerContext.Provider value={contextValue}>
      {children}
      <audio ref={audioRef} preload="metadata" />
    </AudioPlayerContext.Provider>
  );
}

// Hook for using the audio player context
export function useAudioPlayer() {
  const context = useContext(AudioPlayerContext);
  if (context === undefined) {
    throw new Error('useAudioPlayer must be used within an AudioPlayerProvider');
  }
  return context;
} 