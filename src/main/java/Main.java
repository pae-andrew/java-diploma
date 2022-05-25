import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Link;
import com.itextpdf.layout.element.Paragraph;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {

        // создаём конфиг
        LinksSuggester linksSuggester = new LinksSuggester(
                new File("data/config"));

        List<Suggest> suggests = linksSuggester.getSuggests();

        // перебираем пдфки в data/pdfs
        var dir = new File("data/pdfs");
        ArrayList<File> filesIn = new ArrayList<>(Arrays.asList(
                Objects.requireNonNull(
                        dir.listFiles())));

        // для каждой пдфки создаём новую в data/converted
        for (var file : filesIn) {
            var doc = new PdfDocument(new PdfReader(file),
                    new PdfWriter(
                            new File("data/converted/"
                                    + file.getName())));

            // перебираем страницы pdf
            ArrayList<String> pages = new ArrayList<>();
            for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                pages.add(PdfTextExtractor.getTextFromPage(
                        doc.getPage(i)));
            }

            int i = 1;
            List<String> keyWords = new ArrayList<>();
            for (String page : pages) {

                List<String> urls = new ArrayList<>();
                List<String> titles = new ArrayList<>();
                for (Suggest suggest :suggests) {
                    if (page.toLowerCase().contains(
                            suggest.getKeyWord().toLowerCase()) &
                    !keyWords.contains(suggest.getKeyWord())) {
                        urls.add(suggest.getUrl());
                        titles.add(suggest.getTitle());
                        keyWords.add(suggest.getKeyWord());
                    }
                }

                // если в странице есть неиспользованные ключевые слова, создаём новую страницу за ней
                if (urls.size() > 0) {
                    var newPage = doc.addNewPage(
                            pages.indexOf(page) + i + 1);
                    i++;
                    var rect = new Rectangle(
                            newPage.getPageSize()).moveRight(10).moveDown(10);
                    Canvas canvas = new Canvas(newPage, rect);
                    Paragraph paragraph = new Paragraph("Suggestions:\n")
                            .setFontColor(new DeviceRgb(10, 36, 190))
                            .setBackgroundColor(new DeviceRgb(210, 240,10));
                    paragraph.setFontSize(25);

                    // вставляем туда рекомендуемые ссылки из конфига
                    for (int j = 0; j < urls.size(); j++) {
                        PdfLinkAnnotation annotation = new PdfLinkAnnotation(rect);
                        PdfAction action = PdfAction.createURI(urls.get(j));
                        annotation.setAction(action);
                        Link link = new Link(titles.get(j), annotation);
                        paragraph.add(link.setUnderline());
                        paragraph.add("\n");
                    }

                    canvas.add(paragraph);
                    canvas.close();
                }
            }
            doc.close();
        }

        System.out.println("All files are converted!");
    }
}
