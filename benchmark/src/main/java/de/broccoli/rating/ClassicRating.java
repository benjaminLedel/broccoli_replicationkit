package de.broccoli.rating;

import de.broccoli.context.BroccoliContext;
import de.broccoli.dataimporter.models.Bug;
import org.eclipse.jgit.api.ResetCommand;

import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassicRating {

    private int top1;
    private int top5;
    private int top10;

    private double map;
    private double mrr;

    private HashMap<String, List<Result>> results = new HashMap<>();
    private AlgorithmResult algorithmResult;


    public ClassicRating()
    {
        readData();
    }

    private void readData()
    {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(BroccoliContext.getInstance().getOutputFile()));
            String line = reader.readLine();
            while (line != null) {
                String[] entries = line.split("\t");
                if(!results.containsKey(entries[0]))
                    results.put(entries[0], new ArrayList<>());
                results.get(entries[0]).add(new Result(entries));
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void calcAndPrint()
    {
        calc();
        print();
    }

    public AlgorithmResult calc()
    {
        int size = BroccoliContext.getInstance().getBugList().size();
        makeTopKResult(BroccoliContext.getInstance().getBugList());
        map = makeMAP(BroccoliContext.getInstance().getBugList());
        mrr = makeMRR(BroccoliContext.getInstance().getBugList());
        algorithmResult = new AlgorithmResult(BroccoliContext.getInstance().getProjectName(),(double)top1/size,(double)top5/size,(double)top10/size,map,mrr);
        return algorithmResult;
    }

    public void print()
    {
        int size = BroccoliContext.getInstance().getBugList().size();
        System.out.println("+++++++++++++++ Results: " + BroccoliContext.getInstance().getProjectName() + " / " + BroccoliContext.getInstance().getAlgorithm() + " +++++++++++++++" );
        System.out.println("TOP1 " + (double)top1/size);
        System.out.println("TOP5 " + (double)top5/size);
        System.out.println("TOP10 " + (double)top10/size);

        System.out.println("");
        System.out.println("MAP " + map);
        System.out.println("MRR " + mrr);

        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++");
    }

    public void makeTopKResult(ArrayList<Bug> bugobjs) {
        for(Bug b : bugobjs){

            int local_top1=0;
            int local_top5=0;
            int local_top10=0;

            List<Result> resultsLocal = this.results.get(b.getBugId());
            if(resultsLocal == null)
                continue;

            for (Result result: resultsLocal) {
                String fileName = result.getFile();
                int rank = result.getRank();

                if(!checkContained(b, fileName)) continue;
                if (rank < 1) local_top1++;
                if (rank < 5) local_top5++;
                if (rank <10) local_top10++;
            }

            if (local_top1 >0 ) top1++;
            if (local_top5 >0 ) top5++;
            if (local_top10 >0 ) top10++;
        }
    }
//
//    /**
//     *  ��ü ���׸���Ʈ�� ���� MAP ���
//     * @param bugobjs
//     * @param weights
//     * @return
//     */
    public double makeMAP( ArrayList<Bug> bugobjs){
        double map = 0;
        for(Bug b : bugobjs){

            //�� ���׸���Ʈ�� ���� precision ���
            double sum=0;
            int retrieved_d = 0;
            List<Result> resultsLocal = this.results.get(b.getBugId());
            if(resultsLocal == null)
                continue;

            for (Result result: resultsLocal) {
                int rank = result.getRank();
                String fileName = result.getFile();

                if(checkContained(b, fileName)){
                    retrieved_d++;
                    double precision_i = (double)retrieved_d /(rank+1);
                    sum += precision_i;
                }
            }

            map += (sum/b.getSet().size());	//av_precision  for 1 bug;
        }

        return map/bugobjs.size();
    }
//
//    /**
//     * ������ weights�� ���ؼ� MRR ���
//     * @param bugobjs
//     * @return
//     */
    public double makeMRR( ArrayList<Bug> bugobjs ){

        double mrr = 0;
        for(Bug b : bugobjs){

            //�� ���׸���Ʈ�� ���� precision ���
            double rr=0;
            List<Result> resultsLocal = this.results.get(b.getBugId());
            if(resultsLocal == null)
                continue;

            for (Result result: resultsLocal) {
                int rank = result.getRank();
                String fileName = result.getFile();

                if(checkContained(b, fileName)){
                    rr = (double)1/(rank+1);
                    break;
                }
            }
            mrr += rr;
        }

        return mrr/bugobjs.size();
    }

    private boolean checkContained(Bug bug, String filename)
    {
        // no need to check, since this is already the content of the
        return true;
    }
}
