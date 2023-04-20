import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

class Server {

    private final int port;
    private static int clientNumber = 0;
    private static ServerSocket server = null;
    private static List<Topic> topics = new CopyOnWriteArrayList<>();
    static BufferedReader selfReader = new BufferedReader(new InputStreamReader(System.in));

    private static boolean work = true;
    private static final Logger LOGGER;
    static {
        try {
            FileInputStream ins = new FileInputStream("log.properties");
            LogManager.getLogManager().readConfiguration(ins);
            LOGGER = Logger.getLogger(Server.class.getName());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static final String helpMessage = "Available commands: " + "create topic -n=<topic> :" + "view :"
            + "create vote -t=<topic> :" + "view -t=<topic> -v=<vote :" + "vote -t=<topic> -v=<vote> :"
            + "delete -t=<topic> -v=<vote> :" + "exit;";

    public Server(int port) {

        this.port = port;
        try {
            server = new ServerSocket(this.port);
            LOGGER.log(Level.INFO, "Server started at port: " + server.getLocalPort());
            while (work) {
                Socket client = server.accept();
                clientNumber++;
                LOGGER.log(Level.INFO, "New connected from " + client.getInetAddress().getHostAddress());
                ClientHandler clientSock = new ClientHandler(client, clientNumber);

                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception: ", e);
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Exception: ", e);
                }
            }
        }

    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        String clientUserName;
        private final int id;

        public ClientHandler(Socket socket, int clientNumber) {
            this.clientSocket = socket;
            this.id = clientNumber;
        }

        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line;
                while (!(line = in.readLine()).contains("login -u=")) {
                    out.println("You need login to start work, use : login -u=username");
                }
                clientUserName = line.substring(9);
                LOGGER.log(Level.INFO, "User with username: " + clientUserName + " connected");
                out.println("Now you can work!");
                while (!clientSocket.isClosed()) {
                    if (selfReader.ready()) {
                        String s = selfReader.readLine();
                        if (s.equalsIgnoreCase("exit")) {
                            LOGGER.log(Level.INFO, "Exit command entered");
                            work = false;
                            server.close();
                            break;
                        } else if (s.contains("load")) {
                            String filename = s.split(" ")[1];
                            topics = loadFromFile(filename);
                            LOGGER.log(Level.INFO, "Load from file: " + filename);
                        } else if (s.contains("save")) {
                            String filename = s.split(" ")[1];
                            if (saveInFile(filename, topics)) {
                                LOGGER.log(Level.INFO, "Save in file: " + filename);
                            }
                        } else {
                            LOGGER.log(Level.INFO, "Unknown command, try again..");
                        }
                    }
                    line = in.readLine();
                    line.toLowerCase();
                    if (line.equals("exit")) {
                        LOGGER.log(Level.INFO, "Client #" + id + " kill connection");
                        out.println("command exit call, connection close ...");
                        LOGGER.log(Level.INFO, "Client #" + id + " disconnected");
                        break;
                    } else if (line.equals("!help")) {
                        out.println(helpMessage);
                    } else if (line.contains("create topic -n=")) {
                        String[] splited = line.split("=");
                        String topicName = splited[1];
                        if (createTopic(topicName)) {
                            out.println("Topic: " + topicName + " created");
                            LOGGER.log(Level.INFO, clientUserName + " create topic: " + topicName);
                        } else {
                            out.println("Topic with this name is already created");
                        }
                    } else if (line.equals("view")) {
                        if (topics.size() == 0) {
                            out.println("There is no topic yet, please create it..");
                            continue;
                        }
                        StringBuffer buffer = new StringBuffer();
                        for (Topic topic : topics) {
                            buffer.append(topic.getName() + "(" + topic.getVoteList().size() + ")" + "\t");
                        }
                        out.println(buffer);
                    } else if (line.contains("view -t=") && !line.contains("-v=")) {
                        String[] splited = line.split("=");
                        String topicName = splited[1];
                        if (getTopicByName(topicName) == null) {
                            out.println("Incorrect topic name, try again");
                            continue;
                        }
                        if (getTopicByName(topicName).getVoteList().size() == 0) {
                            out.println("No votes in topic");
                            continue;
                        }
                        StringBuffer voteNames = new StringBuffer();
                        for (Vote vote : getTopicByName(topicName).getVoteList()) {
                            voteNames.append(vote.getName() + ":");
                        }
                        out.println(voteNames);
                    } else if (line.toLowerCase().contains("create vote -t=")) {
                        String[] splited = line.split("=");
                        String topicName = splited[1];
                        if (getTopicByName(topicName) == null) {
                            out.println("No topic found, please try again");
                            continue;
                        }
                        out.println("Enter name for vote");
                        String voteName = in.readLine();
                        if (getTopicByName(topicName).getVoteByName(voteName) != null) {
                            out.println("Vote is already created, try again");
                            continue;
                        }
                        out.println("Enter description for vote");
                        String voteDescription = in.readLine();
                        out.println("Enter answers count for vote");
                        int answerCount = Integer.valueOf(in.readLine());
                        out.println("Enter answers in one line,use \";\" to separate answers ");
                        String temp = in.readLine();
                        String[] answers = temp.split(";");
                        if (answers.length != answerCount) {
                            out.println(
                                    "you entered not the number of answers that indicated, try create vote again..");
                            continue;
                        }
                        Map<String, Integer> answersMap = Vote.setAnswerPool(answers, answerCount);
                        out.println("Answers accepted, please print some message to continue");
                        if (createVoteInTopic(topicName, voteName, voteDescription, answerCount, answersMap,
                                clientUserName)) {
                            out.println("Vote created");
                            LOGGER.log(Level.INFO, "User: " + clientUserName + " create vote: " + voteName
                                    + " in topic: " + topicName);
                        }
                    } else if (line.contains("view -t=") && line.contains(" -v=")) {
                        String[] splited = line.split(" ");
                        String topicName = splited[1].substring(3);
                        String voteName = splited[2].substring(3);
                        if (getTopicByName(topicName) == null) {
                            out.println("Incorrect topic name, try again");
                            continue;
                        }
                        if (getTopicByName(topicName).getVoteByName(voteName) == null) {
                            out.println("Incorrect vote name, try again");
                            continue;
                        }
                        viewVote(topicName, voteName, out);
                    } else if (line.contains("vote -t=")) {
                        String[] splited = line.split(" ");
                        String topicName = splited[1].substring(3);
                        String voteName = splited[2].substring(3);
                        if (getTopicByName(topicName) == null) {
                            out.println("Incorrect topic name, try again");
                            continue;
                        }
                        if (getTopicByName(topicName).getVoteByName(voteName) == null) {
                            out.println("Incorrect vote name, try again");
                            continue;
                        }
                        showAnswers(topicName, voteName, out);
                        String answer = in.readLine();
                        if (choseAnswer(topicName, voteName, answer)) {
                            out.println("Accepted your chose");
                            LOGGER.log(Level.INFO, "User: " + clientUserName + " select answer " + answer + " in vote: "
                                    + voteName + " in topic: " + topicName);
                        } else {
                            out.println("Incorrect answer, please try again");
                        }
                    } else if (line.contains("delete -t=")) {
                        String[] splited = line.split(" ");
                        String topicName = splited[1].substring(3);
                        String voteName = splited[2].substring(3);
                        if (getTopicByName(topicName) == null) {
                            out.println("Incorrect topic name, try again");
                            continue;
                        }
                        if (getTopicByName(topicName).getVoteByName(voteName) == null) {
                            out.println("Incorrect vote name, try again");
                            continue;
                        }
                        if (getTopicByName(topicName).getVoteByName(voteName).getOwner().equals(clientUserName)) {
                            if (deleteVote(topicName, voteName)) {
                                out.println("Vote deleted");
                                LOGGER.log(Level.INFO, "User: " + clientUserName + " delete vote:" + voteName
                                        + " from topic:" + topicName);
                                continue;
                            } else {
                                out.println("Somethink went wrong, try again");
                            }
                            continue;
                        }
                        out.println("You can`t delete this vote, becouse you are not a owner");
                    } else {
                        out.println("Unknown command, try again..");
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Exception: " + e);
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Exception: " + e);
                }
            }
        }
    }

    public static boolean saveInFile(String filePath, List<Topic> topics) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(topics);
            return true;
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Exception: ", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    public static List<Topic> loadFromFile(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (List<Topic>) ois.readObject();
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Exception: ", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Exception: ", e);
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Exception: ", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean createTopic(String topicName) {
        if (getTopicByName(topicName) == null) {
            Topic topic = new Topic(topicName);
            topics.add(topic);
            return true;
        }
        return false;
    }

    public static Topic getTopicByName(String topicName) {
        for (Topic topic : topics) {
            if (topicName.equalsIgnoreCase(topic.getName())) {
                return topic;
            }
        }
        return null;
    }

    public static boolean createVoteInTopic(String topicName, String voteName, String voteDescription,
                                            int voteAnswerCout, Map<String, Integer> answers, String voteOwner) {

        if ((getTopicByName(topicName) != null) && (getTopicByName(topicName).getVoteByName(voteName) == null)) {
            Vote createdVote = new Vote(voteName, voteDescription, voteAnswerCout, answers, voteOwner);
            getTopicByName(topicName).getVoteList().add(createdVote);
            return true;
        }
        return false;
    }

    public static boolean deleteVote(String topicName, String voteName) {
        Topic top = getTopicByName(topicName);
        Vote vote = getTopicByName(topicName).getVoteByName(voteName);
        return top.getVoteList().remove(vote);
    }

    public static void showAnswers(String topicName, String voteName, PrintWriter out) {
        if (getTopicByName(topicName) != null && getTopicByName(topicName).getVoteByName(voteName) != null) {
            Vote temp = getTopicByName(topicName).getVoteByName(voteName);
            StringBuffer buffer = new StringBuffer();
            Set<?> set = temp.getAnswers().entrySet();
            Iterator<?> i = set.iterator();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                buffer.append(me.getKey() + ";");
            }
            buffer.append(" Enter your chose fully:");
            out.println(buffer);
        }
    }

    public static boolean choseAnswer(String topicName, String voteName, String answer) {
        Vote temp = getTopicByName(topicName).getVoteByName(voteName);
        if (!temp.getAnswers().containsKey(answer)) {
            return false;
        }
        temp.getAnswers().put(answer, (Integer) (temp.getAnswers().get(answer)) + 1);
        return true;

    }

    public static void viewVote(String topicName, String voteName, PrintWriter out) {
        if (getTopicByName(topicName) != null && getTopicByName(topicName).getVoteByName(voteName) != null) {
            Vote temp = getTopicByName(topicName).getVoteByName(voteName);
            StringBuffer buffer = new StringBuffer();
            buffer.append("Description: " + temp.getDescription() + " |" + " answers:");
            Set<?> set = temp.getAnswers().entrySet();
            Iterator<?> i = set.iterator();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                buffer.append(me.getKey() + "-" + me.getValue() + ";");
            }
            out.println(buffer);
        } else {
            out.println("Incorrect data");
        }
    }

}
