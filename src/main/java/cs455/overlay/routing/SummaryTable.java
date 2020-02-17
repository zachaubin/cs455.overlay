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
        System.out.println("-= NODE [id] =- | -= Packets Sent =- | -= Packets Received =- | -= Packets Relayed =- | -= Sum Values Sent =- | -= Sum Values Received =- ");
        for(SummaryEntry e : table){
            System.out.println(" << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> << >> ");
            System.out.println(" -= Node["+e.nodeId+"] =- | -= "+e.sent+" =- | -= "+e.received+" =- | -= "+e.relayed+" =- | -= "+e.sumsent+" =- | -= "+e.sumreceived+"=- ");
        }
    }

}
