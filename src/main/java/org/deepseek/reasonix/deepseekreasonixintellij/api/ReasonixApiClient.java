package org.deepseek.reasonix.deepseekreasonixintellij.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;
import org.deepseek.reasonix.deepseekreasonixintellij.ReasonixService;

import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ReasonixApiClient {
    public static final String BASE_URL = "http://" + ReasonixService.COMMAND_ARGS[ReasonixService.COMMAND_ARGS.length - 1];
    private static final Logger LOG = Logger.getInstance(ReasonixApiClient.class);
    private final HttpClient httpClient;
    private final ExecutorService executorService;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final ObjectMapper objectMapper;
    private Consumer<ApiEvent> eventConsumer;

    public ReasonixApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        this.executorService = Executors.newSingleThreadExecutor();
        this.objectMapper = new ObjectMapper();
    }

    public void setEventConsumer(Consumer<ApiEvent> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public void connect() {
        connected.set(true);
        executorService.submit(this::listenToSSE);
    }

    public void disconnect() {
        connected.set(false);
    }

    private void listenToSSE() {
        LOG.info("Listening to SSE: " + BASE_URL);
        while (connected.get()) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/events"))
                        .header("Accept", "text/event-stream")
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();

                LOG.info("Attempting SSE connection to: " + BASE_URL + "/events");

                HttpResponse<java.util.stream.Stream<String>> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofLines());

                if (response.statusCode() == 200) {
                    LOG.info("SSE connection established");
                    processSSELines(response.body());
                } else {
                    LOG.warn("SSE connection failed with status: " + response.statusCode());
                }
            } catch (java.net.ConnectException e) {
                LOG.info("SSE connection refused - service may not be ready yet, retrying...");
            } catch (java.io.UncheckedIOException e) {
                LOG.info("SSE stream closed");
            } catch (Exception e) {
                if (connected.get()) {
                    LOG.warn("SSE connection error: " + e.getMessage());
                }
            }

            if (connected.get()) {
                try {
                    LOG.info("Reconnecting in 3 seconds...");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        LOG.info("SSE connection closed");
    }

    private void processSSELines(Stream<String> lines) {
        StringBuilder eventData = new StringBuilder();

        try {
            Iterator<String> iterator = lines.iterator();
            while (iterator.hasNext() && connected.get()) {
                String line = iterator.next();
                LOG.debug("Received SSE line: " + line);
                if (line.startsWith("data:")) {
                    eventData.append(line.substring(5));
                } else if (line.isEmpty() && !eventData.isEmpty()) {
                    String json = eventData.toString().trim();
                    if (!json.isEmpty() && eventConsumer != null) {
                        try {
                            ApiEvent event = parseEvent(json);
                            if (event != null) {
                                eventConsumer.accept(event);
                            }
                        } catch (Exception e) {
                            LOG.warn("Failed to parse SSE event: " + json);
                        }
                    }
                    eventData.setLength(0);
                }
            }
        } catch (UncheckedIOException e) {
            if (connected.get()) {
                LOG.info("SSE stream ended, will reconnect");
            } else {
                LOG.info("SSE stream closed by client");
            }
        }
    }

    private ApiEvent parseEvent(String json) {
        try {
            return objectMapper.readValue(json, ApiEvent.class);
        } catch (JsonProcessingException e) {
            LOG.warn("Failed to parse event JSON: " + json, e);
            return null;
        }
    }

    public ApiResult submit(String input) {
        return postJson("/submit", new SubmitRequest(input));
    }

    public ApiResult cancel() {
        return postJson("/cancel", null);
    }

    public ApiResult approve(String id, boolean allow, boolean session) {
        return approve(id, allow, session, false);
    }

    public ApiResult approve(String id, boolean allow, boolean session, boolean persist) {
        return postJson("/approve", new ApproveRequest(id, allow, session, persist));
    }

    public ApiResult plan(boolean on) {
        return postJson("/plan", new PlanRequest(on));
    }

    public ApiResult compact() {
        return postJson("/compact", null);
    }

    public ApiResult newSession() {
        return postJson("/new", null);
    }

    public ApiResult rewind(int turn) {
        return rewind(turn, "both");
    }

    public ApiResult rewind(int turn, String scope) {
        return postJson("/rewind", new RewindRequest(turn, scope));
    }

    public ForkResult fork(int turn) {
        return fork(turn, null);
    }

    public ForkResult fork(int turn, String name) {
        return postJsonAndParse("/fork", new ForkRequest(turn, name), ForkResult.class);
    }

    public ApiResult summarize(int turn, String mode) {
        return postJson("/summarize", new SummarizeRequest(turn, mode));
    }

    public ApiResult bypass(boolean on) {
        return postJson("/bypass", new BypassRequest(on));
    }

    public ApiResult answer(String id, List<AskAnswer> answers) {
        return postJson("/answer", new AnswerRequest(id, answers));
    }

    public ApiResult resume(String path) {
        return postJson("/resume", new ResumeRequest(path));
    }

    public ApiResult forget(String name) {
        return postJson("/forget", new ForgetRequest(name));
    }

    public List<HistoryMessage> getHistory() {
        return httpGet("/history", new TypeReference<>() {
        });
    }


    public ContextInfo getContext() {
        return httpGet("/context", new TypeReference<>() {
        });
    }

    public List<Checkpoint> getCheckpoints() {
        return httpGet("/checkpoints", new TypeReference<>() {
        });
    }


    public BranchesInfo getBranches() {
        return httpGet("/branches", new TypeReference<>() {
        });
    }

    public StatusInfo getStatus() {
        return httpGet("/status", new TypeReference<>() {
        });
    }

    public List<SessionInfo> getSessions() {
        return httpGet("/sessions", new TypeReference<>() {
        });
    }

    public ApiResult loadSession(String sessionId) {
        return postJson("/session/load", new LoadSessionRequest(sessionId));
    }

    public List<SkillInfo> getSkills() {
        return httpGet("/skills", new TypeReference<>() {
        });
    }

    private <T> T httpGet(String path, TypeReference<T> valueTypeRef) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), valueTypeRef);
            }
        } catch (Exception e) {
            LOG.warn("Failed to get " + path, e);
        }
        return null;
    }

    private <T> ApiResult postJson(String path, T body) {
        try {
            String json = body != null ? objectMapper.writeValueAsString(body) : "{}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return new ApiResult(response.statusCode(), response.body());
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize JSON body", e);
            return new ApiResult(-1, "Failed to serialize request body");
        } catch (java.net.http.HttpTimeoutException e) {
            LOG.error("POST request timed out: " + path, e);
            return new ApiResult(-1, "Request timed out");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ApiResult(-1, "Request interrupted");
        } catch (Exception e) {
            LOG.error("POST request failed: " + path, e);
            return new ApiResult(-1, e.getMessage());
        }
    }

    private <T> T postJsonAndParse(String path, Object body, Class<T> resultType) {
        ApiResult result = postJson(path, body);
        if (result.isSuccess() && result.response() != null) {
            try {
                return objectMapper.readValue(result.response(), resultType);
            } catch (JsonProcessingException e) {
                LOG.warn("Failed to parse " + path + " result", e);
            }
        }
        return null;
    }

    public void shutdown() {
        disconnect();
        executorService.shutdownNow();
    }

    public record ApiResult(int code, String response) {
        public boolean isSuccess() {
            return code == 200 || code == 204;
        }
    }

    public static class SubmitRequest {
        public String input;

        public SubmitRequest(String input) {
            this.input = input;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }
    }

    public static class ApproveRequest {
        public String id;
        public boolean allow;
        public boolean session;
        public boolean persist;

        public ApproveRequest(String id, boolean allow, boolean session) {
            this.id = id;
            this.allow = allow;
            this.session = session;
            this.persist = false;
        }

        public ApproveRequest(String id, boolean allow, boolean session, boolean persist) {
            this.id = id;
            this.allow = allow;
            this.session = session;
            this.persist = persist;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isAllow() {
            return allow;
        }

        public void setAllow(boolean allow) {
            this.allow = allow;
        }

        public boolean isSession() {
            return session;
        }

        public void setSession(boolean session) {
            this.session = session;
        }

        public boolean isPersist() {
            return persist;
        }

        public void setPersist(boolean persist) {
            this.persist = persist;
        }
    }

    public static class PlanRequest {
        public boolean on;

        public PlanRequest(boolean on) {
            this.on = on;
        }

        public boolean isOn() {
            return on;
        }

        public void setOn(boolean on) {
            this.on = on;
        }
    }

    public static class RewindRequest {
        public int turn;
        public String scope;

        public RewindRequest(int turn, String scope) {
            this.turn = turn;
            this.scope = scope;
        }

        public int getTurn() {
            return turn;
        }

        public void setTurn(int turn) {
            this.turn = turn;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }
    }

    public static class ForkRequest {
        public int turn;
        public String name;

        public ForkRequest(int turn, String name) {
            this.turn = turn;
            this.name = name;
        }

        public int getTurn() {
            return turn;
        }

        public void setTurn(int turn) {
            this.turn = turn;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class SummarizeRequest {
        public int turn;
        public String mode;

        public SummarizeRequest(int turn, String mode) {
            this.turn = turn;
            this.mode = mode;
        }

        public int getTurn() {
            return turn;
        }

        public void setTurn(int turn) {
            this.turn = turn;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    public static class BypassRequest {
        public boolean on;

        public BypassRequest(boolean on) {
            this.on = on;
        }

        public boolean isOn() {
            return on;
        }

        public void setOn(boolean on) {
            this.on = on;
        }
    }

    public static class AnswerRequest {
        public String id;
        public List<AskAnswer> answers;

        public AnswerRequest(String id, List<AskAnswer> answers) {
            this.id = id;
            this.answers = answers;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<AskAnswer> getAnswers() {
            return answers;
        }

        public void setAnswers(List<AskAnswer> answers) {
            this.answers = answers;
        }
    }

    public static class ResumeRequest {
        public String path;

        public ResumeRequest(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class ForgetRequest {
        public String name;

        public ForgetRequest(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class LoadSessionRequest {
        public String id;

        public LoadSessionRequest(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}