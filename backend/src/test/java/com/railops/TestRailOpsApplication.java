package com.railops;

import org.springframework.boot.SpringApplication;

public class TestRailOpsApplication {

	public static void main(String[] args) {
		SpringApplication.from(RailOpsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
