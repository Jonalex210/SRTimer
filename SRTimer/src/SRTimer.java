import java.util.Date;
import java.util.ArrayList;
import java.util.Timer; 
import java.util.TimerTask; 

/*
 * Selective Repeat (SR) logical timer data structure
 * The aim of this implementation is to demonstrate how multiple SR logical timer could be implemented over a single physical timer 
 * 
 * */
public class SRTimer {	
	
	//New data structure ArrayList to store the logical timers
	public static ArrayList<long[]> srTimers = new ArrayList<long[]>(); //[[0:Seq#, 1:relTimeout, 2:absTimeout, 3:timestamp], [...], ...]
	//Timer from the java util, timer class, used in this implementation a physical timer.
	private static Timer timer = new Timer();

	private static long start = System.currentTimeMillis();
	
    public SRTimer() {
    	//Debug lines
    	System.out.println("[Time Elapsed: "+timeElapsed()+"ms]"+">>"+"SR Logical Timers Data Structure Started!");
    	System.out.println("-----------------------------------------State of Data Structure-----------------------------------------");
		
		System.out.println("---------------------------------------------------End---------------------------------------------------"+"\n");
    }
    
    /*
     * printDataStructure is mainly used for debug to understand what is happening in the data structure during oprtions
     * */
	public static void printDataStructure() {
		System.out.println("-----------------------------------------State of Data Structure-----------------------------------------");
		for(long[] srTimer : srTimers) {
			System.out.println("Logical Timer" + 
					": Seq# = " + srTimer[0] +
					"; RelativeTimeout = " + srTimer[1] + "ms" +
					"; AbsoluteTimeout = " + srTimer[2] + "ms" +
					"; Timestamp = " + srTimer[3] + "ms");
		}
		System.out.println("---------------------------------------------------End---------------------------------------------------"+"\n");
	}
	
	/*
	 * insertTimer is responsible for inserting new logical timers into the data structure.
	 * */
	public void insertTimer(int seqNum, long absTimeout) {
		//to capture the timestamp
		Date date = new Date();
		//initialization of the relative timeout
		long relTimeout = absTimeout;
		//setting up the new logical timer 
		long[] srTimer_new = {seqNum, relTimeout, absTimeout, date.getTime()};
		
		if(srTimers.isEmpty()){
			//insert the first logical timer
			srTimers.add(srTimer_new);
			
			//Start timer
			timer.schedule(new TimerTask() {
		        public void run() {
		            timeout();
		        }
		    }, absTimeout);
			
			//Debug lines: print text
			System.out.println("[Time Elapsed: "+timeElapsed()+"ms]"+">>"+"Packet Seq# = " + seqNum + ": Logical Timer pushed to Physical Timer with value = " + relTimeout + "ms" );
			printDataStructure();
		} else {
			//To determine if we reached the end of the array
			boolean isEnd = true;
			//Determination of the next insertion for the new logical timer
			for(long[] srTimer : srTimers) {
				//calculation of the new timer's relative timeout
				relTimeout = calcRelTimeout(srTimer, srTimer_new);
				//If relative timeout is negative insert in front!
				if(relTimeout < 0) {
					srTimers.add(srTimers.indexOf(srTimer), srTimer_new);
					isEnd = false;
					break;
				} else {
					isEnd = true;
				}
	
		    }
			//if we reached the end of the data structure without inserting
			if(isEnd) {
				srTimers.add(srTimer_new);
				System.out.println("[Time Elapsed: "+timeElapsed()+"ms]"+">>"+"Packet Seq# = " + seqNum + ": Logical Timer created" );
				printDataStructure();
			} 
			
			//Recalculate every relative timeout after insertion
			reFreshRelTimeout();
			
			//Reset physical timeout to new value if the insertion happened at the index 0.
			if(srTimers.indexOf(srTimer_new)==0) {
				//Stop current timer
				timer.cancel();
				//start new timer
				timer = new Timer();
				timer.schedule(new TimerTask() {
			        public void run() {
			            timeout();
			        }
			    }, absTimeout);
				
				//Degub lines: print text
				System.out.println("[Time Elapsed: "+timeElapsed()+"ms]"+">>"+"Packet Seq# = " + seqNum + ": Logical Timer pushed to Physical Timer with value = " + srTimer_new[1] + "ms" );
				printDataStructure();
			}
		}
		
	}
	
	/*
	 * timeout is called when ever a logical timer expires and can be used to trigger Selective Repeat to resend the packet
	 * */	
	public static void timeout() {
		//Debug lines: Notify on expired timer
		System.out.println("[Time Elapsed: "+timeElapsed()+"ms]"+">>"+"Packet Seq# = " + srTimers.get(0)[0] + ": Timer Expired");
		//Remove expired timer
		srTimers.remove(0);
		printDataStructure();
		//Parse the next logical timer to the physical timer
		if(!srTimers.isEmpty()){
			timer.schedule(new TimerTask() {
		        public void run() {
		            timeout();
		        }
		    }, srTimers.get(0)[1]);
			//Debug lines: print text
			System.out.println("[Time Elapsed: "+timeElapsed()+"ms]"+">>"+"Packet Seq# = " + srTimers.get(0)[0] + ": Logical Timer pushed to Physical Timer with value = " + srTimers.get(0)[1] + "ms" );
			printDataStructure();
		}
	}
		
	
	/*
	 * calcRelativeTimeout determines the relative timeout of logical time with respect to the immediately ahead
	 * */
	public static long calcRelTimeout(long[] logicalTimer_prev, long[] logicalTimer) {
		
		long relTimeout, absTimeout, absTimeout_prev, timestamp, timestamp_prev;
		absTimeout = logicalTimer[2];
		absTimeout_prev = logicalTimer_prev[2];
		timestamp = logicalTimer[3];
		timestamp_prev = logicalTimer_prev[3];
		
		relTimeout = (absTimeout - absTimeout_prev) + (timestamp - timestamp_prev);
		return relTimeout;
	}
	
	/*
	 * reFreshRelTimeout determines the new relative timeout for each logical timeout in the datastructure.
	 * */
	public static void reFreshRelTimeout() {
		
		long relTimeout, absTimeout, absTimeout_prev, timestamp, timestamp_prev;
		
		for(int i=1; i<srTimers.size(); i++) {
			absTimeout = srTimers.get(i)[2];
			absTimeout_prev = srTimers.get(i-1)[2];
			timestamp = srTimers.get(i)[3];
			timestamp_prev = srTimers.get(i-1)[3];
			
			relTimeout = (absTimeout - absTimeout_prev) + (timestamp - timestamp_prev);
			srTimers.get(i)[1] = relTimeout;
		}
		
	}
	
	/*
	 * Determines the elapse time from the creation of the data structure. 
	 * */
	private static long timeElapsed() {
		long finish = System.currentTimeMillis();
		return (finish - start);	
	}
	
}
