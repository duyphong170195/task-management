package com.eight_seneca.task_management.flinters;

import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AggregatorApp {

    // ======== CLI ========
    public static void main(String[] args) throws Exception {
        Args a = Args.parse(args);

        long t0 = System.nanoTime();

        Files.createDirectories(a.outputDir);

        final int shards = Math.max(1, a.threads);
        final int batchSize = a.batchSize;

        @SuppressWarnings("unchecked")
        BlockingQueue<RowBatch>[] queues = new BlockingQueue[shards];
        for (int i = 0; i < shards; i++) {
            queues[i] = new ArrayBlockingQueue<>(a.queueCapacity);
        }

        @SuppressWarnings("unchecked")
        Map<String, Agg>[] shardMaps = new Map[shards];
        for (int i = 0; i < shards; i++) {
            shardMaps[i] = new HashMap<>(1 << 16);
        }

        ExecutorService pool = Executors.newFixedThreadPool(shards);
        for (int i = 0; i < shards; i++) {
            final int shard = i;
            pool.submit(() -> workerLoop(queues[shard], shardMaps[shard]));
        }

        // Producer: parse CSV streaming và dispatch batch vào shard queues
        parseCsvStreaming(a.input, queues, shards, batchSize);

        // Send poison pills to stop workers
        for (int i = 0; i < shards; i++) {
            queues[i].put(RowBatch.POISON);
        }

        pool.shutdown();
        if (!pool.awaitTermination(60, TimeUnit.MINUTES)) {
            pool.shutdownNow();
            throw new RuntimeException("Workers timeout");
        }

        // Build top lists from shard maps (no need merge to 1 big map)
        List<Result> topCtr = top10ByCtr(shardMaps);
        List<Result> topCpa = top10ByLowestCpa(shardMaps);

        writeCsv(a.outputDir.resolve("top10_ctr.csv"), topCtr);
        writeCsv(a.outputDir.resolve("top10_cpa.csv"), topCpa);

        long t1 = System.nanoTime();
        double sec = (t1 - t0) / 1_000_000_000.0;

        long used = usedMemoryBytes();
        System.out.printf(
                Locale.US,
                "Done. Time: %.2fs, approx used memory: %.2f MB%n",
                sec, used / 1024.0 / 1024.0
        );
    }

    // ======== Models ========
    static final class Agg {
        long impressions, clicks, conversions;
        double spend;

        void add(long imp, long clk, double sp, long conv) {
            impressions += imp;
            clicks += clk;
            spend += sp;
            conversions += conv;
        }
    }

    static final class Result {
        final String campaignId;
        final long totalImpressions, totalClicks, totalConversions;
        final double totalSpend;
        final double ctr;      // clicks / impressions
        final Double cpa;      // spend / conversions (null if conversions == 0)

        Result(String id, Agg a) {
            this.campaignId = id;
            this.totalImpressions = a.impressions;
            this.totalClicks = a.clicks;
            this.totalSpend = a.spend;
            this.totalConversions = a.conversions;
            this.ctr = (totalImpressions == 0) ? 0.0 : ((double) totalClicks) / totalImpressions;
            this.cpa = (totalConversions == 0) ? null : (totalSpend / totalConversions);
        }
    }

    // ======== Batch container ========
    static final class RowBatch {
        static final RowBatch POISON = new RowBatch(0, true);

        final String[] campaignId;
        final long[] impressions, clicks, conversions;
        final double[] spend;
        int size = 0;
        final boolean poison;

        RowBatch(int capacity) { this(capacity, false); }

        private RowBatch(int capacity, boolean poison) {
            this.poison = poison;
            this.campaignId = poison ? null : new String[capacity];
            this.impressions = poison ? null : new long[capacity];
            this.clicks = poison ? null : new long[capacity];
            this.conversions = poison ? null : new long[capacity];
            this.spend = poison ? null : new double[capacity];
        }

        void add(String id, long imp, long clk, double sp, long conv) {
            int i = size++;
            campaignId[i] = id;
            impressions[i] = imp;
            clicks[i] = clk;
            spend[i] = sp;
            conversions[i] = conv;
        }

        boolean isFull() { return size == campaignId.length; }
        boolean isEmpty() { return size == 0; }
    }

    // ======== Custom RowProcessor + batch queue ========
    static final class BatchDispatchRowProcessor implements RowProcessor {
        private final BlockingQueue<RowBatch>[] queues;
        private final int shards;
        private final int batchSize;

        // schema fixed: campaign_id,date,impressions,clicks,spend,conversions
        private static final int IDX_CAMPAIGN = 0;
        private static final int IDX_IMP = 2;
        private static final int IDX_CLICKS = 3;
        private static final int IDX_SPEND = 4;
        private static final int IDX_CONV = 5;

        private final RowBatch[] currentBatchPerShard;

        // optional: counting for debug
        private final AtomicInteger rowsSeen = new AtomicInteger();

        BatchDispatchRowProcessor(BlockingQueue<RowBatch>[] queues, int shards, int batchSize) {
            this.queues = queues;
            this.shards = shards;
            this.batchSize = batchSize;
            this.currentBatchPerShard = new RowBatch[shards];
            for (int i = 0; i < shards; i++) currentBatchPerShard[i] = new RowBatch(batchSize);
        }

        @Override
        public void processStarted(ParsingContext context) {
            // no-op
        }

        @Override
        public void rowProcessed(String[] row, ParsingContext context) {
            // row length expected 6
            String id = row[IDX_CAMPAIGN];
            long imp = parseLongFast(row[IDX_IMP]);
            long clk = parseLongFast(row[IDX_CLICKS]);
            double sp = parseDoubleFast(row[IDX_SPEND]);
            long conv = parseLongFast(row[IDX_CONV]);

            int shard = (id.hashCode() & 0x7fffffff) % shards;
            RowBatch b = currentBatchPerShard[shard];
            b.add(id, imp, clk, sp, conv);

            if (b.isFull()) flushShard(shard);

            rowsSeen.incrementAndGet();
        }

        @Override
        public void processEnded(ParsingContext context) {
            // flush remaining partial batches
            for (int shard = 0; shard < shards; shard++) {
                if (!currentBatchPerShard[shard].isEmpty()) flushShard(shard);
            }
            // You can print count if you want:
            // System.out.println("Rows processed: " + rowsSeen.get());
        }

        private void flushShard(int shard) {
            RowBatch full = currentBatchPerShard[shard];
            try {
                queues[shard].put(full);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            currentBatchPerShard[shard] = new RowBatch(batchSize);
        }
    }

    // ======== Producer: univocity streaming parse ========
    static void parseCsvStreaming(Path input,
                                  BlockingQueue<RowBatch>[] queues,
                                  int shards,
                                  int batchSize) throws IOException {

        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setSkipEmptyLines(true);
        settings.getFormat().setDelimiter(',');

        // Processor: custom RowProcessor + batch queue
        settings.setProcessor(new BatchDispatchRowProcessor(queues, shards, batchSize));

        try (InputStream in = new BufferedInputStream(Files.newInputStream(input), 16 * 1024 * 1024);
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            new CsvParser(settings).parse(reader); // streaming
        }
    }

    // ======== Worker: consume batch, update local HashMap (no locks) ========
    static void workerLoop(BlockingQueue<RowBatch> q, Map<String, Agg> map) {
        try {
            while (true) {
                RowBatch b = q.take();
                if (b.poison) break;

                for (int i = 0; i < b.size; i++) {
                    String id = b.campaignId[i];
                    Agg agg = map.get(id);
                    if (agg == null) {
                        agg = new Agg();
                        map.put(id, agg);
                    }
                    agg.add(b.impressions[i], b.clicks[i], b.spend[i], b.conversions[i]);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ======== Top-10 ========
    static List<Result> top10ByCtr(Map<String, Agg>[] shardMaps) {
        PriorityQueue<Result> pq = new PriorityQueue<>(Comparator.comparingDouble(r -> r.ctr)); // min-heap size 10

        for (Map<String, Agg> m : shardMaps) {
            for (var e : m.entrySet()) {
                Result r = new Result(e.getKey(), e.getValue());
                if (pq.size() < 10) pq.offer(r);
                else if (r.ctr > pq.peek().ctr) { pq.poll(); pq.offer(r); }
            }
        }

        ArrayList<Result> out = new ArrayList<>(pq);
        out.sort((a, b) -> Double.compare(b.ctr, a.ctr)); // desc CTR
        return out;
    }

    static List<Result> top10ByLowestCpa(Map<String, Agg>[] shardMaps) {
        // max-heap by CPA => keep 10 smallest
        PriorityQueue<Result> pq = new PriorityQueue<>((a, b) -> Double.compare(b.cpa, a.cpa));

        for (Map<String, Agg> m : shardMaps) {
            for (var e : m.entrySet()) {
                Result r = new Result(e.getKey(), e.getValue());
                if (r.cpa == null) continue; // exclude conversions=0
                if (pq.size() < 10) pq.offer(r);
                else if (r.cpa < pq.peek().cpa) { pq.poll(); pq.offer(r); }
            }
        }

        ArrayList<Result> out = new ArrayList<>(pq);
        out.sort(Comparator.comparingDouble(a -> a.cpa)); // asc CPA
        return out;
    }

    // ======== CSV Writer ========
    static void writeCsv(Path file, List<Result> rows) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            w.write("campaign_id,total_impressions,total_clicks,total_spend,total_conversions,CTR,CPA");
            w.newLine();

            for (Result r : rows) {
                String cpaStr = (r.cpa == null) ? "" : String.format(Locale.US, "%.2f", r.cpa);

                w.write(r.campaignId); w.write(',');
                w.write(Long.toString(r.totalImpressions)); w.write(',');
                w.write(Long.toString(r.totalClicks)); w.write(',');
                w.write(String.format(Locale.US, "%.2f", r.totalSpend)); w.write(',');
                w.write(Long.toString(r.totalConversions)); w.write(',');
                w.write(String.format(Locale.US, "%.4f", r.ctr)); w.write(',');
                w.write(cpaStr);
                w.newLine();
            }
        }
    }

    // ======== Fast parsers (dataset clean) ========
    static long parseLongFast(String s) {
        long v = 0;
        for (int i = 0; i < s.length(); i++) v = v * 10 + (s.charAt(i) - '0');
        return v;
    }

    static double parseDoubleFast(String s) {
        long intPart = 0, fracPart = 0, div = 1;
        boolean dot = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') { dot = true; continue; }
            int d = c - '0';
            if (!dot) intPart = intPart * 10 + d;
            else { fracPart = fracPart * 10 + d; div *= 10; }
        }
        return intPart + (fracPart / (double) div);
    }

    static long usedMemoryBytes() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    // ======== CLI args ========
    static final class Args {
        final Path input;
        final Path outputDir;
        final int threads;
        final int batchSize;
        final int queueCapacity;

        Args(Path input, Path outputDir, int threads, int batchSize, int queueCapacity) {
            this.input = input;
            this.outputDir = outputDir;
            this.threads = threads;
            this.batchSize = batchSize;
            this.queueCapacity = queueCapacity;
        }

        static Args parse(String[] args) {
            Map<String, String> m = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                String k = args[i];
                if (k.startsWith("--")) {
                    if (i + 1 >= args.length) usageAndExit("Missing value for " + k);
                    m.put(k, args[++i]);
                } else {
                    usageAndExit("Unknown argument: " + k);
                }
            }

            Path input = getPath(m, "--input", true);
            Path output = getPath(m, "--output", true);
            int threads = getInt(m, "--threads", Runtime.getRuntime().availableProcessors());
            int batchSize = getInt(m, "--batchSize", 4096);
            int queueCap = getInt(m, "--queueCapacity", 50);

            if (threads <= 0) usageAndExit("--threads must be > 0");
            if (batchSize <= 0) usageAndExit("--batchSize must be > 0");
            if (queueCap <= 0) usageAndExit("--queueCapacity must be > 0");

            return new Args(input, output, threads, batchSize, queueCap);
        }

        static Path getPath(Map<String, String> m, String key, boolean required) {
            String v = m.get(key);
            if (v == null) {
                if (required) usageAndExit("Missing " + key);
                return null;
            }
            return Paths.get(v);
        }

        static int getInt(Map<String, String> m, String key, int def) {
            String v = m.get(key);
            if (v == null) return def;
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                usageAndExit("Invalid int for " + key + ": " + v);
                return def;
            }
        }

        static void usageAndExit(String msg) {
            System.err.println(msg);
            System.err.println("Usage:");
            System.err.println("  java -jar aggregator.jar --input ad_data.csv --output results/ " +
                    "[--threads 8] [--batchSize 4096] [--queueCapacity 50]");
            System.exit(2);
        }
    }
}
