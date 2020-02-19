package cs455.overlay.routing;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class SummaryTable {

    public ArrayList<SummaryEntry> table;

    public SummaryTable(){
        table = new ArrayList<>();
    }

    public class SummaryEntry{
        public int nodeId;
        public long sent;
        public long relayed;
        public long received;
        public long sumsent;
        public long sumreceived;


        public SummaryEntry(int nodeId, long sent, long relayed, long received, long sumsent, long sumreceived){
            this.nodeId = nodeId;
            this.sent = sent;
            this.relayed = relayed;
            this.received = received;
            this.sumsent = sumsent;
            this.sumreceived = sumreceived;

        }
    }

    public void addEntry(int nodeId, long sent, long relayed, long received, long sumsent, long sumreceived){
        SummaryEntry summaryEntry = new SummaryEntry(nodeId, sent, relayed, received, sumsent, sumreceived);
        table.add(summaryEntry);
    }

    public void printSummary(){
        long[] sums;
        SummaryEntry summaryEntryForTotals = new SummaryEntry(0,0,0,0,0,0);
        System.out.println(" -= NODE[ ID] =- | -= Packets Sent =- | -= Packets Received =- | -= Packets Relayed =- | -= Sum Values Sent =- | -= Sum Values Received =- ");
        for(SummaryEntry e : table){
            System.out.println(" << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> ");
            System.out.println(" -# Node["+
                    String.format("%1$3s", e.nodeId)
                    +"] #- | -# " +
                    String.format("%1$13s", e.sent)
                    +" #- | -# "+
                    String.format("%1$16s", e.received)
                    +" #- | -# "+
                    String.format("%1$15s", e.relayed)
                    +" #- | -# "+
                    String.format("%1$15s", e.sumsent)
                    +" #- | -# "+
                    String.format("%1$19s", e.sumreceived)
                    +" #- ");
            summaryEntryForTotals.sent += e.sent;
            summaryEntryForTotals.relayed += e.relayed;
            summaryEntryForTotals.received += e.received;
            summaryEntryForTotals.sumsent += e.sumsent;
            summaryEntryForTotals.sumreceived += e.sumreceived;

        }
        System.out.println(" == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == == ");
        System.out.println(" -=  Totals   =- | " +
                "-= "+
                String.format("%1$13s", summaryEntryForTotals.sent)
                +" =- | " +
                "-= "+
                String.format("%1$16s", summaryEntryForTotals.received)
                +" =- | " +
                "-= "+
                String.format("%1$15s", summaryEntryForTotals.relayed)
                +" =- | " +
                "-= "+
                String.format("%1$15s", summaryEntryForTotals.sumsent)
                +" =- | " +
                "-= "+
                String.format("%1$19s", summaryEntryForTotals.sumreceived)
                +" =- ");
    }

}
