package com.dpbank.api;

import org.springframework.boot.SpringApplication;

public class TestDpBankApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(DpBankApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
