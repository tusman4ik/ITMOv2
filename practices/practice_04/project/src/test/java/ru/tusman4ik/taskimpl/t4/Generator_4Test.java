package ru.tusman4ik.taskimpl.t4;

import org.junit.jupiter.api.Test;
import ru.tusman4ik.taskimpl.t4.Generator_4.Edge;
import ru.tusman4ik.taskimpl.t4.Generator_4.GraphConfig;
import ru.tusman4ik.taskimpl.t4.Generator_4.GraphSpec;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Generator_4Test {

    private static final GraphConfig CFG_5 = new GraphConfig(5, 2, 3, 1, 5, 2, 5);
    private static final GraphConfig CFG_6 = new GraphConfig(6, 3, 5, 1, 5, 2, 5);

    @Test
    void invariantsHold() {
        for (long seed = 0; seed < 2000; seed++) {
            assertInvariants(Generator_4.build(new Random(seed), CFG_5), CFG_5);
        }
    }

    @Test
    void deterministicPerSeed() {
        GraphSpec a = Generator_4.build(new Random(12345L), CFG_5);
        GraphSpec b = Generator_4.build(new Random(12345L), CFG_5);
        assertTrue(Arrays.deepEquals(a.matrix(), b.matrix()));
        assertEquals(a.answer(), b.answer());
        assertEquals(a.from(), b.from());
        assertEquals(a.to(), b.to());
    }

    @Test
    void hopDistributionCoversTwoThreeFour() {
        int[] hops = new int[5];
        for (long seed = 0; seed < 100_000; seed++) {
            GraphSpec spec = Generator_4.build(new Random(seed), CFG_5);
            hops[weightShortestHops(spec.matrix(), spec.from(), spec.to())]++;
        }
        System.out.printf("hops: 2=%d 3=%d 4=%d%n", hops[2], hops[3], hops[4]);
        assertTrue(hops[2] > 0 && hops[3] > 0 && hops[4] > 0);
    }

    @Test
    void sixVerticesHold() {
        for (long seed = 0; seed < 200; seed++) {
            GraphSpec spec = Generator_4.build(new Random(seed), CFG_6);
            assertInvariants(spec, CFG_6);
        }
    }

    @Test
    void stressFiveMillionGenerations() {
        for (long seed = 0; seed < 5_000_000L; seed++) {
            GraphSpec spec = Generator_4.build(new Random(seed), CFG_5);
            assertCore(spec);
        }
    }

    private static void assertInvariants(GraphSpec spec, GraphConfig cfg) {
        int n = cfg.n();
        int[][] m = spec.matrix();
        assertCore(spec);

        // Дерево отдельно: матрица минус экстры — связно, ровно n-1 ребро, веса в диапазоне.
        int[][] tree = copy(m);
        for (Edge e : spec.extras()) {
            // Каждая дуга = оптимум-на-момент + delta; оптимум никогда не бьётся,
            // поэтому древесный оптимум между концами годится для проверки.
            int treeOpt = Generator_4.floyd(withoutExtras(m, spec.extras()))[e.u()][e.v()];
            assertTrue(e.w() >= treeOpt + cfg.deltaMin() && e.w() <= treeOpt + cfg.deltaMax(),
                    "extra not heavier by delta: " + e);
            tree[e.u()][e.v()] = 0;
            tree[e.v()][e.u()] = 0;
        }
        int treeEdges = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (tree[i][j] != 0) {
                    treeEdges++;
                    assertTrue(tree[i][j] >= cfg.wMin() && tree[i][j] <= cfg.wMax());
                }
            }
        }
        assertEquals(n - 1, treeEdges);
        assertTrue(connected(tree));

        // Ответ = вес кратчайшего from→to, рёбер в нём 2..4.
        int[][] dist = Generator_4.floyd(m);
        assertEquals(dist[spec.from()][spec.to()], spec.answer());
        int h = weightShortestHops(m, spec.from(), spec.to());
        assertTrue(h >= 2 && h <= cfg.n() - 1, "hops=" + h);

        // Между from и to не менее двух различных простых путей.
        assertTrue(countSimplePaths(m, spec.from(), spec.to(), 3) >= 2);

        // Плотность в [30%, 70%] — метрика, не отбор; здесь конструктивно всегда внутри.
        assertTrue(spec.density() >= 0.3 && spec.density() <= 0.7, "density=" + spec.density());
    }

    private static void assertCore(GraphSpec spec) {
        int n = spec.matrix().length;
        int edges = 0;
        for (int i = 0; i < n; i++) {
            assertEquals(0, spec.matrix()[i][i], "loop at " + i);
            for (int j = i + 1; j < n; j++) {
                assertEquals(spec.matrix()[i][j], spec.matrix()[j][i], "not symmetric");
                if (spec.matrix()[i][j] != 0) {
                    edges++;
                }
            }
        }
        assertEquals(edges, spec.edgeCount());
    }

    private static int[][] copy(int[][] m) {
        int[][] c = new int[m.length][];
        for (int i = 0; i < m.length; i++) {
            c[i] = m[i].clone();
        }
        return c;
    }

    private static int[][] withoutExtras(int[][] m, java.util.List<Edge> extras) {
        int[][] c = copy(m);
        for (Edge e : extras) {
            c[e.u()][e.v()] = 0;
            c[e.v()][e.u()] = 0;
        }
        return c;
    }

    private static boolean connected(int[][] m) {
        boolean[] seen = new boolean[m.length];
        int[] q = new int[m.length];
        int h = 0, t = 0;
        q[t++] = 0;
        seen[0] = true;
        while (h < t) {
            int u = q[h++];
            for (int v = 0; v < m.length; v++) {
                if (m[u][v] != 0 && !seen[v]) {
                    seen[v] = true;
                    q[t++] = v;
                }
            }
        }
        for (boolean b : seen) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    private static int weightShortestHops(int[][] m, int from, int to) {
        int n = m.length;
        int[] dist = new int[n];
        int[] hops = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE / 2);
        Arrays.fill(hops, Integer.MAX_VALUE / 2);
        dist[from] = 0;
        hops[from] = 0;
        boolean[] done = new boolean[n];
        for (int iter = 0; iter < n; iter++) {
            int u = -1;
            for (int i = 0; i < n; i++) {
                if (!done[i] && (u == -1 || dist[i] < dist[u]
                        || (dist[i] == dist[u] && hops[i] < hops[u]))) {
                    u = i;
                }
            }
            done[u] = true;
            for (int v = 0; v < n; v++) {
                if (m[u][v] != 0 && dist[v] > dist[u] + m[u][v]) {
                    dist[v] = dist[u] + m[u][v];
                    hops[v] = hops[u] + 1;
                }
            }
        }
        return hops[to];
    }

    private static int countSimplePaths(int[][] m, int from, int to, int cap) {
        boolean[] seen = new boolean[m.length];
        int[] count = new int[1];
        dfs(m, from, to, seen, count, cap);
        return count[0];
    }

    private static void dfs(int[][] m, int u, int to, boolean[] seen, int[] count, int cap) {
        if (count[0] >= cap) {
            return;
        }
        if (u == to) {
            count[0]++;
            return;
        }
        seen[u] = true;
        for (int v = 0; v < m.length; v++) {
            if (m[u][v] != 0 && !seen[v]) {
                dfs(m, v, to, seen, count, cap);
            }
        }
        seen[u] = false;
    }

}
