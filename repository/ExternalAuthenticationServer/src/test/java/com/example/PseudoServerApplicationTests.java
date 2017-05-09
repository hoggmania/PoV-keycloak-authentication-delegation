package com.example;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PseudoServerApplicationTests {
	TestRestTemplate restTemplate = new TestRestTemplate();
	
	@LocalServerPort
	int port;

	@Test
	public void contextLoads() {
		assertThat(restTemplate.getForObject("http://localhost:" + port, String.class), is("Guten Tag."));
	}

}
