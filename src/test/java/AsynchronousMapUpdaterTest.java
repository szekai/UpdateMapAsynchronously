import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class AsynchronousMapUpdaterTest {
    @Test
    public void populateMap() throws ExecutionException, InterruptedException {
        Executor someExecutor = ForkJoinPool.commonPool();
        Future<Map<String,Integer>> futureClassModels = new AsynchronousMapUpdater(someExecutor).apply("A");
// Do lots of other stuff
        Map<String,Integer> completedModels = futureClassModels.get();
        assertEquals("{A=1}", completedModels.toString());
    }

    @Test
    public void runParallel(){
        String[] s = {"a","a","b","c","d","a","e","f","a","g","c"};
        Executor someExecutor = ForkJoinPool.commonPool();
        AsynchronousMapUpdater verySlowUpdater = new AsynchronousMapUpdater(someExecutor);
        Arrays.stream(s).parallel()
                .forEach(x -> {
                    Future<Map<String,Integer>> futureClassModels = verySlowUpdater.apply(x);
                    Map<String,Integer> completedModels = null;
                    try {
                        completedModels = futureClassModels.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    System.out.println(completedModels);
                });
        assertEquals("{a=4, b=1, c=2, d=1, e=1, f=1, g=1}", verySlowUpdater.getResult().toString());
    }

    @Test
    public void runParallel2(){
        String[] s = {"a","a","b","c","d","a","e","f","a","g","c"};
        Executor someExecutor = ForkJoinPool.commonPool();
        AsynchronousMapUpdater verySlowUpdater = new AsynchronousMapUpdater(someExecutor);
        Arrays.stream(s).parallel()
                .forEach(x -> {
                    Future<Map<String,Integer>> futureClassModels = verySlowUpdater.apply(x, verySlowUpdater::getMapReduceFuture);
                    Map<String,Integer> completedModels = null;
                    try {
                        completedModels = futureClassModels.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    System.out.println(completedModels);
                });
        assertEquals("{a=4, b=1, c=2, d=1, e=1, f=1, g=1}", verySlowUpdater.getResult().toString());
    }
}
