package com.drools.test.com.drools.test.kie.file.system;

import java.io.File;

import org.drools.core.ClockType;
import org.junit.Assert;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.builder.model.KieSessionModel.KieSessionType;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.conf.ClockTypeOption;

public class DroolsSessionUtil {

	private static final String RULES_PACKAGE = "com.mlnms.common.fmwk.drools.impl";

	private static final String SESSION_NAME_PREFIX = "KSession_";

	private static final String OUTPUT_DRL_PREFIX = "src" + File.separator + "main" + File.separator + "resources"
			+ File.separator + "com" + File.separator + "common" + File.separator + "fmwk" + File.separator + "drools"
			+ File.separator + "impl" + File.separator;

	private static final String RESULT_PROCESSOR = "resultProcessor";

	private static final String DRL = ".drl";

	public KieSession createNewStatefulSession(DroolsResultProcessor resultProcessor, String rules, String kBaseName) {

		KieServices kieServices = KieServices.Factory.get();
		KieModuleModel kieModuleModel = kieServices.newKieModuleModel();

		KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel(kBaseName)
				.setEventProcessingMode(EventProcessingOption.STREAM).setDefault(true).addPackage(RULES_PACKAGE);

		// Need this to create session.
		KieSessionModel kieSessionModel = kieBaseModel.newKieSessionModel(SESSION_NAME_PREFIX + kBaseName);
		kieSessionModel.setClockType(ClockTypeOption.get(ClockType.PSEUDO_CLOCK.getId()));
		KieFileSystem kfs = kieServices.newKieFileSystem();
		String kieModuleModelStr = kieModuleModel.toXML();
		kfs.writeKModuleXML(kieModuleModelStr);
		System.out.println("kieModuleModel:" + kieModuleModelStr);

		String outFile = OUTPUT_DRL_PREFIX + "result" + DRL;
		System.out.println("output drl location: " + outFile);
		kfs.write(outFile, rules);

		KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();

		Results results = kieBuilder.getResults();
		if (results.hasMessages(Message.Level.ERROR)) {
			Assert.fail("Rule has errors:" + results.getMessages().toString());
		}

		KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());

		results = kieContainer.verify();

		if (results.hasMessages(Message.Level.WARNING, Message.Level.ERROR)) {
			Assert.fail("Rule file has errors:" + results.getMessages().toString());
		}

		KieSession kieSession = kieContainer.newKieSession(SESSION_NAME_PREFIX + kBaseName);
		kieSession.setGlobal(RESULT_PROCESSOR, resultProcessor);

		return kieSession;
	}

	public StatelessKieSession createNewStatelessSession(String rules, String kBaseName) {

		KieServices kieServices = KieServices.Factory.get();
		KieModuleModel kieModuleModel = kieServices.newKieModuleModel();

		KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel(kBaseName).setDefault(true)
				.addPackage(RULES_PACKAGE);

		// Need this to create session.
		KieSessionModel kieSessionModel = kieBaseModel.newKieSessionModel(SESSION_NAME_PREFIX + kBaseName);
		kieSessionModel.setType(KieSessionType.STATELESS);
		KieContainer kieContainer = addRuleFilesToKieContainer(rules, kieServices, kieModuleModel);

		StatelessKieSession kieSession = kieContainer.newStatelessKieSession(SESSION_NAME_PREFIX + kBaseName);

		return kieSession;
	}

	private KieContainer addRuleFilesToKieContainer(String rules, KieServices kieServices,
			KieModuleModel kieModuleModel) {
		KieFileSystem kfs = kieServices.newKieFileSystem();
		String kieModuleModelStr = kieModuleModel.toXML();
		kfs.writeKModuleXML(kieModuleModelStr);
		System.out.println("kieModuleModel:" + kieModuleModelStr);

		String outFile = OUTPUT_DRL_PREFIX + "result" + DRL;
		System.out.println("output drl location: " + outFile);
		kfs.write(outFile, rules);

		KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();

		Results results = kieBuilder.getResults();
		if (results.hasMessages(Message.Level.ERROR)) {
			Assert.fail("Rule has errors:" + results.getMessages().toString());
		}

		KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());

		results = kieContainer.verify();

		if (results.hasMessages(Message.Level.WARNING, Message.Level.ERROR)) {
			Assert.fail("Rule file has errors:" + results.getMessages().toString());
		}
		return kieContainer;
	}

}
