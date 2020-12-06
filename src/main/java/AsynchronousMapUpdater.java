import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class AsynchronousMapUpdater {
    private final Executor backgroundJobExecutor;
    final ConcurrentMap<String, Integer> result = new ConcurrentHashMap<>();

    public Map<String, Integer> getResult() {
        return result;
    }

    public AsynchronousMapUpdater(final Executor backgroundJobExecutor) {
        this.backgroundJobExecutor = backgroundJobExecutor;
    }

    public Future<Map<String, Integer>> apply(final String input) {
        final Stream.Builder<CompletableFuture<Void>> mapReduces = Stream.builder();
        final CompletableFuture<Void> mapReduce = getMapReduceFuture(input);
        mapReduces.add(mapReduce);
        return getMapFuture(mapReduces);
    }

    //able accept different function
    public Future<Map<String, Integer>> apply(final String input, final Function<String, CompletableFuture<Void>> compute) {
        final Stream.Builder<CompletableFuture<Void>> mapReduces = Stream.builder();
        final CompletableFuture<Void> mapReduce = compute.apply(input);
        mapReduces.add(mapReduce);
        return getMapFuture(mapReduces);
    }

    private Future<Map<String, Integer>> getMapFuture(Stream.Builder<CompletableFuture<Void>> mapReduces) {
        return CompletableFuture.allOf(
                mapReduces.build().toArray(
                        CompletableFuture[]::new
                )
        ).thenApply(x -> result);
    }

    public CompletableFuture<Void> getMapReduceFuture(final String input) {
        return CompletableFuture.runAsync(() -> result.compute(input, (k, v) -> (v == null) ? 1 : v + 1), backgroundJobExecutor);
    }

// Original code before refactoring
//    public Future<Map<String, Integer>> apply(final String input) {
//        final Stream.Builder<CompletableFuture<Void>> mapReduces = Stream.builder();
//        final CompletableFuture<Void> mapReduce = CompletableFuture.runAsync(() ->
//                result.compute(input, (k, v) -> (v == null) ? 1 : v + 1), backgroundJobExecutor);
//        mapReduces.add(mapReduce);
//
//        return CompletableFuture.allOf(
//                mapReduces.build().toArray(
//                        CompletableFuture[]::new
//                )
//        ).thenApply(x -> result);
//
//    }
}
