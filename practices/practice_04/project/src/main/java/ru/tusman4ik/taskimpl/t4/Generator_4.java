package ru.tusman4ik.taskimpl.t4;

import ru.tusman4ik.task.generators.PrototypeGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class Generator_4 extends PrototypeGenerator {

    public record GraphConfig(
            int n,
            int extraMin,
            int extraMax,
            int wMin,
            int wMax,
            int deltaMin,
            int deltaMax
    ) {}

    public record Edge(int u, int v, int w) {}

    public record Matrix() {}

    public record GraphSpec(
            int n,
            int[][] matrix,
            List<Edge> extras,
            int from,
            int to,
            int answer,
            int edgeCount,
            double density
    ) {}


    public static GraphSpec build(Random rng, GraphConfig cfg) {
        int n = cfg.n();
        int[][] m = new int[n][n];

        int[] code = new int[n - 2];
        for (int i = 0; i < code.length; i++) {
            code[i] = rng.nextInt(n);
        }
        int[] degree = new int[n];
        Arrays.fill(degree, 1);
        for (int c : code) {
            degree[c]++;
        }
        for (int c : code) {
            int leaf = -1;
            for (int i = 0; i < n; i++) {
                if (degree[i] == 1) {
                    leaf = i;
                    break;
                }
            }
            setEdge(m, leaf, c, weight(rng, cfg));
            degree[leaf]--;
            degree[c]--;
        }
        int[] last = new int[2];
        int k = 0;
        for (int i = 0; i < n; i++) {
            if (degree[i] == 1) {
                last[k++] = i;
            }
        }
        setEdge(m, last[0], last[1], weight(rng, cfg));

        int[][] dist = floyd(m);
        int[][] hops = new int[n][n];
        for (int s = 0; s < n; s++) {
            bfsHops(m, s, hops[s]);
        }

        // 3. Диаметральная пара (максимум рёбер вдоль вес-кратчайшего), ничья — случайно.
        int best = -1;
        List<int[]> diameterPairs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (hops[i][j] > best) {
                    best = hops[i][j];
                    diameterPairs.clear();
                    diameterPairs.add(new int[]{i, j});
                } else if (hops[i][j] == best) {
                    diameterPairs.add(new int[]{i, j});
                }
            }
        }
        int[] pair = diameterPairs.get(rng.nextInt(diameterPairs.size()));
        List<Integer> path = treePath(m, pair[0], pair[1]);

        // 4. Спец-хорда: оба конца на диаметральном пути, не смежны в дереве.
        // Гарантия «≥2 путей from→to» по построению: древесный + объездной.
        List<int[]> chords = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            for (int j = i + 2; j < path.size(); j++) {
                chords.add(new int[]{path.get(i), path.get(j)});
            }
        }
        List<Edge> extras = new ArrayList<>();
        int[] chord = chords.get(rng.nextInt(chords.size()));
        extras.add(placeExtra(m, rng, cfg, chord[0], chord[1]));

        // 5. Остальные дуги до extraMin..extraMax: случайные не-рёбра.
        int total = cfg.extraMin() + rng.nextInt(cfg.extraMax() - cfg.extraMin() + 1);
        List<int[]> candidates = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (m[i][j] == 0 && !isPlaced(extras, i, j)) {
                    candidates.add(new int[]{i, j});
                }
            }
        }
        while (extras.size() < total && !candidates.isEmpty()) {
            int[] c = candidates.remove(rng.nextInt(candidates.size()));
            extras.add(placeExtra(m, rng, cfg, c[0], c[1]));
        }

        int edges = (n - 1) + extras.size();
        int possible = n * (n - 1) / 2;
        return new GraphSpec(
                n,
                m,
                List.copyOf(extras),
                pair[0],
                pair[1],
                dist[pair[0]][pair[1]],
                edges,
                (double) edges / possible
        );
    }

    private static int weight(Random rng, GraphConfig cfg) {
        return cfg.wMin() + rng.nextInt(cfg.wMax() - cfg.wMin() + 1);
    }

    private static void setEdge(int[][] m, int u, int v, int w) {
        m[u][v] = w;
        m[v][u] = w;
    }

    private static Edge placeExtra(int[][] m, Random rng, GraphConfig cfg, int u, int v) {
        int[][] dist = floyd(m);
        int w = dist[u][v] + cfg.deltaMin() + rng.nextInt(cfg.deltaMax() - cfg.deltaMin() + 1);
        setEdge(m, u, v, w);
        return new Edge(u, v, w);
    }

    private static boolean isPlaced(List<Edge> extras, int u, int v) {
        for (Edge e : extras) {
            if ((e.u() == u && e.v() == v) || (e.u() == v && e.v() == u)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Флойд–Уоршелл, только веса. Без петель и отрицательных — всё корректно.
     */
    public static int[][] floyd(int[][] m) {
        int n = m.length;
        int[][] d = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                d[i][j] = (i == j) ? 0 : (m[i][j] == 0 ? Integer.MAX_VALUE / 2 : m[i][j]);
            }
        }
        for (int mid = 0; mid < n; mid++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (d[i][j] > d[i][mid] + d[mid][j]) {
                        d[i][j] = d[i][mid] + d[mid][j];
                    }
                }
            }
        }
        return d;
    }

    private static void bfsHops(int[][] m, int s, int[] hops) {
        Arrays.fill(hops, -1);
        hops[s] = 0;
        int[] queue = new int[m.length];
        int head = 0, tail = 0;
        queue[tail++] = s;
        while (head < tail) {
            int u = queue[head++];
            for (int v = 0; v < m.length; v++) {
                if (m[u][v] != 0 && hops[v] == -1) {
                    hops[v] = hops[u] + 1;
                    queue[tail++] = v;
                }
            }
        }
    }

    private static List<Integer> treePath(int[][] m, int from, int to) {
        int n = m.length;
        int[] prev = new int[n];
        Arrays.fill(prev, -1);
        int[] queue = new int[n];
        int head = 0, tail = 0;
        queue[tail++] = from;
        prev[from] = from;
        while (head < tail) {
            int u = queue[head++];
            if (u == to) {
                break;
            }
            for (int v = 0; v < n; v++) {
                if (m[u][v] != 0 && prev[v] == -1) {
                    prev[v] = u;
                    queue[tail++] = v;
                }
            }
        }
        List<Integer> path = new ArrayList<>();
        for (int cur = to; ; cur = prev[cur]) {
            path.addFirst(cur);
            if (cur == from) {
                break;
            }
        }
        return path;
    }
}
