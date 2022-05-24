import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LinksSuggester {

    private List<Suggest> suggests;

    public List<Suggest> getSuggests() {
        return suggests;
    }

    public LinksSuggester(File file) throws IOException, WrongLinksFormatException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String currentLine = reader.readLine();
        StringBuilder text = new StringBuilder();
        while (currentLine != null) {

            text.append(currentLine).append("\n");

            currentLine = reader.readLine();
        }
       suggests =  suggest(text.toString());
    }

    public List<Suggest> suggest(String text) {

        String[] lines = text.split("\n");
        List<Suggest> suggests = new ArrayList<>();

        for (String line : lines) {
            String[] objs = line.split("\t");
            if (objs.length != 3) {
                throw new WrongLinksFormatException("Неверное количество аргументов в строке: " + line);
            }
            suggests.add(new Suggest(objs[0], objs[1], objs[2]));
        }

        return suggests;
    }
}
