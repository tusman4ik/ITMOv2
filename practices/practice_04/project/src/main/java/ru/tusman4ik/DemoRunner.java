package ru.tusman4ik;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import ru.tusman4ik.task.registry.GeneratorRegistry;
import ru.tusman4ik.task.registry.PrototypeId;
import ru.tusman4ik.task.registry.PrototypeInfo;
import ru.tusman4ik.task.registry.enums.Exam;
import ru.tusman4ik.task.registry.enums.Subj;
import ru.tusman4ik.task.templates.CheckResult;
import ru.tusman4ik.taskimpl.t4.Generator_4;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Демо: печатает генерации прототипа в консоль, кладёт HTML в /tmp и гасит контекст.
 * Запуск: ./gradlew bootRun --args='--demo.enabled=true --demo.number=4 --demo.prototype=0 --demo.matrix=true'
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "demo", name = "enabled", havingValue = "true")
public class DemoRunner implements ApplicationRunner {

    private static final Pattern MATRIX_ROW = Pattern.compile("^[0-9 ]+$");
    private static final Pattern PAIR = Pattern.compile("между вершинами (\\d+) и (\\d+)");

    private final GeneratorRegistry registry;
    private final ConfigurableApplicationContext ctx;
    private final int number;
    private final int prototype;
    private final boolean matrix;

    public DemoRunner(GeneratorRegistry registry,
                      ConfigurableApplicationContext ctx,
                      @Value("${demo.number:3}") int number,
                      @Value("${demo.prototype:0}") int prototype,
                      @Value("${demo.matrix:false}") boolean matrix) {
        this.registry = registry;
        this.ctx = ctx;
        this.number = number;
        this.prototype = prototype;
        this.matrix = matrix;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        PrototypeInfo info = registry.info(new PrototypeId(Exam.OGE, Subj.CS, number, prototype));
        String title = "Генератор " + number + "/" + prototype + " — формулировки (сиды 1000–1019)";
        StringBuilder html = new StringBuilder();
        html.append("""
                <!DOCTYPE html>
                <html lang="ru">
                <head><meta charset="UTF-8"><title>""").append(title).append("""
                </title></head>
                <body>
                <h1>""").append(title).append("""
                </h1>
                <ol>
                """);
        for (long seed = 1000; seed < 1020; seed++) {
            String line;
            try {
                line = info.produce(seed).statement();
            } catch (RuntimeException e) {
                line = "FAILED: " + e;
            }
            System.out.println("[" + seed + "] " + line.replace("\n", " / "));
            html.append("<li>").append(renderDemo(line, info, seed)).append("</li>\n");
        }
        html.append("""
                </ol>
                </body>
                </html>
                """);
        java.nio.file.Path out = java.nio.file.Path.of("/tmp/demo-" + number + "-" + prototype + ".html");
        java.nio.file.Files.writeString(out, html.toString());
        System.out.println("HTML written to " + out);
        System.exit(SpringApplication.exit(ctx));
    }

    private String renderDemo(String line, PrototypeInfo info, long seed) {
        if (!matrix) {
            return line;
        }
        return line + "<br/>" + matrixTable(line, info, seed);
    }

    /** Только для демо: числовые строки statement → HTML-таблица + ответ Флойдом + сверка check(). */
    private static String matrixTable(String line, PrototypeInfo info, long seed) {
        List<int[]> rows = new ArrayList<>();
        for (String raw : line.split("\n")) {
            String row = raw.trim();
            if (MATRIX_ROW.matcher(row).matches()) {
                String[] cells = row.split(" +");
                int[] nums = new int[cells.length];
                for (int i = 0; i < cells.length; i++) {
                    nums[i] = Integer.parseInt(cells[i]);
                }
                rows.add(nums);
            }
        }
        if (rows.isEmpty()) {
            return "<i>матрица не найдена</i>";
        }
        int n = rows.size();
        int[][] m = rows.toArray(new int[0][]);
        Matcher pair = PAIR.matcher(line);
        if (!pair.find()) {
            return "<i>пара вершин не найдена</i>";
        }
        int from = Integer.parseInt(pair.group(1)) - 1;
        int to = Integer.parseInt(pair.group(2)) - 1;
        int answer = Generator_4.floyd(m)[from][to];

        StringBuilder sb = new StringBuilder("<table border=\"1\">");
        for (int[] row : rows) {
            sb.append("<tr>");
            for (int cell : row) {
                sb.append("<td>").append(cell).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");

        String verdict;
        try {
            CheckResult checked = info.check(seed, "{\"answer\":" + answer + "}");
            verdict = "check=" + checked.total();
        } catch (RuntimeException e) {
            verdict = "check FAILED: " + e;
        }
        sb.append("<br/>Ответ Флойда: ").append(answer).append(", ").append(verdict);
        return sb.toString();
    }
}
