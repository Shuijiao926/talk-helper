package com.roamingguide.starter.otel;

/**
 * AI 领域语义属性常量定义
 * 基于 OpenTelemetry GenAI Semantic Conventions 扩展
 * 用于在 Span 中标注 AI 相关的上下文信息
 *
 * @see <a href="https://opentelemetry.io/docs/specs/semconv/gen-ai/">OTel GenAI Semantic Conventions</a>
 */
public final class AiSemanticAttributes {

    private AiSemanticAttributes() {
    }

    // --- LLM 调用语义 ---

    /** LLM 供应商标识, e.g. "dashscope", "openai" */
    public static final String GEN_AI_SYSTEM = "gen_ai.system";

    /** 请求使用的模型名称, e.g. "qwen3-max" */
    public static final String GEN_AI_REQUEST_MODEL = "gen_ai.request.model";

    /** 温度参数 */
    public static final String GEN_AI_REQUEST_TEMPERATURE = "gen_ai.request.temperature";

    /** Top-P 参数 */
    public static final String GEN_AI_REQUEST_TOP_P = "gen_ai.request.top_p";

    /** 最大输出 Token 数 */
    public static final String GEN_AI_REQUEST_MAX_TOKENS = "gen_ai.request.max_tokens";

    /** 完整 Prompt 内容 (注意脱敏) */
    public static final String GEN_AI_PROMPT = "gen_ai.prompt";

    /** LLM 生成的完整输出 */
    public static final String GEN_AI_COMPLETION = "gen_ai.completion";

    /** 输入 Token 数 */
    public static final String GEN_AI_USAGE_INPUT_TOKENS = "gen_ai.usage.input_tokens";

    /** 输出 Token 数 */
    public static final String GEN_AI_USAGE_OUTPUT_TOKENS = "gen_ai.usage.output_tokens";

    /** 总 Token 数 */
    public static final String GEN_AI_USAGE_TOTAL_TOKENS = "gen_ai.usage.total_tokens";

    /** 首 Token 响应时间 (ms) - Time To First Token */
    public static final String GEN_AI_PERFORMANCE_TTFT_MS = "gen_ai.performance.ttft_ms";

    /** Token 间隔时间 (ms) - Time Per Output Token */
    public static final String GEN_AI_PERFORMANCE_TPOT_MS = "gen_ai.performance.tpot_ms";

    /** LLM 调用总耗时 (ms) */
    public static final String GEN_AI_PERFORMANCE_DURATION_MS = "gen_ai.performance.duration_ms";

    /** 是否调用成功 */
    public static final String GEN_AI_RESPONSE_SUCCESS = "gen_ai.response.success";

    /** 错误信息 */
    public static final String GEN_AI_RESPONSE_ERROR = "gen_ai.response.error";

    // --- TTS 语音合成语义 ---

    /** TTS 模型名称, e.g. "cosyvoice-v3-flash" */
    public static final String TTS_MODEL = "tts.model";

    /** TTS 音色名称, e.g. "longxiaochun_v3" */
    public static final String TTS_VOICE = "tts.voice";

    /** TTS 输入文本长度 (字符数) */
    public static final String TTS_INPUT_TEXT_LENGTH = "tts.input.text_length";

    /** TTS 输出音频大小 (bytes) */
    public static final String TTS_OUTPUT_AUDIO_BYTES = "tts.output.audio_bytes";

    /** TTS 输出音频格式 */
    public static final String TTS_OUTPUT_FORMAT = "tts.output.format";

    /** TTS 合成耗时 (ms) */
    public static final String TTS_DURATION_MS = "tts.duration_ms";

    /** TTS 合成角色 */
    public static final String TTS_ROLE = "tts.role";

    // --- Agent 编排语义 ---

    /** Agent 服务名称, e.g. "talk-helper" */
    public static final String AGENT_NAME = "agent.name";

    /** Agent 当前执行的动作, e.g. "podcast-generate" */
    public static final String AGENT_ACTION = "agent.action";

    /** Agent 工作流步骤序号 */
    public static final String AGENT_WORKFLOW_STEP = "agent.workflow.step";

    /** Agent 工作流步骤名称, e.g. "text-preprocess", "script-gen", "tts", "audio-merge" */
    public static final String AGENT_WORKFLOW_STEP_NAME = "agent.workflow.step_name";

    /** MCP 工具名称 */
    public static final String AGENT_TOOL_NAME = "agent.tool.name";

    /** MCP 工具参数 */
    public static final String AGENT_TOOL_PARAMS = "agent.tool.params";

    // --- 任务/会话语义 ---

    /** 会话 ID */
    public static final String SESSION_ID = "session.id";

    /** 用户 ID */
    public static final String USER_ID = "user.id";

    /** 对话轮次 */
    public static final String SESSION_TURN = "session.turn";

    /** 任务 ID */
    public static final String TASK_ID = "task.id";

    /** 任务类型 */
    public static final String TASK_TYPE = "task.type";

    /** 任务状态 */
    public static final String TASK_STATUS = "task.status";

    // --- Span 名称常量 ---

    public static final String SPAN_LLM_CHAT = "llm.chat";
    public static final String SPAN_TTS_SYNTHESIZE = "tts.synthesize";
    public static final String SPAN_TASK_PROCESS = "task.process";
    public static final String SPAN_TASK_TEXT_PREPROCESS = "task.text_preprocess";
    public static final String SPAN_TASK_SCRIPT_GEN = "task.script_gen";
    public static final String SPAN_TASK_AUDIO_GEN = "task.audio_gen";
    public static final String SPAN_TASK_AUDIO_MERGE = "task.audio_merge";
}
