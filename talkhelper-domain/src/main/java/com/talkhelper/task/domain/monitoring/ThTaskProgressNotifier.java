package com.talkhelper.task.domain.monitoring;

import com.talkhelper.task.dto.ThTaskProgressDTO;

public interface ThTaskProgressNotifier {

    void notifyTaskProgress(String taskId, ThTaskProgressDTO progress);
}
