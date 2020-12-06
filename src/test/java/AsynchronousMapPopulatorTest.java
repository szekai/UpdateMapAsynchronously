import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class AsynchronousMapPopulatorTest {
    @Test
    public void populateMap() throws ExecutionException, InterruptedException {
        Executor someExecutor = ForkJoinPool.commonPool();
        Map<String,Integer> wordClassObservations = Map.of("A", 1,"B", 2);
        Future<Map<String,Integer>> futureClassModels = new AsynchronousMapPopulator(someExecutor).apply(wordClassObservations);
// Do lots of other stuff
        Map<String,Integer> completedModels = futureClassModels.get();
        assertEquals("{A=2, B=3}", completedModels.toString());
    }

    @Test
    public void runParallel(){
        String[] s = {"a","b","c","d","e","f","g"};
        Arrays.stream(s).parallel()
                .forEach(System.out::println);
    }
}
