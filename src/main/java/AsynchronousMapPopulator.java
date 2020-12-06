import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

public class AsynchronousMapPopulator {
    private final Executor backgroundJobExecutor;

    public AsynchronousMapPopulator(final Executor backgroundJobExecutor) {
        this.backgroundJobExecutor = backgroundJobExecutor;
    }

    public Future<Map<String, Integer>> apply(final Map<String, Integer> input) {
        final ConcurrentMap<String, Integer> result = new ConcurrentHashMap<>(input.size());
        final Stream.Builder<CompletableFuture<Void>> incrementingJobs = Stream.builder();
        for (final Map.Entry<String, Integer> entry : input.entrySet()) {
            final String className = entry.getKey();
            final Integer oldValue = entry.getValue();
            final CompletableFuture<Void> incrementingJob = CompletableFuture.runAsync(() -> {
                result.put(className, oldValue + 1);
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

    public static void main (String agrs[]) throws ExecutionException, InterruptedException {
        Executor someExecutor = ForkJoinPool.commonPool();
        Map<String,Integer> wordClassObservations = Map.of("A", 1,"B", 2);
        Future<Map<String,Integer>> futureClassModels = new AsynchronousMapPopulator(someExecutor).apply(wordClassObservations);
// Do lots of other stuff
        Map<String,Integer> completedModels = futureClassModels.get();
        System.out.println(completedModels);
    }
}
