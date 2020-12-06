import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Modified example copied from stackoverflow
 * https://stackoverflow.com/questions/47156778/asynchronously-populating-a-java-map-and-returning-it-as-a-future
 */
public class AsynchronousMapPopulator {
    private final Executor backgroundJobExecutor;

    public AsynchronousMapPopulator(final Executor backgroundJobExecutor) {
        this.backgroundJobExecutor = backgroundJobExecutor;
    }

    public Future<Map<String, Integer>> apply(final Map<String, Integer> input) {
        final ConcurrentMap<String, Integer> result = new ConcurrentHashMap<>(input.size());
        final Stream.Builder<CompletableFuture<Void>> incrementingJobs = Stream.builder();
        for (final Map.Entry<String, Integer> entry : input.entrySet()) {
            final String key = entry.getKey();
            final Integer value = entry.getValue();
            final CompletableFuture<Void> incrementingJob = CompletableFuture.runAsync(() -> {
                result.put(key, value + 1);
            }, backgroundJobExecutor);
            incrementingJobs.add(incrementingJob);
        }
        // using thenApply instead of join here:
        return CompletableFuture.allOf(
                incrementingJobs.build().toArray(
                        CompletableFuture[]::new
                )
        ).thenApply(x -> result);
    }
}
