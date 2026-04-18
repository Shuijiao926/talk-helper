'use client';

import { useEffect, useState, useRef, useContext } from 'react';
import { PlayIcon, PauseIcon } from '@heroicons/react/24/solid';
import { ClockIcon, ExclamationCircleIcon } from '@heroicons/react/24/outline';
import { TaskVO } from '@/lib/client-api/types/TaskVO';
import { apiRequest } from '@/lib/client-api/base';
import { toast } from 'sonner';
import { TaskStatus } from '@/types/task';
import { getLocalePath } from '@/utils/locale-util';
import { useTranslation } from 'react-i18next';
import Link from 'next/link';
import { AppContext } from '@/contexts/AppContext';
import { formatDuration } from '@/utils/time'
import { useAudioPlayer } from '@/contexts/AudioPlayerContext'

interface ListPanelProps {
  refreshTrigger?: number;
  apiUrl: string;
  showPagination?: boolean;
}

export function ListPanel({ refreshTrigger, apiUrl, showPagination = true }: ListPanelProps) {
  const { user } = useContext(AppContext);
  const { play, pause, isPlaying, currentTrack } = useAudioPlayer();
  const [podcasts, setPodcasts] = useState<TaskVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const { i18n } = useTranslation();
  const locale = i18n.language;

  const handlePlay = (podcast: TaskVO) => {
    if (podcast.status !== TaskStatus.Success || !podcast.result?.audio_url) {
      toast.error('音频尚未准备好');
      return;
    }

    if (isPlaying && currentTrack?.id === podcast.uuid) {
      pause();
    } else {
      play({
        id: podcast.uuid,
        url: podcast.result.audio_url,
        title: podcast.result.title || podcast.user_inputs?.topic || 'Untitled Podcast',
        // artist: 'Your Artist', // 如果有作者信息可以添加
        // thumbnail: 'your_thumbnail_url', // 如果有封面可以添加
        duration: podcast.result.duration
      });
    }
  };

  // Cleanup audio when component unmounts
  useEffect(() => {
    return () => {
      // No need to manually pause, global player handles its lifecycle
    };
  }, []);

  useEffect(() => {
    const fetchPodcasts = async () => {
      if (process.env.NEXT_PUBLIC_CLERK_ENABLED && !user) {
        setLoading(false);
        return;
      }

      try {
        const response = await apiRequest({
          url: `${apiUrl}?page=${page}&page_size=${pageSize}`,
          method: 'GET',
        });
        setPodcasts(response.data.data.items);
        setTotalPages(response.data.data.pagination.totalPages);

        // Check if there are any pending tasks
        const hasPendingTasks = response.data.data.items.some(
          (task: TaskVO) => task.status === 'pending'
        );

        // If there are pending tasks, set up a refresh after 5 seconds
        if (hasPendingTasks) {
          const timeoutId = setTimeout(() => {
            fetchPodcasts();
          }, 5000);
        }
      } catch (error) {
        console.error('Failed to fetch podcasts:', error);
      } finally {
        setLoading(false);
      }
    };

    const timer = setTimeout(() => {
      setLoading(true);
      fetchPodcasts();
    }, 1000);
    return () => clearTimeout(timer);
  }, [page, pageSize, refreshTrigger, user]);

  const getStatusIcon = (status: TaskStatus, podcast: TaskVO) => {
    switch (status) {
      case TaskStatus.Success:
        return (
          <div className="flex-shrink-0 w-16 h-16 bg-gradient-to-br from-indigo-500/90 to-purple-500/90 rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
            <button
              onClick={(e) => {
                e.stopPropagation();
                handlePlay(podcast);
              }}
              className="w-12 h-12 bg-white/20 backdrop-blur-sm rounded-xl flex items-center justify-center hover:bg-white/30 transition-colors"
            >
              {isPlaying && currentTrack?.id === podcast.uuid ? (
                <PauseIcon className="w-6 h-6 text-white" />
              ) : (
                <PlayIcon className="w-6 h-6 text-white" />
              )}
            </button>
          </div>
        );
      case TaskStatus.Pending:
      case TaskStatus.Processing:
        return (
          <div className="flex-shrink-0 w-16 h-16 bg-gradient-to-br from-yellow-500/90 to-orange-500/90 rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
            <div className="w-12 h-12 bg-white/20 backdrop-blur-sm rounded-xl flex items-center justify-center">
              <ClockIcon className="w-6 h-6 text-white animate-pulse" />
            </div>
          </div>
        );
      case TaskStatus.Failed:
        return (
          <div className="flex-shrink-0 w-16 h-16 bg-gradient-to-br from-red-500/90 to-pink-500/90 rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
            <div className="w-12 h-12 bg-white/20 backdrop-blur-sm rounded-xl flex items-center justify-center">
              <ExclamationCircleIcon className="w-6 h-6 text-white" />
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[200px]">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-4 md:grid md:grid-cols-2 md:gap-4 md:max-w-6xl md:mx-auto relative">
      {podcasts.map((podcast) => (
        <div
          key={podcast.uuid}
          className="group relative flex gap-4 p-4 bg-gradient-to-r from-white/90 to-gray-50/80 dark:from-gray-700/90 dark:to-gray-800/80 backdrop-blur-sm rounded-3xl hover:scale-[1.02] transition-all duration-300"
          style={{
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.4)'
          }}
        >
          {/* Left side - Status Icon (独立的，不受Link影响) */}
          {getStatusIcon(podcast.status, podcast)}

          {/* Right side - Content (可点击跳转) */}
          <Link
            href={getLocalePath(locale, `/podcast/${podcast.uuid}`)}
            className="flex-1 min-w-0 cursor-pointer"
          >
            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-1 truncate">
              {podcast.result?.title || podcast.user_inputs?.topic || 'Untitled Podcast'}
            </h3>
            <div className="mt-2 flex items-center gap-2 flex-wrap">
              <span className="px-2 py-1 text-xs font-medium bg-gradient-to-r from-indigo-50/80 to-purple-50/60 dark:from-indigo-900/40 dark:to-purple-900/30 rounded-lg text-indigo-600 dark:text-indigo-300">
                {podcast.status_human}
              </span>
              <span className="text-xs text-gray-500 dark:text-gray-400">
                {new Date(podcast.created_at || '').toLocaleDateString()}
              </span>
              {/* 显示音频时长 */}
              {podcast.result?.duration && (
                <span className="px-2 py-1 text-xs font-medium bg-gradient-to-r from-emerald-50/80 to-teal-50/60 dark:from-emerald-900/40 dark:to-teal-900/30 rounded-lg text-emerald-600 dark:text-emerald-300">
                  {formatDuration(podcast.result.duration)}
                </span>
              )}
            </div>
          </Link>
        </div>
      ))}

      {/* Pagination Controls */}
      {showPagination && podcasts.length > 0 && <div className="col-span-1 md:col-span-2 flex justify-center items-center gap-4 mt-8">
        <button
          onClick={() => setPage(p => Math.max(1, p - 1))}
          disabled={page === 1}
          className="px-4 py-2 bg-gradient-to-r from-indigo-500/90 to-purple-500/90 text-white rounded-xl disabled:opacity-50 disabled:cursor-not-allowed hover:scale-105 transition-all duration-300"
        >
          Previous
        </button>
        <span className="text-gray-600 dark:text-gray-400">
          Page {page} of {totalPages}
        </span>
        <button
          onClick={() => setPage(p => Math.min(totalPages, p + 1))}
          disabled={page === totalPages}
          className="px-4 py-2 bg-gradient-to-r from-indigo-500/90 to-purple-500/90 text-white rounded-xl disabled:opacity-50 disabled:cursor-not-allowed hover:scale-105 transition-all duration-300"
        >
          Next
        </button>
      </div>
      }
    </div>
  );
}