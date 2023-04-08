import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Vote implements Serializable {

    private String owner;
    private String name;
    private String description;
    private int answerCount;
    private Map<String, Integer> answers = new ConcurrentHashMap<>();

    public Vote(String voteName, String voteDescription, int answerCount, Map<String, Integer> answers, String owner) {
        this.name = voteName;
        this.description = voteDescription;
        this.answerCount = answerCount;
        this.answers = answers;
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Integer> getAnswers() {
        return answers;
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public static Map<String, Integer> setAnswerPool(String[] answers, int answersCount) {
        Map<String, Integer> pool = new ConcurrentHashMap<>();
        for (int i = 0; i < answersCount; i++) {
            if (pool.containsKey(answers[i])) {
                i--;
                System.out.println("Duplicated answer, please try again");
            }
            if (answers[i] != "") {
                pool.put(answers[i], 0);
            }
        }
        return pool;
    }

}
