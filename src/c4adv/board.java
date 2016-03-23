/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package c4adv;

/**
 *
 * @author Pankaj
 */
public class board
{
    static final int max=1,min=2;
    static final int dw=64,dl=10,ds=1;
    int[][] t;
    //static final int[] weigth={10,7,4,1,1,1,1,1,1,1,1,1};
    final int W,H;//=15,H=7;
    int[] h;
    int[] b;
    int[][] countblock;
    int[][] cellpartof;
    int hash;
    board(int aa,int ba)
    {
        //W=a;
        //H=b;
        W=aa;
        H=ba;
        h=new int[W];
        b=new int[W*H];
        win=new int[3];
        t=new int[b.length][3];
        hash=0;
        for (int[] t1 : t) {
            t1[0] = (int)(Integer.MAX_VALUE*Math.random());
            t1[1] = (int)(Integer.MAX_VALUE*Math.random());
            t1[2] = (int)(Integer.MAX_VALUE*Math.random());
            hash ^= t1[0];
        }
        java.util.ArrayList<Integer> bt[]=new java.util.ArrayList[W*H];
        for(int i=0;i<bt.length;i++)
            bt[i]=new java.util.ArrayList<Integer>();
            
            
        int n=0;
        for(int i=0;i<W;i++)
        {
            for(int j=0;j<H;j++)
            {
                if(i+3<W)
                {
                    bt[index(i,j)].add(n);
                    bt[index(i+1,j)].add(n);
                    bt[index(i+2,j)].add(n);
                    bt[index(i+3,j)].add(n);
                    n++;
                }
                if(j+3<H)
                {
                    bt[index(i,j)].add(n);
                    bt[index(i,j+1)].add(n);
                    bt[index(i,j+2)].add(n);
                    bt[index(i,j+3)].add(n);
                    n++;
                }
                if(i+3<W&&j+3<H)
                {
                    bt[index(i,j)].add(n);
                    bt[index(i+1,j+1)].add(n);
                    bt[index(i+2,j+2)].add(n);
                    bt[index(i+3,j+3)].add(n);
                    n++;
                }
                if(i+3<W&&j>2)
                {
                    bt[index(i,j)].add(n);
                    bt[index(i+1,j-1)].add(n);
                    bt[index(i+2,j-2)].add(n);
                    bt[index(i+3,j-3)].add(n);
                    n++;
                }
            }
        }
        
        countblock=new int[n][3];
        cellpartof=new int[W*H][];
        for(int i=0;i<cellpartof.length;i++)
        {
            cellpartof[i]=new int[bt[i].size()];
            for(int j=0;j<cellpartof[i].length;j++)
                cellpartof[i][j]=bt[i].get(j);
        }
    }
    /*
     * copies bd into itself
     */
    void copy(board bd)
    {
        System.arraycopy(bd.b,0,b,0,b.length);
        System.arraycopy(bd.h,0,h,0,h.length);
        hash=bd.hash;
    }

    /* returns the number of legal moves in the current board
     */
    int nl()
    {
        int a=0;
        for(int i=0;i<W;i++)
            if(legal(i)) a++;
        return a;
    }
    private final int index(int i,int j)
    {
        return i*H+j;
    }
    void drop(int i,int col)
    {
        int ind=index(i,h[i]);
        b[ind]=col;
        hash^=t[ind][0];
        hash^=t[ind][col];
        for(int block:cellpartof[ind])
            countblock[block][col]++;
        h[i]++;
    }
    void remove(int i)
    {
        h[i]--;
        int ind=index(i,h[i]);
        for(int block:cellpartof[ind])
            countblock[block][b[ind]]--;
        hash^=t[ind][0];
        hash^=t[ind][b[ind]];
        b[ind]=0;
    }
    //int hash()
    //{
    //    int h=0;
    ///    for(int i=0;i<b.length;i++)
    //        h^=t[i][b[i]];
    //    return h;
    //}
    int hash()
    {
        return hash;
    }
    boolean legal(int i)
    {
        return h[i]<H;
    }
    int eval1()
    {
        int dtbymax=0,dtbymin=0,tbymax=0,tbymin=0,ltbymax=0,ltbymin=0;
        int ind,hm;
        boolean mxt,pxt,mnt,pnt,lowest;
        out:for(int i=0;i<W;i++)
        {
            pxt=false;
            pnt=false;
            lowest=true;
            hm=h[i];
            if(i>0&&h[i-1]>hm) hm=h[i-1];
            if(i<W-1&&h[i+1]>hm) hm=h[i+1];
            hm=Math.min(hm+1,H);
            for(int j=h[i];j<hm;j++)
            {
                mxt=mnt=false;
                ind=index(i,j);
                for(int block:cellpartof[ind])
                    if(countblock[block][max]==0&&countblock[block][min]==3)mnt=true;
                    else if(countblock[block][max]==3&&countblock[block][min]==0)mxt=true;
                if(mxt&&mnt)
                {
                    continue out;
                }
                if(mxt)
                {
                    if(pxt)
                    {
                        dtbymax++;
                        continue out;
                    }
                    else if(lowest) ltbymax++;
                    
                    if(!pnt) tbymax++;
                    lowest=false;
                }
                if(mnt)
                {
                    if(pnt)
                    {
                        dtbymin++;
                        continue out;
                    }
                    else if(lowest) ltbymin++;
                    if(!pxt)tbymin++;
                    lowest=false;
                }
                pnt=mnt;
                pxt=mxt;
            }
        }
        return
            8*(tbymax-tbymin)
           +dl*(ltbymax-ltbymin)
           +dw*(dtbymax-dtbymin);
    }
    int eval3()
    {
        int dtbymax=0,dtbymin=0,tbymax=0,tbymin=0,ltbymax=0,ltbymin=0;
        int ind,hm;
        boolean mxt,pxt,mnt,pnt,mx,mn;
        out:for(int i=0;i<W;i++)
        {
            pxt=false;
            pnt=false;
            mx=mn=false;
            hm=h[i];
            if(i>0&&h[i-1]>hm) hm=h[i-1];
            if(i<W-1&&h[i+1]>hm) hm=h[i+1];
            hm=Math.min(hm+1,H);
            for(int j=h[i];j<hm;j++)
            {
                mxt=mnt=false;
                ind=index(i,j);
                for(int block:cellpartof[ind])
                    if(countblock[block][max]==0&&countblock[block][min]==3)mnt=true;
                    else if(countblock[block][max]==3&&countblock[block][min]==0)mxt=true;
                if(mxt&&mnt)
                {
                    continue out;
                }
                if(mxt)
                {
                    if(pxt)
                    {
                        dtbymax++;
                        continue out;
                    }
                    else if(!(mx||mn)) ltbymax++;
                    else if(!pnt)tbymax++;
                    mx=true;
                }
                if(mnt)
                {
                    if(pnt)
                    {
                        dtbymin++;
                        continue out;
                    }
                    else if(!(mx||mn)) ltbymin++;
                    else if(!pxt) tbymin++;
                    mn=true;
                }
                pnt=mnt;
                pxt=mxt;
            }
        }
        return
            50*(tbymax-tbymin)
           +51*(ltbymax-ltbymin)
           +60*(dtbymax-dtbymin);
    }
    int eval2()
    {
        int dtbymax=0,dtbymin=0,etbymax=0,otbymax=0,etbymin=0,otbymin=0,ltbymax=0,ltbymin=0;
        int ind,hm;
        boolean mxt,pxt,mnt,pnt,lowest;
        out:for(int i=0;i<W;i++)
        {
            pxt=false;
            pnt=false;
            lowest=true;
            hm=h[i];
            if(i>0&&h[i-1]>hm) hm=h[i-1];
            if(i<W-1&&h[i+1]>hm) hm=h[i+1];
            hm=Math.min(hm+1,H);
            for(int j=h[i];j<hm;j++)
            {
                mxt=mnt=false;
                ind=index(i,j);
                for(int block:cellpartof[ind])
                    if(countblock[block][max]==0&&countblock[block][min]==3)mnt=true;
                    else if(countblock[block][max]==3&&countblock[block][min]==0)mxt=true;
                if(mxt&&mnt)
                {
                    if(j%2==0)
                    {
                        otbymax++;
                        otbymin++;
                    }
                    else 
                    {
                        etbymax++;
                        etbymin++;
                    }
                    continue out;
                }
                if(mxt)
                {
                    if(pxt)
                    {
                        dtbymax++;
                        continue out;
                    }
                    else if(lowest) ltbymax++;
                    else if(!pnt) {if(j%2==0) otbymax++;else etbymax++;}
                    lowest=false;
                }
                if(mnt)
                {
                    if(pnt)
                    {
                        dtbymin++;
                        continue out;
                    }
                    else if(lowest) ltbymin++;
                    else if(!pxt) {if(j%2==0) otbymin++;else etbymin++;}
                    lowest=false;
                }
                pnt=mnt;
                pxt=mxt;
            }
        }
        return
            8*(otbymax-etbymin)
           +12*(etbymax-otbymin)
           +14*(ltbymax-ltbymin)
           +dw*(dtbymax-dtbymin);
    }
    int win[];
    int calcQuick()
    {
        win[max]=-1;
        win[min]=-1;
        int ind;
        int n=0;
        for(int i=0;i<W;i++)
        if(legal(i))
        {
            n++;
            ind=index(i,h[i]);
            for(int block:cellpartof[ind])
                if(countblock[block][max]==0&&countblock[block][min]==3)win[min]=i;
                else if(countblock[block][max]==3&&countblock[block][min]==0)win[max]=i;
        }
        return n;
    }
    void print()
    {
        for(int i=0;i<H;i++)
        {
            for(int j=0;j<W;j++)
                System.out.print("|"+(b[index(j,H-i-1)]==max?"X":b[index(j,H-i-1)]==min?"O":" "));
            System.out.println("|");
        }
        System.out.print(" ");
        for(int i=0;i<W;i++)
            System.out.print(i+(i<10?" ":""));
        System.out.println();
    }
    void clear()
    {
        hash=0;
        for(int i=0;i<b.length;i++)
        {
            b[i]=0;
            hash^=t[i][0];
        }
        for(int i=0;i<h.length;i++)
            h[i]=0;
        for(int i=0;i<countblock.length;i++)
            for(int j=0;j<countblock[i].length;j++)
                countblock[i][j]=0;
    }
}