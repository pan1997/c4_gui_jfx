/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4adv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.function.IntSupplier;

/**
 *
 * @author Pankaj
 */

public abstract class player {
    int bms;
    int staticMo[];

    abstract int analyse(board b, int col, long t);

    abstract String getpv(board b, int col);

    void drop(int i) {
        System.out.println("Dropped " + i);
        //do nothing
    }

    void clear() {
        System.out.println("Cleared");
    }

    int getMove(board b, int col, long t) {
        return analyse(b, col, t);
    }

    static int[][] mos;

    static {
        mos = new int[40][];
        for (int i = 0; i < 40; i++) {
            mos[i] = new int[i];
            if (i > 0) {
                mos[i][0] = i / 2;
                //System.out.print(i/2);
                for (int j = 1; j < i; j++)
                    mos[i][j] = mos[i][j - 1] + (j % 2 == 0 ? 1 : -1) * j;
                //System.out.println();
            }
        }
    }
}

class externalPlayer extends player {
    Process prg;
    PrintWriter out;
    BufferedReader bin;

    externalPlayer(String s, String... x) throws IOException {
        ProcessBuilder p = new ProcessBuilder(s);
        //System.out.println(p.);
        prg = p.start();
        out = new PrintWriter(prg.getOutputStream());
        for (int i = 0; i < x.length; i++)
            out.println(x[i]);
        out.flush();
        bin = new BufferedReader(new InputStreamReader(prg.getInputStream()));
        try {
            int r;
            while ((r = bin.read()) != -1) {
                System.out.print((char) r);
                if (!bin.ready())
                    break;
            }
        } catch (Exception e) {
            System.out.println("end");
            System.out.println(e);
        }
    }

    void drop(int i) {
        System.out.println("drop " + i);
        out.println("drop " + i);
        out.flush();
    }

    void clear() {
        bms = 0;
        out.println("clear");
        out.flush();
    }

    @Override
    int analyse(board b, int col, long t) {
        out.println("analyse " + t);
        out.flush();
        String s;
        try {
            while ((s = bin.readLine()) != null) {
                System.out.println(s);
                s = s.trim();
                if (s.startsWith("BM")) {
                    StringTokenizer st = new StringTokenizer(s);
                    while (st.hasMoreTokens())
                        if (st.nextToken().equals("ms"))
                            bms = Integer.parseInt(st.nextToken());
                    return s.charAt(3) - '0';
                }
            }
        } catch (Exception e) {
            System.out.println("Error");
        }
        return 0;
    }

    @Override
    String getpv(board b, int col) {
        // TODO Auto-generated method stub
        return "";
    }
}

class NegaScout extends player {
    IntSupplier eval;
    HashMap<Integer, int[]> trans;
    int evalN;
    int n;

    NegaScout(int n) {
        evalN = n;
        trans = new HashMap<>(75000000);
    }

    int pvs(board b, int alpha, int beta, int d, int col) {
        d--;
        n++;
        int hash, bms, bpm, ms, i;
        bpm = b.calcQuick();
        if (bpm == 0) return 0;
        else if (bpm == 1)
            d++;
        if (b.win[col] != -1)
            return 900 + d;
        else if ((bpm = b.win[col == b.max ? b.min : b.max]) != -1)
            d++;
        if (d < 0)
            return eval.getAsInt() * (col == b.max ? 1 : -1);
        int[] t = trans.get(hash = b.hash());
        if (t == null) {
            trans.put(hash, t = new int[4]);
            t[0] = d;
            t[1] = bpm = -1;
            t[2] = bms = -1000;
            t[3] = 1000;
        } else {
            if (t[0] >= d) {
                if (t[2] >= beta) return t[2];
                if (t[3] <= alpha) return t[3];
                if (t[3] == t[2]) return t[2];
                alpha = alpha > t[2] ? alpha : t[2];
                beta = beta < t[3] ? beta : t[3];
            }
            /*else if(t[0]<d+2&&beta+1==alpha)
            {
                if(t[2]>beta+8) return t[2];
                if(t[3]<alpha-8) return t[3];
            }*/
            else {
                t[0] = d;
                if (t[2] < 800) t[2] = -1000;
                else return t[2];
                if (t[3] > -800) t[3] = 1000;
                else return t[3];
            }
            bpm = t[1];
            if (bpm != -1 && b.legal(bpm)) {
                b.drop(bpm, col);
                bms = -pvs(b, -beta, -alpha, d, col == b.max ? b.min : b.max);
                b.remove(bpm);
                if (bms > alpha) {
                    alpha = bms;
                    if (alpha >= beta) {
                        t[2] = bms;
                        return bms;
                    }
                }
            } else {
                t[1] = -1;
                bpm = -1;
                bms = -1000;
            }
        }
        for (int j = 0; j < b.W; j++) {
            i = staticMo[j];
            if (b.legal(i) && bpm != i) {
                b.drop(i, col);
                ms = -pvs(b, -alpha - 1, -alpha, d, col == b.max ? b.min : b.max);
                if (alpha < ms && ms < beta)
                    ms = -pvs(b, -beta, -ms, d, col == b.max ? b.min : b.max);
                b.remove(i);
                if (ms > bms) {
                    bms = ms;
                    t[1] = i;
                    if (alpha < ms) {
                        alpha = ms;
                        if (alpha >= beta) {
                            t[2] = bms;
                            return bms;
                        }
                    }
                }
            }
        }
        if (bms < beta) t[3] = bms;
        return bms;
    }

    @Override
    public String toString() {
        return "NegaScout(cut)with transpositions wint eval" + evalN;
    }

    @Override
    public int analyse(board b, int col, long t) {
        staticMo = mos[b.W];
        switch (evalN) {
            case 1:
                eval = b::eval1;
                break;
            case 2:
                eval = b::eval2;
                break;
            case 3:
                eval = b::eval3;
                break;
        }
        trans.clear();
        n = 0;
        int nt = 0;
        long s = System.currentTimeMillis();
        int i;
        int pnodes = 0;
        for (i = 3; (System.currentTimeMillis() - s) < t && i < 25; i++) {
            pnodes = n;
            n = 0;
            System.out.print((bms = pvs(b, -2000, 2000, i, col)) / 8.0);
            nt += n;
            System.out.print(" pv->");
            System.out.print(getpv(b, col));
            System.out.println(" ) nodes " + n + " (real nodes)=" + trans.size());
            if (pnodes == n)
                break;
            if (bms > 500 || bms < -500) return trans.get(b.hash())[1];
        }
        i--;
        System.out.println("Total time " + (System.currentTimeMillis() - s) + " total nodes " + nt + "@" + (nt / (System.currentTimeMillis() - s)) + "(Real=" + trans.size() + ") min depth " + i);
        return trans.get(b.hash())[1];
    }

    @Override
    String getpv(board b, int col) {
        int[] t = trans.get(b.hash());
        if (t == null || t[1] == -1) return "";
        String ans = " " + t[1];
        b.drop(t[1], col);
        ans += getpv(b, col == b.max ? b.min : b.max);
        b.remove(t[1]);
        return ans;
    }
}

class NegaScoutEx extends player {
    IntSupplier eval;
    HashMap<Integer, int[]> trans;
    int evalN;
    int n;

    NegaScoutEx(int n) {
        evalN = n;
        trans = new HashMap<>(10000000);
    }

    int pvs(board b, int alpha, int beta, int d, int col) {
        d--;
        int pd = d;
        n++;
        int hash, bms, bpm, ms, i;
        bpm = b.calcQuick();
        if (bpm == 0) return 0;
        else if (bpm == 1)
            d++;
        if (b.win[col] != -1)
            return 900 + d;
        else if ((bpm = b.win[col == b.max ? b.min : b.max]) != -1)
            d++;
        if (d < 0)
            return eval.getAsInt() * (col == b.max ? 1 : -1);
        int[] t = trans.get(hash = b.hash());
        if (t == null) {
            trans.put(hash, t = new int[4]);
            t[0] = d;
            t[1] = bpm = -1;
            t[2] = bms = -1000;
            t[3] = 1000;
        } else {
            if (t[0] >= d) {
                if (t[2] >= beta) return t[2];
                if (t[3] <= alpha) return t[3];
                if (t[3] == t[2]) return t[2];
                alpha = alpha > t[2] ? alpha : t[2];
                beta = beta < t[3] ? beta : t[3];
            }
            /*else if(t[0]<d+2&&beta+1==alpha)
            {
                if(t[2]>beta+8) return t[2];
                if(t[3]<alpha-8) return t[3];
            }*/
            else {
                t[0] = d;
                if (t[2] < 800) t[2] = -1000;
                else return t[2];
                if (t[3] > -800) t[3] = 1000;
                else return t[3];
            }
            bpm = t[1];
            if (bpm != -1 && b.legal(bpm)) {
                b.drop(bpm, col);
                bms = -pvs(b, -beta, -alpha, d, col == b.max ? b.min : b.max);
                b.remove(bpm);
                if (bms > alpha) {
                    alpha = bms;
                    if (alpha >= beta) {
                        t[2] = bms;
                        return bms;
                    }
                }
            } else {
                t[1] = -1;
                bpm = -1;
                bms = -1000;
            }
        }
        for (int j = 0; j < b.W; j++) {
            i = staticMo[j];
            if (b.legal(i) && bpm != i) {
                b.drop(i, col);
                ms = -pvs(b, -alpha - 1, -alpha, pd == d && d < 4 ? d - 1 : d, col == b.max ? b.min : b.max);
                if (alpha < ms && ms < beta)
                    ms = -pvs(b, -beta, -ms, d, col == b.max ? b.min : b.max);
                b.remove(i);
                if (ms > bms) {
                    bms = ms;
                    t[1] = i;
                    if (alpha < ms) {
                        alpha = ms;
                        if (alpha >= beta) {
                            t[2] = bms;
                            return bms;
                        }
                    }
                }
            }
        }
        if (bms < beta) t[3] = bms;
        return bms;
    }

    @Override
    public String toString() {
        return "NegaScoutEx(cut)with transpositions wint eval" + evalN;
    }

    @Override
    public int analyse(board b, int col, long t) {
        staticMo = mos[b.W];
        switch (evalN) {
            case 1:
                eval = b::eval1;
                break;
            case 2:
                eval = b::eval2;
                break;
            case 3:
                eval = b::eval3;
                break;
        }
        trans.clear();
        n = 0;
        int nt = 0;
        long s = System.currentTimeMillis();
        int i;
        int pnodes;
        for (i = 3; (System.currentTimeMillis() - s) < t && i < 25; i++) {
            pnodes = n;
            n = 0;
            System.out.print((bms = pvs(b, -2000, 2000, i, col)) / 8.0);
            nt += n;
            System.out.print(" pv->");
            System.out.print(getpv(b, col));
            System.out.println(" ) nodes " + n + " (real nodes)=" + trans.size());
            if (pnodes == n)
                break;
            if (bms > 500 || bms < -500) return trans.get(b.hash())[1];
        }
        i--;
        System.out.println("Total time " + (System.currentTimeMillis() - s) + " total nodes " + nt + "@" + (nt / (System.currentTimeMillis() - s)) + "(Real=" + trans.size() + ") min depth " + i);
        return trans.get(b.hash())[1];
    }

    @Override
    String getpv(board b, int col) {
        int[] t = trans.get(b.hash());
        if (t == null || t[1] == -1) return "";
        String ans = " " + t[1];
        b.drop(t[1], col);
        ans += getpv(b, col == b.max ? b.min : b.max);
        b.remove(t[1]);
        return ans;
    }
}