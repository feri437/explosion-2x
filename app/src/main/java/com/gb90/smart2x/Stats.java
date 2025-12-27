package com.gb90.smart2x;

import java.util.List;

public class Stats {
    // Heuristic "entry 2" score: compares current <2 streak to historical distribution.
    public static class Result {
        public final int streakLt2;
        public final double p2Base;
        public final boolean entry2;
        public final double score;
        public Result(int streakLt2, double p2Base, boolean entry2, double score){
            this.streakLt2 = streakLt2;
            this.p2Base = p2Base;
            this.entry2 = entry2;
            this.score = score;
        }
    }

    public static Result evaluate(List<Double> newestFirst){
        if (newestFirst == null || newestFirst.isEmpty()){
            return new Result(0, 0.0, false, 0.0);
        }
        int n = newestFirst.size();
        int ge2 = 0;
        for(double v: newestFirst) if (v >= 2.0) ge2++;
        double p2Base = (double) ge2 / (double) n;

        // current streak of <2 from newest backwards
        int streak = 0;
        for(double v: newestFirst){
            if (v < 2.0) streak++; else break;
        }

        // score: longer streak increases urgency; base probability increases confidence.
        double streakFactor = Math.min(1.0, streak / 10.0); // 0..1 at streak=10
        double score = 0.55 * p2Base + 0.45 * streakFactor;
        boolean entry2 = (streak >= 6 && p2Base >= 0.35) || (score >= 0.55);
        return new Result(streak, p2Base, entry2, score);
    }
}
