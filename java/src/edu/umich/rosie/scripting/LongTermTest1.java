package edu.umich.rosie.scripting;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenuBar;
import sml.Identifier;
import edu.umich.rosie.soar.*;
import edu.umich.rosie.language.IMessagePasser;
import edu.umich.rosie.language.IMessagePasser.RosieMessage;
import edu.umich.rosie.language.InternalMessagePasser;
import edu.umich.rosie.language.LanguageConnector.MessageType;

// How to use: in the receiveMessage function, add case statements to the
// switch block that send Rosie commands. Change the number of messages
// that are sent on loop to Rosie by changing the "limit" variable in the
// updateMode function (e.g. if you have 4 case statements, change limit to
// 3)
public class LongTermTest1 extends AgentConnector implements IMessagePasser.IMessageListener{
	int mode;
	double avg_d_time;
	InternalMessagePasser passer;
	SoarAgent agent;
	String soar_stats = ""; 
	long startTime;
	long goalTime = 120000000000L;
	Vector<Double> dataVec = new Vector<Double>();
	
	//Constructor. Initializes mode to zero and sets a reference to the
	//external messagePasser
	public LongTermTest1(SoarAgent agent_in, InternalMessagePasser passer_in) {
		super(agent_in);
		// TODO Auto-generated constructor stub
		mode = 0;
		avg_d_time = 0;
		this.agent = agent_in;
		passer = passer_in;
		this.passer.addMessageListener(this);
//		startTime = System.nanoTime();
//		goalTime = 2 * 60 * 1000000000; // Converting 1 minute to milliseconds
		
	}
	
	public void timeCheck() {
		if (System.nanoTime() - startTime > goalTime) {
			//Send stopSoar command.
			this.agent.stop();
//			System.out.println(System.nanoTime() - startTime);
//			System.out.println(goalTime);
			System.out.println("STOPPED: Time elapsed");
			for (int i = 0; i < dataVec.size(); ++i) {
				System.out.println("Decision: " + i + "  Time: " + dataVec.elementAt(i));
			}
			//Save array to excel 
		}
	}
	
	//Updates the mode variable. Mode is a counter that resets to 0
	//once it attempts to go past the limit value.
	public void updateMode() {
		int limit = 3;
		
		if (mode == limit) {
			mode = 0;
		}
		else {
			++mode;
		}
	}
	
	//Sends Rosie the message specified by msg, of type INSTRUCTOR_MESSAGE,
	//using the passer member variable
	public void sendRosieMessage(String msg) {
		passer.sendMessage(msg, MessageType.INSTRUCTOR_MESSAGE);
	}
	
	//Sends Rosie the correct message depending on what stage of the
	//sequence she's on. Stage of sequence represented by "mode" variable,
	//which is updated by "updateMode()"
	@Override
	public void receiveMessage(RosieMessage message) {
		
		String msg_str = message.message.toString();
			
		timeCheck();
		
		switch (mode) {
		case 0:
			if (msg_str == "Give me a task." || msg_str == "Test me or give me another task.") {
				sendRosieMessage("Pick up the red block.");
				this.updateMode();
			}
			break;
		case 1:
			if (msg_str == "Test me or give me another task.") {
				sendRosieMessage("Put the red block on the table.");
				this.updateMode();
			}
			break;
		case 2:
			if (msg_str == "Test me or give me another task.") {
				sendRosieMessage("Pick up the red block.");
				this.updateMode();
			}
			break;
		
		case 3:
			if (msg_str == "Test me or give me another task.") {
				sendRosieMessage("Put the red block on the pantry.");
				this.updateMode();
			}
			break;
		
		default:
			this.updateMode();
		}
	}
	
	//Parse and print out data returned by Soar stats --cycle command
	public void printBasicCycleTime() {
		Pattern p = Pattern.compile("[\\d].?[\\d]+");
		Matcher m = p.matcher(soar_stats.substring(150, 167));
		m.find();
		String temp = m.group();
		m.find();
		System.out.println("Decision: " + temp + "  Time: "+ m.group());
//		dataVec.addElement(Long.parseLong(temp, 10));
//		dataVec.addElement(Double.parseDouble(m.group()));
//		System.out.println("Decision: " + dataVec.elementAt((int) (Long.parseLong(temp, 10) - 1)));
//		System.out.println("Time: " + dataVec.elementAt((int) (Long.parseLong(temp, 10))));
	}
	
	//Sends command to Soar to print individual cycle time. 
	//Parses and prints the data.
	public void basicCycleTimeData() {
		soar_stats = this.agent.sendCommand("stats -c");
		this.agent.sendCommand("stats -T");
		this.agent.sendCommand("stats -t");
		if (soar_stats.length() > 2) {
			printBasicCycleTime();
		}
	}
	
	//Turns epmem off
	public void epmemOff() {
		this.agent.sendCommand("epmem -d");
	}
	
	//Turns epmem on
	public void epmemOn() {
		this.agent.sendCommand("epmem -e");
	}
	
	//Parses and prints the Soar output from the memory command
	public void printMemPoolData() {
		if (soar_stats != null) {
			System.out.println(soar_stats);
		}
	}
	
	//Sends command to Soar to return memory data. Parses and prints out the data.
	public void memPoolData() {
		soar_stats = this.agent.sendCommand("stats -m");
		printMemPoolData();
	}
	
	//Need both stats -T and stats -t, without -T only maximum data will be reported.
	@Override
	protected void onInputPhase(Identifier inputLink) {
		basicCycleTimeData();
		if (soar_stats.length() < 2) {
			epmemOff();
			startTime = System.nanoTime();
		}
//		memPoolData();
	}
	
	@Override
	protected void onOutputEvent(String attName, Identifier id) {
		// TODO Auto-generated method stub
		
	}
	

	//ERROR: Does not appear to be doing anything. Ask Aaron about this.
	@Override
	protected void onInitSoar() {
		//Enable tracking so that the "stats -C" command works
		this.agent.sendCommand("stats -t");
		//Enable epmem usage. Change to '-d' to disable.
		this.agent.sendCommand("epmem -d");
	}

	@Override
	protected void createMenu(JMenuBar menuBar) {
		// TODO Auto-generated method stub
		
	}

	
	
}
