package com.hku.nook.service;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.model.ChatCompletionCreateParams;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ChatFunction;
import ai.z.openapi.service.model.ChatFunctionParameterProperty;
import ai.z.openapi.service.model.ChatFunctionParameters;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import ai.z.openapi.service.model.ChatTool;
import ai.z.openapi.service.model.ChatToolType;
import ai.z.openapi.service.model.Choice;
import ai.z.openapi.service.model.Delta;
import ai.z.openapi.service.model.ModelData;
import ai.z.openapi.service.model.ToolCalls;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.hku.nook.domain.AiChatMessage;
import com.hku.nook.domain.AiChatRequest;
import com.hku.nook.domain.AiChatResponse;
import com.hku.nook.domain.AiChatSource;
import com.hku.nook.domain.Video;
import com.hku.nook.service.util.HttpUtil;
import org.apache.mahout.cf.taste.common.TasteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiAgentService.class);

    private static final String TOOL_SEARCH_SITE_VIDEOS = "search_site_videos";
    private static final String TOOL_RECOMMEND_VIDEOS = "recommend_videos";
    private static final String TOOL_TAVILY_SEARCH = "tavily_search";
    private static final String TOOL_MESSAGE_ROLE = "tool";
    private static final String BASE_SYSTEM_PROMPT = "你是Nook的AI助手。\n"
            + "重要规则：\n"
            + "1. 当用户请求新闻、站内搜索或推荐时或者你无法直接回答用户时，必须先使用工具获取信息，然后基于工具返回的内容生成完整、结构化的回答。\n"
            + "2. 用户明确提到本站/站内/本平台时，必须先调用search_site_videos；只有站内结果不足时才调用tavily_search补充。\n"
            + "3. 综合搜索结果，用自己的话总结和回答用户的问题。\n"
            + "4. 回答要准确、简洁、格式清晰，不必在回答中包含URL链接（系统会单独展示来源）。\n"
            + "5. 不要凭空编造信息。";

    @Autowired
    private ZhipuAiClient zhipuAiClient;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private VideoService videoService;

    @Value("${ai.zhipu.model:glm-4.5-air}")
    private String model;

    @Value("${ai.zhipu.api-key:}")
    private String apiKey;

    @Value("${ai.tavily.api-key:}")
    private String tavilyApiKey;

    @Value("${ai.tavily.search-depth:advanced}")
    private String tavilySearchDepth;

    @Value("${ai.tavily.max-results:20}")
    private int tavilyMaxResults;

    @Value("${ai.search.min-site-results:1}")
    private int minSiteResults;

    @Value("${ai.zhipu.tool.max-iterations:5}")
    private int maxToolIterations;

    public void streamChat(AiChatRequest request, SseEmitter emitter, Long userId) {
        try {
            if (request == null || CollectionUtils.isEmpty(request.getMessages())) {
                emitter.send(SseEmitter.event().name("error").data("消息不能为空"));
                emitter.complete();
                return;
            }
            if (apiKey == null || apiKey.trim().isEmpty()) {
                emitter.send(SseEmitter.event().name("error").data("未配置智谱API Key"));
                emitter.complete();
                return;
            }
            List<ChatMessage> messages = buildMessages(request.getMessages());
            List<ChatTool> tools = buildTools(getLastUserContent(request.getMessages()));

            List<AiChatSource> sources = new ArrayList<>();
            ToolCallResult toolCallResult = runToolLoop(messages, tools, sources, userId,
                    getLastUserContent(request.getMessages()));
            if (toolCallResult.assistantContent != null) {
                emitContentInChunks(emitter, toolCallResult.assistantContent);
                emitter.send(SseEmitter.event().name("sources").data(dedupeSources(sources)));
                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
                return;
            }
            if (toolCallResult.hasToolCalls) {
                String fallbackContent = generateFinalAnswer(messages);
                if (fallbackContent != null) {
                    emitContentInChunks(emitter, fallbackContent);
                }
            } else {
                boolean hasTokens = streamAnswer(messages, sources, emitter);
                if (!hasTokens) {
                    String fallbackContent = generateFinalAnswer(messages);
                    if (fallbackContent != null) {
                        emitContentInChunks(emitter, fallbackContent);
                    }
                }
            }
            emitter.send(SseEmitter.event().name("sources").data(dedupeSources(sources)));
            emitter.send(SseEmitter.event().name("done").data(""));
            emitter.complete();
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data(e.getMessage()));
            } catch (IOException ignored) {
                // ignore send error
            }
            emitter.completeWithError(e);
        }
    }

    public AiChatResponse chat(AiChatRequest request, Long userId) throws IOException {
        if (request == null || CollectionUtils.isEmpty(request.getMessages())) {
            throw new IllegalArgumentException("messages required");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Zhipu API key required");
        }
        List<ChatMessage> messages = buildMessages(request.getMessages());
        List<ChatTool> tools = buildTools(getLastUserContent(request.getMessages()));
        List<AiChatSource> sources = new ArrayList<>();
        ToolCallResult toolCallResult = runToolLoop(messages, tools, sources, userId,
                getLastUserContent(request.getMessages()));
        String content = toolCallResult.assistantContent;
        if (content == null) {
            content = generateFinalAnswer(messages);
        }
        com.hku.nook.domain.AiChatResponse result = new com.hku.nook.domain.AiChatResponse();
        result.setContent(content);
        result.setSources(dedupeSources(sources));
        return result;
    }

    private List<ChatMessage> buildMessages(List<AiChatMessage> input) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM.value())
                .content(buildSystemPrompt())
                .build());
        for (AiChatMessage message : input) {
            if (message == null || message.getContent() == null) {
                continue;
            }
            messages.add(ChatMessage.builder()
                    .role(normalizeRole(message.getRole()))
                    .content(message.getContent())
                    .build());
        }
        return messages;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return ChatMessageRole.USER.value();
        }
        String value = role.trim().toLowerCase();
        if ("system".equals(value)) {
            return ChatMessageRole.SYSTEM.value();
        }
        if ("assistant".equals(value)) {
            return ChatMessageRole.ASSISTANT.value();
        }
        if ("function".equals(value)) {
            return ChatMessageRole.FUNCTION.value();
        }
        return ChatMessageRole.USER.value();
    }

    private String buildSystemPrompt() {
        return "【系统信息】今天的日期是：" + LocalDate.now() + "（" +
                LocalDate.now().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.CHINA) +
                "）\n\n" + BASE_SYSTEM_PROMPT;
    }

    private String getLastUserContent(List<AiChatMessage> messages) {
        if (messages == null) {
            return null;
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            AiChatMessage message = messages.get(i);
            if (message != null && "user".equalsIgnoreCase(message.getRole())) {
                return message.getContent();
            }
        }
        return messages.get(messages.size() - 1) != null ? messages.get(messages.size() - 1).getContent() : null;
    }

    private List<ChatTool> buildTools(String searchQuery) {
        List<ChatTool> tools = new ArrayList<>();
        Map<String, ChatFunctionParameterProperty> properties = new LinkedHashMap<>();
        properties.put("query", ChatFunctionParameterProperty.builder()
                .type("string")
                .description("搜索关键词")
                .build());
        properties.put("limit", ChatFunctionParameterProperty.builder()
                .type("integer")
                .description("返回结果数量，默认5")
                .build());
        ChatFunctionParameters parameters = ChatFunctionParameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("query"))
                .build();
        ChatFunction function = ChatFunction.builder()
                .name(TOOL_SEARCH_SITE_VIDEOS)
                .description("使用ES在Nook站内搜索视频或者用户，尽可能多的使用关键字来提高搜索准确度")
                .parameters(parameters)
                .build();
        tools.add(ChatTool.builder()
                .type(ChatToolType.FUNCTION.value())
                .function(function)
                .build());
        Map<String, ChatFunctionParameterProperty> tavilyProperties = new LinkedHashMap<>();
        tavilyProperties.put("query", ChatFunctionParameterProperty.builder()
                .type("string")
                .description("搜索关键词")
                .build());
        tavilyProperties.put("max_results", ChatFunctionParameterProperty.builder()
                .type("integer")
                .description("返回结果数量，默认5")
                .build());
        tavilyProperties.put("search_depth", ChatFunctionParameterProperty.builder()
                .type("string")
                .description("搜索深度，可选 basic/advanced")
                .build());
        ChatFunctionParameters tavilyParameters = ChatFunctionParameters.builder()
                .type("object")
                .properties(tavilyProperties)
                .required(Arrays.asList("query"))
                .build();
        ChatFunction tavilyFunction = ChatFunction.builder()
                .name(TOOL_TAVILY_SEARCH)
                .description("使用Tavily进行联网搜索，返回网页结果。仅在站内搜索结果不足时使用。")
                .parameters(tavilyParameters)
                .build();
        tools.add(ChatTool.builder()
                .type(ChatToolType.FUNCTION.value())
                .function(tavilyFunction)
                .build());
        Map<String, ChatFunctionParameterProperty> recommendProperties = new LinkedHashMap<>();
        recommendProperties.put("size", ChatFunctionParameterProperty.builder()
                .type("integer")
                .description("推荐数量，默认5")
                .build());
        ChatFunctionParameters recommendParameters = ChatFunctionParameters.builder()
                .type("object")
                .properties(recommendProperties)
                .build();
        ChatFunction recommendFunction = ChatFunction.builder()
                .name(TOOL_RECOMMEND_VIDEOS)
                .description("推荐当前登录用户感兴趣的视频")
                .parameters(recommendParameters)
                .build();
        tools.add(ChatTool.builder()
                .type(ChatToolType.FUNCTION.value())
                .function(recommendFunction)
                .build());
        return tools;
    }

    private ToolCallResult runToolLoop(List<ChatMessage> messages,
            List<ChatTool> tools,
            List<AiChatSource> sources,
            Long userId,
            String userQuery) throws IOException {
        ToolCallResult result = new ToolCallResult();
        int iterations = Math.max(1, maxToolIterations);
        for (int i = 0; i < iterations; i++) {
            ChatCompletionCreateParams.ChatCompletionCreateParamsBuilder<?, ?> builder = ChatCompletionCreateParams
                    .builder()
                    .model(model)
                    .messages(messages)
                    .tools(tools)
                    .stream(false);
            builder.toolChoice("auto");
            ChatCompletionCreateParams params = builder.build();
            ChatCompletionResponse response = zhipuAiClient.chat().createChatCompletion(params);
            ModelData data = response.getData();
            if (data == null || CollectionUtils.isEmpty(data.getChoices())) {
                return result;
            }
            ChatMessage assistantMessage = data.getChoices().get(0).getMessage();
            if (assistantMessage == null || CollectionUtils.isEmpty(assistantMessage.getToolCalls())) {
                if (assistantMessage != null && assistantMessage.getContent() != null) {
                    result.assistantContent = String.valueOf(assistantMessage.getContent());
                }
                return result;
            }
            result.hasToolCalls = true;
            if (assistantMessage.getRole() == null || assistantMessage.getRole().trim().isEmpty()) {
                assistantMessage.setRole(ChatMessageRole.ASSISTANT.value());
            }
            messages.add(assistantMessage);
            for (ToolCalls toolCall : assistantMessage.getToolCalls()) {
                if (toolCall == null || toolCall.getFunction() == null) {
                    continue;
                }
                String toolName = toolCall.getFunction().getName();
                JsonNode arguments = toolCall.getFunction().getArguments();
                log.info("工具调用 - 工具名称: {}, 参数: {}, 参数类型: {}", toolName, arguments,
                        arguments != null ? arguments.getNodeType() : "null");
                if (TOOL_SEARCH_SITE_VIDEOS.equals(toolName)) {
                    // 处理参数：如果是字符串类型的JSON，需要先解析
                    JSONObject argsJson = null;
                    if (arguments != null) {
                        if (arguments.isTextual()) {
                            // 如果是字符串，先解析成JSON对象
                            argsJson = JSONObject.parseObject(arguments.asText());
                        } else if (arguments.isObject()) {
                            // 如果已经是对象，转换成JSON
                            argsJson = JSONObject.parseObject(arguments.toString());
                        }
                    }
                    String query = argsJson != null ? argsJson.getString("query") : null;
                    Integer limit = argsJson != null ? argsJson.getInteger("limit") : null;
                    log.info("解析后的参数 - query: {}, limit: {}", query, limit);
                    List<Map<String, Object>> videoResults = elasticSearchService.searchVideos(query, limit);
                    log.info("ES搜索结果 - 查询关键词: {}, 返回数量: {}, 结果: {}", query, videoResults.size(), videoResults);

                    JSONArray list = new JSONArray();
                    for (Map<String, Object> video : videoResults) {
                        JSONObject item = new JSONObject();
                        item.put("id", video.get("id"));
                        item.put("title", video.get("title"));
                        item.put("description", video.get("description"));
                        item.put("url", video.get("url"));
                        list.add(item);
                        addSource(sources, "nook", String.valueOf(video.get("title")),
                                String.valueOf(video.get("url")));
                    }
                    JSONObject resultJson = new JSONObject();
                    resultJson.put("results", list);
                    JSONArray webList = new JSONArray();
                    if (list.size() < Math.max(1, minSiteResults)) {
                        TavilyResult tavily = tavilySearch(query + " 视频", tavilyMaxResults);
                        if (tavily != null && tavily.results != null) {
                            for (TavilyItem item : tavily.results) {
                                JSONObject obj = new JSONObject();
                                obj.put("title", item.title);
                                obj.put("url", item.url);
                                obj.put("content", item.content);
                                webList.add(obj);
                                addSource(sources, "web", item.title, item.url);
                            }
                        }
                    }
                    if (!webList.isEmpty()) {
                        resultJson.put("webResults", webList);
                    }
                    ChatMessage functionMessage = ChatMessage.builder()
                            .role(TOOL_MESSAGE_ROLE)
                            .name(toolName)
                            .toolCallId(toolCall.getId())
                            .content(resultJson.toJSONString())
                            .build();
                    messages.add(functionMessage);
                } else if (TOOL_TAVILY_SEARCH.equals(toolName)) {
                    // 处理参数
                    JSONObject argsJson = null;
                    if (arguments != null) {
                        if (arguments.isTextual()) {
                            argsJson = JSONObject.parseObject(arguments.asText());
                        } else if (arguments.isObject()) {
                            argsJson = JSONObject.parseObject(arguments.toString());
                        }
                    }
                    String query = argsJson != null ? argsJson.getString("query") : null;
                    if (isBlank(query)) {
                        query = null;
                    }
                    Integer limit = argsJson != null ? argsJson.getInteger("max_results") : null;
                    String depth = argsJson != null ? argsJson.getString("search_depth") : null;
                    TavilyResult tavily = tavilySearch(query, limit != null && limit > 0 ? limit : tavilyMaxResults,
                            depth);
                    JSONObject resultJson = new JSONObject();
                    JSONArray list = new JSONArray();
                    if (tavily != null && tavily.results != null) {
                        for (TavilyItem item : tavily.results) {
                            JSONObject obj = new JSONObject();
                            obj.put("title", item.title);
                            obj.put("url", item.url);
                            obj.put("content", item.content);
                            list.add(obj);
                            addSource(sources, "web", item.title, item.url);
                        }
                    }
                    resultJson.put("results", list);
                    ChatMessage functionMessage = ChatMessage.builder()
                            .role(TOOL_MESSAGE_ROLE)
                            .name(toolName)
                            .toolCallId(toolCall.getId())
                            .content(resultJson.toJSONString())
                            .build();
                    messages.add(functionMessage);
                } else if (TOOL_RECOMMEND_VIDEOS.equals(toolName)) {
                    // 处理参数
                    JSONObject argsJson = null;
                    if (arguments != null) {
                        if (arguments.isTextual()) {
                            argsJson = JSONObject.parseObject(arguments.asText());
                        } else if (arguments.isObject()) {
                            argsJson = JSONObject.parseObject(arguments.toString());
                        }
                    }
                    Integer limit = argsJson != null ? argsJson.getInteger("size") : null;
                    int size = limit != null && limit > 0 ? limit : 5;
                    log.info("推荐视频 - userId: {}, size: {}", userId, size);
                    JSONObject resultJson = new JSONObject();
                    JSONArray list = new JSONArray();
                    if (userId == null) {
                        log.warn("推荐视频失败 - userId为null");
                        resultJson.put("error", "userId required");
                    } else {
                        try {
                            log.info("开始调用videoService.recommend(userId={})", userId);
                            List<Video> videos = videoService.recommend(userId);
                            log.info("推荐结果 - 视频数量: {}", videos != null ? videos.size() : 0);
                            if (videos != null) {
                                int count = Math.min(size, videos.size());
                                for (int index = 0; index < count; index++) {
                                    Video video = videos.get(index);
                                    JSONObject item = new JSONObject();
                                    item.put("id", video.getId());
                                    item.put("title", video.getTitle());
                                    item.put("description", video.getDescription());
                                    item.put("url", video.getUrl());
                                    list.add(item);
                                    addSource(sources, "nook", video.getTitle(), video.getUrl());
                                }
                            }
                        } catch (TasteException e) {
                            log.error("推荐视频异常 - userId: {}, error: {}", userId, e.getMessage(), e);
                            resultJson.put("error", e.getMessage());
                        } catch (Exception e) {
                            log.error("推荐视频未知异常 - userId: {}, error: {}", userId, e.getMessage(), e);
                            resultJson.put("error", e.getMessage());
                        }
                    }
                    resultJson.put("results", list);
                    log.info("推荐视频返回结果 - results数量: {}, resultJson: {}", list.size(), resultJson);
                    ChatMessage functionMessage = ChatMessage.builder()
                            .role(TOOL_MESSAGE_ROLE)
                            .name(toolName)
                            .toolCallId(toolCall.getId())
                            .content(resultJson.toJSONString())
                            .build();
                    messages.add(functionMessage);
                }
            }
        }
        return result;
    }

    private TavilyResult tavilySearch(String query, int maxResults) throws IOException {
        return tavilySearch(query, maxResults, tavilySearchDepth);
    }

    private TavilyResult tavilySearch(String query, int maxResults, String searchDepth) throws IOException {
        if (isBlank(query)) {
            return null;
        }
        if (tavilyApiKey == null || tavilyApiKey.trim().isEmpty()) {
            return null;
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("query", query);
        params.put("search_depth", searchDepth != null && !searchDepth.trim().isEmpty()
                ? searchDepth
                : tavilySearchDepth);
        params.put("max_results", maxResults);
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put("Authorization", "Bearer " + tavilyApiKey);
        HttpUtil.HttpResponse response;
        try {
            response = HttpUtil.postJson("https://api.tavily.com/search", params, headers);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
        if (response == null || response.getBody() == null) {
            return null;
        }
        JSONObject json = JSONObject.parseObject(response.getBody());
        JSONArray results = json.getJSONArray("results");
        if (results == null) {
            return null;
        }
        List<TavilyItem> items = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            JSONObject item = results.getJSONObject(i);
            TavilyItem tavilyItem = new TavilyItem();
            tavilyItem.title = item.getString("title");
            tavilyItem.url = item.getString("url");
            tavilyItem.content = item.getString("content");
            items.add(tavilyItem);
        }
        TavilyResult result = new TavilyResult();
        result.results = items;
        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean streamAnswer(List<ChatMessage> messages,
            List<AiChatSource> sources,
            SseEmitter emitter) throws IOException {
        ChatCompletionCreateParams.ChatCompletionCreateParamsBuilder<?, ?> builder = ChatCompletionCreateParams
                .builder()
                .model(model)
                .messages(messages)
                .stream(true);
        builder.toolChoice("none");
        ChatCompletionResponse response = zhipuAiClient.chat().createChatCompletion(builder.build());
        final boolean[] hasTokens = { false };
        response.getFlowable().blockingForEach(data -> {
            if (data == null || CollectionUtils.isEmpty(data.getChoices())) {
                return;
            }
            for (Choice choice : data.getChoices()) {
                Delta delta = choice.getDelta();
                if (delta != null && delta.getContent() != null) {
                    try {
                        emitter.send(SseEmitter.event().name("token").data(delta.getContent()));
                        hasTokens[0] = true;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        return hasTokens[0];
    }

    private String generateFinalAnswer(List<ChatMessage> messages) throws IOException {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(model)
                .messages(messages)
                .stream(false)
                .toolChoice("none")
                .build();
        ChatCompletionResponse response = zhipuAiClient.chat().createChatCompletion(params);
        ModelData data = response.getData();
        if (data != null && !CollectionUtils.isEmpty(data.getChoices())) {
            ChatMessage assistant = data.getChoices().get(0).getMessage();
            if (assistant != null && assistant.getContent() != null) {
                return String.valueOf(assistant.getContent());
            }
        }
        return null;
    }

    private void emitContentInChunks(SseEmitter emitter, String content) throws IOException {
        if (content == null || content.isEmpty()) {
            return;
        }
        int chunkSize = 20;
        for (int i = 0; i < content.length(); i += chunkSize) {
            int end = Math.min(content.length(), i + chunkSize);
            emitter.send(SseEmitter.event().name("token").data(content.substring(i, end)));
        }
    }

    private void addSource(List<AiChatSource> sources, String type, String title, String url) {
        if (url == null || "null".equals(url)) {
            return;
        }
        sources.add(new AiChatSource(type, title, url));
    }

    private List<AiChatSource> dedupeSources(List<AiChatSource> sources) {
        if (sources == null) {
            return new ArrayList<>();
        }
        Map<String, AiChatSource> map = new LinkedHashMap<>();
        for (AiChatSource source : sources) {
            if (source == null || source.getUrl() == null) {
                continue;
            }
            map.put(source.getUrl(), source);
        }
        return new ArrayList<>(map.values());
    }

    private static class ToolCallResult {
        private boolean hasToolCalls;
        private String assistantContent;
    }

    private static class TavilyResult {
        private List<TavilyItem> results;
    }

    private static class TavilyItem {
        private String title;
        private String url;
        private String content;
    }
}
