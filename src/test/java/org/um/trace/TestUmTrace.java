package org.um.trace;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestUmTrace {

	private void printBar(String msg) {
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		System.out.println("@ " + msg );
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	}
	
	private void stop(int sleeep) {
		try {
			Thread.sleep(sleeep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * By holding an instance of UmTrace it is possible to get rid of the thread local. 
	 * 
     * <ul>
	 * <li>UMTRACE Aggregates. Node: UmTrace [path=/ProgramStart] Time: 1.001000 s. Pct: 100.00%
	 * <li>UMTRACE Aggregates. Node: UmTrace [path=/ProgramStart/Task 1] Time: 0.100000 s. Pct: 9.99%
     * <li>UMTRACE Aggregates. Node: UmTrace [path=/ProgramStart/Task 2] Time: 0.900000 s. Pct: 89.91%
     * </ul>
	 */
	@Test
	void testNoStatic() {
		printBar("Example without static, without thread local");
		
		UmTrace dt = new UmTrace();
		
		// A root log is needed, or results will be off. 
		// Since we treat each instance of UmTrace as an immutable we have to assign it over again
		// at each enter and exit. 
		dt = dt.push("ProgramStart");
	
		dt = dt.push("Task 1");
		stop(100);
		dt = dt.pop();
		
		dt = dt.push("Task 2");
		stop(900);
		dt = dt.pop();
		
		// Exit the root context will trigger a dump to system out
		dt = dt.pop();
	}

	/**
	 * Adding a context make it possible to keep track of different aspects. 
	 * 
	 * <ul>
	 * <li>UMTRACE Aggregates. Node: UmTrace [path=/Count Beans:Task 1] Time: 0.900000 s. Pct: 100.00%
	 * <li>UMTRACE Aggregates. Node: UmTrace [path=/Count Apples:Program Start] Time: 1.901000 s. Pct: 100.00%
	 * <li>UMTRACE Aggregates. Node: UmTrace [path=/Count Apples:Program Start/Count Apples:Task 1] Time: 0.100000 s. Pct: 5.26%
	 * <li>UMTRACE Aggregates. Node: UmTrace [path=/Count Apples:Program Start/Count Apples:Task 2] Time: 0.900000 s. Pct: 47.34%
	 * <li>UMTRACE Aggregates. Node: UmTrace [path=/Count Pears:Task 1] Time: 0.900000 s. Pct: 100.00%
	 * </ul>
	 */
	@Test
	void testWithContext() {
		String ctx = "Count Apples";
		String ctx2 = "Count Beans";
		String ctx3 = "Count Pears";
		
		printBar(ctx + " and " + ctx2);
		 
		// For the time being a root trace wrapping all other traces is needed (in this case 'ProgramStart'). 
		UmTrace.enter(ctx, "Program Start");
		
		UmTrace.enter(ctx, "Task 1"); 
		stop(100);
		UmTrace.exit(ctx);
		
		UmTrace.enter(ctx, "Task 2");
		stop(900);
		UmTrace.exit(ctx);

		UmTrace.enter(ctx2, "Task 1");
		stop(900);
		// Exit 'Count Beans' will trigger a dump to system out
		UmTrace.exit(ctx2);		

		// Exit 'Count Apples' context will trigger a dump to system out
		UmTrace.exit(ctx);
		
		UmTrace.enter(ctx3, "Task 1");
		stop(900);
		// Exit 'Count Pears' will trigger a dump to system out
		UmTrace.exit(ctx3);				
		
		System.out.println("Note: '" + ctx2 + "' were executed as part of '" + ctx + "', which makes the numbers look off.");
	}
	
	/**
	 * The canonical Example. 
	 * 
	 * This example is using static calls to UmTrace, using a thread local to keep track of state. 
	 * 
	 * <ul>
	 * <li>UMTRACE Aggregates. Node: UmTrace [path=/ProgramStart] Time: 1,001000 s. Pct: 100,00%
	 * <li>UMTRACE Aggregates. Node: UmTrace [path=/ProgramStart/Task 1] Time: 0,100000 s. Pct: 9,99%
	 * <li>UMTRACE Aggregates. Node: UmTrace [path=/ProgramStart/Task 2] Time: 0,900000 s. Pct: 89,91%
	 * </ul>
	 */
	@Test
	void testCanonical(){
		printBar("Canonical (default context)");
		
		// For funny reasons a root log is needed, or results will seem funny. 
		UmTrace.enter("ProgramStart");
	
		UmTrace.enter("Task 1");
		stop(100);
		UmTrace.exit();
		
		UmTrace.enter("Task 2");
		stop(900);
		UmTrace.exit();
		
		// Exit the root context will trigger a dump to system out
		UmTrace.exit();
	}

}
