import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TokenTestResultChecker
{
    public static void main(String[] args)
    {
        try {
            List<String> result = Files.readAllLines(Path.of("C:\\Users\\Silop\\Desktop\\HastiSearch\\assignment1\\tokenized_result.txt"));
            List<String> actual = Files.readAllLines(Path.of("C:\\Users\\Silop\\Desktop\\HastiSearch\\assignment1\\token_test_tokenized_ok.txt"));

            if (result.equals(actual)) {
                System.out.println("Result with patterns matches the desired result");
                System.exit(0);
            }

            Set<String> resSet = new HashSet<>(result);
            Set<String> actSet = new HashSet<>(actual);
            Set<String> actSet2 = new HashSet<>(actual);

            actSet.removeAll(resSet);
            resSet.removeAll(actSet2);

            if (!actSet.isEmpty()) {
                System.out.println("------- Words missing that are needed ------");
                for (String word : actSet) {
                    System.out.println(word);
                }
            }
            if (!resSet.isEmpty()) {
                System.out.println("------- Words present that are wrong ------");
                for (String word : resSet) {
                    System.out.println(word);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
