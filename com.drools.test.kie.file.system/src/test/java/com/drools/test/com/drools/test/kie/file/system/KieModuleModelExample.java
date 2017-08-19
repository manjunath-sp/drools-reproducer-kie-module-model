
package com.drools.test.com.drools.test.kie.file.system;

import org.drools.core.ClockType;
import org.drools.core.util.Drools;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.builder.model.KieSessionModel.KieSessionType;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.conf.ClockTypeOption;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

public class KieModuleModelExample {

	public void stateFulSessionExecute(PrintStream out) {

		KieServices ks = KieServices.Factory.get();
		KieModuleModel kieModuleModel = ks.newKieModuleModel();

		KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel("kiemodulemodel")
				.setEventProcessingMode(EventProcessingOption.STREAM).setDefault(true)
				.addPackage("org.drools.example.api.kiemodulemodel");

		KieSessionModel kieSessionModel = kieBaseModel.newKieSessionModel("ksession6");
		kieSessionModel.setClockType(ClockTypeOption.get(ClockType.PSEUDO_CLOCK.getId()));
		kieSessionModel.setType(KieSessionType.STATEFUL);
		
		KieFileSystem kfs = ks.newKieFileSystem();
		kfs.writeKModuleXML(kieModuleModel.toXML()); 
		
		System.out.println(kieModuleModel.toXML());
		
		
		kfs.write("src/main/resources/kiemodulemodel/test.drl", getRule()); 
		 
		KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
		if (kieBuilder.getResults().hasMessages(Level.ERROR)) {
			throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
		}

		KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());

		KieSession kSession = kContainer.newKieSession("ksession6");
		kSession.setGlobal("out", out);

		Message msg1 = new Message("Dave", "Hello, HAL. Do you read me, HAL?");
		
		kSession.insert(msg1);
		kSession.fireAllRules();

		Message msg2 = new Message("Dave", "Open the pod bay doors, HAL.");
		kSession.insert(msg2);
		kSession.fireAllRules();

		Message msg3 = new Message("Dave", "What's the problem?");
		kSession.insert(msg3);
		kSession.fireAllRules();
		
		kSession.dispose();
	}
	
	
	public void statelessSessionExecute(PrintStream out) {

		KieServices ks = KieServices.Factory.get();
		KieModuleModel kieModuleModel = ks.newKieModuleModel();

		KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel("kiemodulemodel").setDefault(true)
				.addPackage("org.drools.example.api.kiemodulemodel");

		KieSessionModel kieSessionModel = kieBaseModel.newKieSessionModel("ksession6");
		kieSessionModel.setType(KieSessionType.STATELESS);
		
		KieFileSystem kfs = ks.newKieFileSystem();
		kfs.writeKModuleXML(kieModuleModel.toXML()); 
		System.out.println(kieModuleModel.toXML());
		
		kfs.write("src/main/resources/kiemodulemodel/test.drl", getRule()); 
		 
		KieBuilder kieBuilder = ks.newKieBuilder(kfs).buildAll();
		if (kieBuilder.getResults().hasMessages(Level.ERROR)) {
			throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
		}

		KieContainer kContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
		
		StatelessKieSession kSession = kContainer.newStatelessKieSession("ksession6");

		Message msg1 = new Message("Dave", "Hello, HAL. Do you read me, HAL?");
		Message msg2 = new Message("Dave", "Open the pod bay doors, HAL.");
		Message msg3 = new Message("Dave", "What's the problem?");
		
		kSession.execute(Arrays.asList(new Object[] {msg1,msg2,msg3}));
	}

	@Test
	public void testStateFul(){
		KieModuleModelExample kieModuleModelExample = new KieModuleModelExample();
		kieModuleModelExample.stateFulSessionExecute(System.out);
	}
	
	@Test
	public void testStateless(){
		KieModuleModelExample kieModuleModelExample = new KieModuleModelExample();
		kieModuleModelExample.statelessSessionExecute(System.out);
		//Nothing printed in the console. Rules have not executed
		
	}

	private static String getRule() {
		String s = "" + "package org.drools.example.api.kiemodulemodel \n\n"
				+ "import com.drools.test.com.drools.test.kie.file.system.Message \n\n" + "rule rule6 when \n"
				+ "    Message(text == \"What's the problem?\") \n" + "then\n"
				+ "    insert( new Message(\"HAL\", \"I think you know what the problem is just as well as I do.\" ) ); \n"
				+ "end \n";

		return s;
	}
}
