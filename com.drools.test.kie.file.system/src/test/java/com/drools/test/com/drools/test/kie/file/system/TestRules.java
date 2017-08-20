package com.drools.test.com.drools.test.kie.file.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

public class TestRules {

	private static final String NEW_LINE = System.getProperty("line.separator");

	@Test
	public void testStatefulSession() {
		String rules = getFileContentAsString("test.drl");

		DroolsResultProcessor resultProcessor = new DroolsResultProcessor();
		DroolsSessionUtil droolsSessionUtil = new DroolsSessionUtil();
		KieSession kieSession = droolsSessionUtil.createNewStatefulSession(resultProcessor, rules, "KBaseName");

		kieSession.insert(new Message("jack", "hey there!"));
		kieSession.fireAllRules();

		Message message = (Message) resultProcessor.getCaptureEvent();
		Assert.assertNotNull(message);
	}

	@Test
	public void testStatelessSession() {
		String rules = getFileContentAsString("stateless_test.drl");

		DroolsSessionUtil droolsSessionUtil = new DroolsSessionUtil();
		StatelessKieSession kieSession = droolsSessionUtil.createNewStatelessSession(rules, "KBaseName");

		Message message = new Message("jack", "hey there!");

		kieSession.execute(message);
		Assert.assertEquals("I've got your message", message.getText());
	}

	private String getFileContentAsString(String fileName) {
		URL fileUrl = this.getClass().getClassLoader().getResource(fileName);
		if (fileUrl != null) {
			try (InputStream fis = fileUrl.openStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(fis));) {
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
					sb.append(NEW_LINE);
				}
				return sb.toString();
			} catch (IOException e) {
				Assert.fail("Unable to access file:" + fileUrl.toString() + "exception:" + e);
			}
		}
		return null;
	}
}
